// RUN_PIPELINE_TILL: FRONTEND
// LANGUAGE: +ContextParameters

<!UNSUPPORTED!>context(_: String)<!>
class C {
    <!UNSUPPORTED!>context(_: String)<!>
    constructor() {}
}

class C2 {
    <!UNSUPPORTED!>context(_: String)<!>
    constructor() {}
}

<!UNSUPPORTED!>context(_: String)<!>
interface I

<!UNSUPPORTED!>context(_: String)<!>
enum class E

<!UNSUPPORTED!>context(_: String)<!>
annotation class A

<!UNSUPPORTED!>context(_: String)<!>
object O

<!UNSUPPORTED!>context(_: String)<!>
typealias TA = Any

val objectExpression = <!UNRESOLVED_REFERENCE!>context<!>(<!UNRESOLVED_REFERENCE!>_<!><!SYNTAX!>: String<!>)<!SYNTAX!><!> object<!SYNTAX!><!> {}

<!UNSUPPORTED!>context(_: String)<!>
operator fun Any.getValue(thiz: Any?, metadata: Any?): Any = this

<!UNSUPPORTED!>context(_: String)<!>
operator fun Any.setValue(thiz: Any?, metadata: Any?, value: String): Any = this

<!UNSUPPORTED!>context(_: String)<!>
operator fun Any.provideDelegate(thiz: Any?, metadata: Any?): Any = this

var x: String = ""
    context(_: String) <!SYNTAX!>get<!>
    context(_: String) <!SYNTAX!>set<!>

var y: String = ""
    context(_: String) <!SYNTAX!>get<!><!SYNTAX!>(<!><!SYNTAX!>)<!> <!SYNTAX!>=<!> <!SYNTAX!>"<!><!SYNTAX!>"<!>
    context(_: String) <!SYNTAX!>set<!><!SYNTAX!>(<!><!SYNTAX!>v<!><!SYNTAX!>)<!> <!FUNCTION_DECLARATION_WITH_NO_NAME!><!SYNTAX!><!>{}<!>

<!UNSUPPORTED!>context(_: String)<!>
var Any.d <!DELEGATE_SPECIAL_FUNCTION_NONE_APPLICABLE!>by<!> ""

context(_: String)
var b = <!CONTEXT_RECEIVERS_WITH_BACKING_FIELD!>""<!>

context(<!SYNTAX!><!>)
fun emptyContext() {}

context(<!SYNTAX!><!>)
class EmptyContextClass

context(<!CONTEXT_PARAMETER_WITHOUT_NAME!>String<!>)
fun contextReceiverSyntax() {}

context(<!CONTEXT_PARAMETER_WITHOUT_NAME!>String<!>, _: Int)
fun mixedSyntax() {}

<!UNSUPPORTED!>context(String)<!>
class ClassWithContextReceiverSyntax {
    <!UNSUPPORTED!>context(String)<!>
    constructor() {}
}

<!UNSUPPORTED!>context(x: String = <!CONTEXT_PARAMETER_WITH_DEFAULT, CONTEXT_PARAMETER_WITH_DEFAULT!>""<!>)<!>
class ClassWithContextDefaultValue

context(x: String = <!CONTEXT_PARAMETER_WITH_DEFAULT!>""<!>)
val contextHasDefaultValue: String get() = ""

context(x: String = <!CONTEXT_PARAMETER_WITH_DEFAULT!>""<!>)
fun contextHasDefaultValue() {}
