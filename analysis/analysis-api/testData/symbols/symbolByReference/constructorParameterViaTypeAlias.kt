// DO_NOT_CHECK_SYMBOL_RESTORE_K2
// DO_NOT_CHECK_NON_PSI_SYMBOL_RESTORE
package test

class MyClass(param: String)

typealias MyAlias = MyClass

fun usage() {
    MyAlias(<caret>param = "hello")
}