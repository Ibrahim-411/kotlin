/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("AtomicArraysKt")

@file:OptIn(ExperimentalStdlibApi::class)

package kotlin.concurrent

import java.util.concurrent.atomic.*

/**
 * Casts the given [AtomicIntArray] instance to [java.util.concurrent.atomic.AtomicIntegerArray].
 */
@SinceKotlin("2.1")
@Suppress("UNCHECKED_CAST")
public fun AtomicIntArray.asJavaAtomicArray(): AtomicIntegerArray = this as AtomicIntegerArray

/**
 * Casts the given [java.util.concurrent.atomic.AtomicIntegerArray] instance to [AtomicIntArray].
 */
@SinceKotlin("2.1")
@Suppress("UNCHECKED_CAST")
public fun java.util.concurrent.atomic.AtomicIntegerArray.asKotlinAtomicArray(): AtomicIntArray = this as AtomicIntArray

/**
 * Casts the given [AtomicLongArray] instance to [java.util.concurrent.atomic.AtomicLongArray].
 */
@SinceKotlin("2.1")
@Suppress("UNCHECKED_CAST")
public fun AtomicLongArray.asJavaAtomicArray(): java.util.concurrent.atomic.AtomicLongArray = this as java.util.concurrent.atomic.AtomicLongArray

/**
 * Casts the given [java.util.concurrent.atomic.AtomicLongArray] instance to [AtomicLongArray].
 */
@SinceKotlin("2.1")
@Suppress("UNCHECKED_CAST")
public fun java.util.concurrent.atomic.AtomicLongArray.asKotlinAtomicArray(): AtomicLongArray = this as AtomicLongArray

/**
 * Casts the given [AtomicArray]<T> instance to [java.util.concurrent.atomic.AtomicReferenceArray]<T>.
 */
@SinceKotlin("2.1")
@Suppress("UNCHECKED_CAST")
public fun <T> AtomicArray<T>.asJavaAtomicArray(): AtomicReferenceArray<T> = this as AtomicReferenceArray<T>

/**
 * Casts the given [java.util.concurrent.atomic.AtomicReferenceArray]<T> instance to [AtomicArray]<T>.
 */
@SinceKotlin("2.1")
@Suppress("UNCHECKED_CAST")
public fun <T> java.util.concurrent.atomic.AtomicReferenceArray<T>.asKotlinAtomicArray(): AtomicArray<T> = this as AtomicArray<T>