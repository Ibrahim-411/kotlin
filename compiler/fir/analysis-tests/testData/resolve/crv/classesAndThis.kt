// WITH_STDLIB

class A(val x: String = "x") {
    fun foo(y: String): A {
        y // local, should not report
        <!RETURN_VALUE_NOT_USED!>x<!> // unused, may have getter
        this // should not report
        return this // used
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as A

        if (x != other.x) return false

        return true
    }
}

interface I {
    fun foo()
}

object Impl: I {
    override fun foo() {
        <!RETURN_VALUE_NOT_USED!>Impl<!>
        TODO("Not yet implemented")
    }
}

class Impl2(): I by Impl

annotation class Bar(
    val a: IntArray = [1, 2],
    val b: IntArray = intArrayOf(1, 2)
)
