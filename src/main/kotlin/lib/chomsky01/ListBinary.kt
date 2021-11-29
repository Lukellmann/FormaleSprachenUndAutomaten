package lib.chomsky01


fun main() {
    (0..100).forEach { n -> println("$n: ${binaryNumber(n)}") }
}


private fun binaryNumber(n: Int): String {
    require(n >= 0)

    return when (n) {
        0 -> "0"
        1 -> "1"
        else -> binaryNumber(n / 2) + if (n % 2 == 0) "0" else "1"
    }
}
