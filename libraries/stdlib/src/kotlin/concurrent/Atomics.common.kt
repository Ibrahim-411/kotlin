/*
 * Copyright 2010-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package kotlin.concurrent

import kotlin.internal.ActualizeByJvmBuiltinProvider

/**
 * An [Int] value that may be updated atomically.
 *
 * Platform-specific implementation details:
 *
 * When targeting the Native backend, [AtomicInt] stores a volatile [Int] variable and atomically updates it.
 * For additional details about atomicity guarantees for reads and writes see [kotlin.concurrent.Volatile].
 *
 * When targeting the JVM, instances of [AtomicInt] are represented by [java.util.concurrent.atomic.AtomicInteger].
 * For details about guarantees of volatile accesses and updates of atomics refer to The Java Language Specification (17.4 Memory Model).
 *
 * For JS and Wasm [AtomicInt] is implemented trivially and is not thread-safe since these platforms do not support multi-threading.
 */
@ActualizeByJvmBuiltinProvider
@ExperimentalStdlibApi
public expect class AtomicInt public constructor(value: Int) {
    /**
     * Atomically gets the value of the atomic.
     */
    public fun load(): Int

    /**
     * Atomically sets the value of the atomic to the [new value][newValue].
     */
    public fun store(newValue: Int)

    /**
     * Atomically sets the value to the given [new value][newValue] and returns the old value.
     */
    public fun exchange(newValue: Int): Int

    /**
     * Atomically sets the value to the given [new value][newValue] if the current value equals the [expected value][expectedValue],
     * returns true if the operation was successful and false only if the current value was not equal to the expected value.
     *
     * This operation has so-called strong semantics,
     * meaning that it returns false if and only if current and expected values are not equal.
     *
     * Comparison of values is done by value.
     */
    public fun compareAndSet(expectedValue: Int, newValue: Int): Boolean

    /**
     * Atomically sets the value to the given [new value][newValue] if the current value equals the [expected value][expectedValue]
     * and returns the old value in any case.
     *
     * This operation has so-called strong semantincs,
     * meaning that it returns false if and only if current and expected values are not equal.
     *
     * Comparison of values is done by value.
     */
    public fun compareAndExchange(expectedValue: Int, newValue: Int): Int

    /**
     * Atomically adds the [given value][delta] to the current value and returns the old value.
     */
    public fun fetchAndAdd(delta: Int): Int

    /**
     * Atomically adds the [given value][delta] to the current value and returns the new value.
     */
    public fun addAndFetch(delta: Int): Int

    /**
     * Returns the string representation of the underlying [Int] value.
     *
     * This operation does not provide any atomicity guarantees.
     */
    public override fun toString(): String
}

/**
 * Atomically adds the [given value][delta] to the current value.
 */
@ExperimentalStdlibApi
public operator fun AtomicInt.plusAssign(delta: Int): Unit { this.addAndFetch(delta) }

/**
 * Atomically subtracts the [given value][delta] from the current value.
 */
@ExperimentalStdlibApi
public operator fun AtomicInt.minusAssign(delta: Int): Unit { this.addAndFetch(-delta) }

/**
 * Atomically increments the current value by one and returns the old value.
 */
@ExperimentalStdlibApi
public fun AtomicInt.fetchAndIncrement(): Int = this.fetchAndAdd(1)

/**
 * Atomically increments the current value by one and returns the new value.
 */
@ExperimentalStdlibApi
public fun AtomicInt.incrementAndFetch(): Int = this.addAndFetch(1)

/**
 * Atomically decrements the current value by one and returns the new value.
 */
@ExperimentalStdlibApi
public fun AtomicInt.decrementAndFetch(): Int = this.addAndFetch(-1)

/**
 * Atomically decrements the current value by one and returns the old value.
 */
@ExperimentalStdlibApi
public fun AtomicInt.fetchAndDecrement(): Int = this.fetchAndAdd(-1)

/**
 * A [Long] value that may be updated atomically.
 *
 * Platform-specific implementation details:
 *
 * When targeting the Native backend, [AtomicLong] stores a volatile [Long] variable and atomically updates it.
 * For additional details about atomicity guarantees for reads and writes see [kotlin.concurrent.Volatile].
 *
 * When targeting the JVM, instances of [AtomicLong] are represented by [java.util.concurrent.atomic.AtomicLong].
 * For details about guarantees of volatile accesses and updates of atomics refer to The Java Language Specification (17.4 Memory Model).
 *
 * For JS and Wasm [AtomicLong] is implemented trivially and is not thread-safe since these platforms do not support multi-threading.
 */
@ActualizeByJvmBuiltinProvider
@ExperimentalStdlibApi
public expect class AtomicLong public constructor(value: Long) {
    /**
     * Atomically gets the value of the atomic.
     */
    public fun load(): Long

    /**
     * Atomically sets the value of the atomic to the [new value][newValue].
     */
    public fun store(newValue: Long)

    /**
     * Atomically sets the value to the given [new value][newValue] and returns the old value.
     */
    public fun exchange(newValue: Long): Long

    /**
     * Atomically sets the value to the given [new value][newValue] if the current value equals the [expected value][expectedValue],
     * returns true if the operation was successful and false only if the current value was not equal to the expected value.
     *
     * This operation has so-called strong semantics,
     * meaning that it returns false if and only if current and expected values are not equal.
     *
     * Comparison of values is done by value.
     */
    public fun compareAndSet(expectedValue: Long, newValue: Long): Boolean

    /**
     * Atomically sets the value to the given [new value][newValue] if the current value equals the [expected value][expectedValue]
     * and returns the old value in any case.
     *
     * This operation has so-called strong semantics,
     * meaning that it returns false if and only if current and expected values are not equal.
     *
     * Comparison of values is done by value.
     */
    public fun compareAndExchange(expectedValue: Long, newValue: Long): Long

    /**
     * Atomically adds the [given value][delta] to the current value and returns the old value.
     */
    public fun fetchAndAdd(delta: Long): Long

    /**
     * Atomically adds the [given value][delta] to the current value and returns the new value.
     */
    public fun addAndFetch(delta: Long): Long

    /**
     * Returns the string representation of the underlying [Long] value.
     *
     * This operation does not provide any atomicity guarantees.
     */
    public override fun toString(): String
}

/**
 * Atomically adds the [given value][delta] to the current value.
 */
@ExperimentalStdlibApi
public operator fun AtomicLong.plusAssign(delta: Long): Unit { this.addAndFetch(delta) }

/**
 * Atomically subtracts the [given value][delta] from the current value.
 */
@ExperimentalStdlibApi
public operator fun AtomicLong.minusAssign(delta: Long): Unit { this.addAndFetch(-delta) }

/**
 * Atomically increments the current value by one and returns the old value.
 */
@ExperimentalStdlibApi
public fun AtomicLong.fetchAndIncrement(): Long = this.fetchAndAdd(1)

/**
 * Atomically increments the current value by one and returns the new value.
 */
@ExperimentalStdlibApi
public fun AtomicLong.incrementAndFetch(): Long = this.addAndFetch(1)

/**
 * Atomically decrements the current value by one and returns the new value.
 */
@ExperimentalStdlibApi
public fun AtomicLong.decrementAndFetch(): Long = this.addAndFetch(-1)

/**
 * Atomically decrements the current value by one and returns the old value.
 */
@ExperimentalStdlibApi
public fun AtomicLong.fetchAndDecrement(): Long = this.fetchAndAdd(-1)

/**
 * A [Boolean] value that may be updated atomically.
 *
 * Platform-specific implementation details:
 *
 * When targeting the Native backend, [AtomicBoolean] stores a volatile [Boolean] variable and atomically updates it.
 * For additional details about atomicity guarantees for reads and writes see [kotlin.concurrent.Volatile].
 *
 * When targeting the JVM, instances of [AtomicBoolean] are represented by [java.util.concurrent.atomic.AtomicInteger].
 * For details about guarantees of volatile accesses and updates of atomics refer to The Java Language Specification (17.4 Memory Model).
 *
 * For JS and Wasm [AtomicBoolean] is implemented trivially and is not thread-safe since these platforms do not support multi-threading.
 */
@ActualizeByJvmBuiltinProvider
@ExperimentalStdlibApi
public expect class AtomicBoolean public constructor(value: Boolean) {
    /**
     * Atomically gets the value of the atomic.
     */
    public fun load(): Boolean

    /**
     * Atomically sets the value of the atomic to the [new value][newValue].
     */
    public fun store(newValue: Boolean)

    /**
     * Atomically sets the value to the given [new value][newValue] and returns the old value.
     */
    public fun exchange(newValue: Boolean): Boolean

    /**
     * Atomically sets the value to the given [new value][newValue] if the current value equals the [expected value][expectedValue],
     * returns true if the operation was successful and false only if the current value was not equal to the expected value.
     *
     * This operation has so-called strong semantics,
     * meaning that it returns false if and only if current and expected values are not equal.
     *
     * Comparison of values is done by value.
     */
    public fun compareAndSet(expectedValue: Boolean, newValue: Boolean): Boolean

    /**
     * Atomically sets the value to the given [new value][newValue] if the current value equals the [expected value][expectedValue]
     * and returns the old value in any case.
     *
     * This operation has so-called strong semantics,
     * meaning that it returns false if and only if current and expected values are not equal.
     *
     * Comparison of values is done by value.
     */
    public fun compareAndExchange(expectedValue: Boolean, newValue: Boolean): Boolean

    /**
     * Returns the string representation of the current [Boolean] value.
     */
    public override fun toString(): String
}

/**
 * An object reference that may be updated atomically.
 *
 * Platform-specific implementation details:
 *
 * When targeting the Native backend, [AtomicReference] stores a volatile variable of type [T] and atomically updates it.
 * For additional details about atomicity guarantees for reads and writes see [kotlin.concurrent.Volatile].
 *
 * When targeting the JVM, instances of [AtomicReference] are represented by [java.util.concurrent.atomic.AtomicReference].
 * For details about guarantees of volatile accesses and updates of atomics refer to The Java Language Specification (17.4 Memory Model).
 *
 * For JS and Wasm [AtomicReference] is implemented trivially and is not thread-safe since these platforms do not support multi-threading.
 */
@ActualizeByJvmBuiltinProvider
@ExperimentalStdlibApi
public expect class AtomicReference<T> public constructor(value: T) {
    /**
     * Atomically gets the value of the atomic.
     */
    public fun load(): T

    /**
     * Atomically sets the value of the atomic to the [new value][newValue].
     */
    public fun store(newValue: T)

    /**
     * Atomically sets the value to the given [new value][newValue] and returns the old value.
     */
    public fun exchange(newValue: T): T

    /**
     * Atomically sets the value to the given [new value][newValue] if the current value equals the [expected value][expectedValue],
     * returns true if the operation was successful and false only if the current value was not equal to the expected value.
     *
     * This operation has so-called strong semantics,
     * meaning that it returns false if and only if current and expected values are not equal.
     *
     * Comparison of values is done by reference.
     */
    public fun compareAndSet(expectedValue: T, newValue: T): Boolean

    /**
     * Atomically sets the value to the given [new value][newValue] if the current value equals the [expected value][expectedValue]
     * and returns the old value in any case.
     *
     * This operation has so-called strong semantics,
     * meaning that it returns false if and only if current and expected values are not equal.
     *
     * Comparison of values is done by reference.
     */
    public fun compareAndExchange(expectedValue: T, newValue: T): T

    /**
     * Returns the string representation of the underlying object.
     *
     * This operation does not provide any atomicity guarantees.
     */
    public override fun toString(): String
}
