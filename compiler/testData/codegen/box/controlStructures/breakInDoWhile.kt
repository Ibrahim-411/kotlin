// IGNORE_BACKEND: JS_IR, JS_IR_ES6
// ISSUE: KT-73130
fun foo(): String {
    var value         = 10
    var test          = 23
    var someCondition = true

    do {
        if (someCondition) {
            break
        }

        return "FAIL"
    } while (value != test)
    return "OK"
}

fun box() = foo()
