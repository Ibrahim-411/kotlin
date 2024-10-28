// LANGUAGE: +MultiPlatformProjects

// MODULE: main
// TARGET_PLATFORM: Common
// FILE: main.kt
package app

expect class Foo() {
    val text: String
}

fun main() {
    println(Foo().text)
}

// MODULE: jvm()()(main)
// TARGET_PLATFORM: JVM
// FILE: jvm.kt
package app

actual class Foo {
    actual val text = "Hello, world!"
}