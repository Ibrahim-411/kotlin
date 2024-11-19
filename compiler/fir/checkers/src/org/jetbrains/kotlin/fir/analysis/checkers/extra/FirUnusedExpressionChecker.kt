/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.extra

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirBlockChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirAnonymousInitializer
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.impl.FirContractCallBlock
import org.jetbrains.kotlin.fir.references.symbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirReceiverParameterSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.isSubtypeOf
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.fir.visitors.FirVisitor

object FirUnusedExpressionChecker : FirBlockChecker(MppCheckerKind.Common) {
    override fun check(expression: FirBlock, context: CheckerContext, reporter: DiagnosticReporter) {
        // Only consider "top-level" FirBlocks like bodies for FirFunctions and FirAnonymousInitializers.
        if (context.containingElements.any { it !== expression && it is FirBlock }) return
        if (expression is FirContractCallBlock) return

        // A "used" FirBlock is one that uses the last statement as an implicit return.
        // If the containing element of the block is an FirFunction or FirAnonymousInitializer, the block is unused.
        // If the containing element is a try-catch-finally, possible when used as the initializer of a class property,
        // the block is unused only for the finally block.
        // All other FirBlocks are considered used.
        val containingElement = context.containingElements.last { it !is FirBlock }
        val state = when (containingElement) {
            is FirFunction, is FirAnonymousInitializer -> UsageState.Unused
            is FirTryExpression -> if (containingElement.finallyBlock === expression) UsageState.Unused else UsageState.Used
            else -> UsageState.Used
        }

        val visitor = UsageVisitor(context, reporter)
        expression.accept(visitor, state)
    }

    private enum class UsageState {
        Used,
        Unused,
    }

    private class UsageVisitor(
        private val context: CheckerContext,
        private val reporter: DiagnosticReporter,
    ) : FirVisitor<Unit, UsageState>() {
        override fun visitElement(element: FirElement, data: UsageState) {
            if (data == UsageState.Unused && element is FirExpression && !element.hasSideEffect()) {
                val factory = when {
                    element is FirAnonymousFunctionExpression && element.anonymousFunction.isLambda
                        -> FirErrors.UNUSED_LAMBDA_EXPRESSION
                    else -> FirErrors.UNUSED_EXPRESSION
                }
                reporter.reportOn(element.source, factory, context)
                return
            }

            when (element) {
                // Annotations can just be ignored.
                is FirAnnotation -> Unit

                is FirWhenExpression -> {
                    when (val variable = element.subjectVariable) {
                        null -> element.subject?.accept(this, UsageState.Used)
                        else -> variable.accept(this, UsageState.Used)
                    }
                    for (branch in element.branches) {
                        branch.condition.accept(this, UsageState.Used)
                        branch.result.accept(this, data)
                    }
                }

                is FirTryExpression -> {
                    element.tryBlock.accept(this, data)
                    for (catch in element.catches) {
                        catch.block.accept(this, data)
                    }
                    element.finallyBlock?.accept(this, UsageState.Unused)
                }

                is FirBlock -> {
                    // Increment and decrement operators are always considered used (because they have a side effect).
                    if (element.source?.kind is KtFakeSourceElementKind.DesugaredIncrementOrDecrement) return

                    val statements = element.statements
                    val lastIndex = statements.lastIndex
                    for (i in statements.indices) {
                        val statement = statements[i]
                        val isImplicitReturn = i == lastIndex &&
                                statement is FirExpression &&
                                statement.resolvedType.isSubtypeOf(element.resolvedType, context.session)
                        statement.accept(this, if (isImplicitReturn) data else UsageState.Unused)
                    }
                }

                is FirLoop -> {
                    element.condition.accept(this, UsageState.Used)
                    element.block.accept(this, UsageState.Unused)
                }

                is FirFunction -> {
                    val lastStatementUsed = element is FirAnonymousFunction && element.isLambda
                    element.acceptChildren(this, if (lastStatementUsed) UsageState.Used else UsageState.Unused)
                }

                is FirAnonymousInitializer -> {
                    element.acceptChildren(this, UsageState.Unused)
                }

                // Default to everything being used
                else -> element.acceptChildren(this, UsageState.Used)
            }
        }
    }
}

/**
 * Elements with side effects are those that may execute some other expressions when executed.
 * This includes functions (as they inherently are defined as having side effects), access of
 * properties with custom getters, and may other types within the FIR tree.
 *
 * Note: ***be conservative***. Indicating an [FirExpression] is side-effect-free should only be
 * done for elements, which when removed from the code, won't impact the behavior of the code.
 */
private fun FirExpression.hasSideEffect(): Boolean {
    return when (this) {
        // Literals and references that are known to be side-effect-free.
        is FirLiteralExpression,
        is FirClassReferenceExpression,
        is FirResolvedQualifier,
        is FirThisReceiverExpression,
            -> false

        // The definition of an anonymous function is side-effect-free.
        // Invoking an anonymous function has side effects, but this is performed by another FIR element.
        is FirAnonymousFunctionExpression,
            -> false

        // A smart cast has a side effect iff its original expression has a side effect.
        is FirSmartCastExpression -> {
            originalExpression.hasSideEffect()
        }

        // A callable reference is side-effect-free only if all of its receivers are side-effect-free.
        is FirCallableReferenceAccess -> {
            dispatchReceiver?.hasSideEffect() == true ||
                    extensionReceiver?.hasSideEffect() == true ||
                    explicitReceiver?.hasSideEffect() == true
        }

        // String concatenation and class access are side-effect-free if all arguments are side-effect-free.
        is FirStringConcatenationCall,
        is FirGetClassCall,
            -> {
            arguments.any { it.hasSideEffect() }
        }

        // Property access is side-effect-free if the referenced property does not have a custom getter.
        // However, this check is limited to just considering value parameters, receiver parameters, and
        // local properties without delegates as side-effect-free, to be conservative and match K1 behavior.
        is FirPropertyAccessExpression -> {
            if (source?.kind == KtFakeSourceElementKind.ReferenceInAtomicQualifiedAccess) true
            else when (val propertySymbol = calleeReference.symbol) {
                is FirValueParameterSymbol, is FirReceiverParameterSymbol -> false
                is FirPropertySymbol -> !propertySymbol.isLocal || propertySymbol.hasDelegate
                else -> true
            }
        }

        else -> true
    }
}
