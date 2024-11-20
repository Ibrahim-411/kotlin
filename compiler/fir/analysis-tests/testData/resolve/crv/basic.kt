// WITH_STDLIB

fun stringF(): String = ""

fun Any.consume(): Unit = Unit

fun returnsExp() = stringF()

fun returnsBody(): String {
    return stringF()
}

fun vals() {
    val used: String
    used = stringF()
    lateinit var used2: String
    used2 = stringF()
}

class Inits {
    val init1 = stringF()

    val explicit: String
        get() = stringF()

    val unused: String
        get() {
            <!RETURN_VALUE_NOT_USED!>stringF()<!>
            return ""
        }
}

fun defaultValue(param: String = stringF()) {}

fun basic() {
    val used = stringF() // used
    println(stringF()) // used
    <!RETURN_VALUE_NOT_USED!>stringF()<!> // unused
}

fun stringConcat(): String {
    <!RETURN_VALUE_NOT_USED!>"42"<!> // unsued
    val x = "42"
    <!RETURN_VALUE_NOT_USED!>"answer is $x"<!> // unused
    val y = "answer is $x" // used
    return "answer is $y" // used
}

fun throws(): Nothing {
    <!RETURN_VALUE_NOT_USED!>IllegalStateException()<!> // unused
    throw IllegalStateException()
}

fun createE() = IllegalStateException() // used

fun throws2() {
    <!RETURN_VALUE_NOT_USED!>createE()<!> // unused
    throw createE() // used
}

fun usesNothing() {
    throws() // should not be reported as unused
}
