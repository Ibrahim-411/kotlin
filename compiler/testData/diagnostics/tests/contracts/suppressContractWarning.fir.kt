// RUN_PIPELINE_TILL: FRONTEND
// OPT_IN: kotlin.contracts.ExperimentalContracts

import kotlin.contracts.*

// Force an error diagnostic to be present so the test pipeline only runs FRONTEND.
fun runFrontendOnly(): <!UNRESOLVED_REFERENCE!>UnknownType<!> = null!!

fun test1Warning(block: () -> Unit) {
    contract {
        <!WRONG_INVOCATION_KIND!>callsInPlace(block, InvocationKind.EXACTLY_ONCE)<!>
    }
}

fun test1Suppress(block: () -> Unit) {
    @Suppress("WRONG_INVOCATION_KIND")
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
}

fun test2Warning(block: () -> Unit) {
    contract {
        <!WRONG_INVOCATION_KIND!>callsInPlace(block, InvocationKind.EXACTLY_ONCE)<!>
    }
}

fun test2Suppress(block: () -> Unit) {
    contract {
        @Suppress("WRONG_INVOCATION_KIND")
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
}
