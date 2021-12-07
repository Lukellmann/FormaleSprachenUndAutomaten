@file:Suppress("UNUSED_PARAMETER")

package lib.computability


fun main() {
    println("4 + 23 = ${add(4, 23)}")
    println("14 * 45 = ${mul(14, 45)}")
    println("6 ^ 5 = ${pot(6, 5)}")
}


private fun gAdd(x: Int) = x

private fun hAdd(x1: Int, x2: Int, x3: Int) = x3 + 1

private fun add(x: Int, y: Int): Int = when (y) {
    0 -> gAdd(x)
    else -> hAdd(x, y - 1, add(x, y - 1))
}


private fun gMul(x: Int) = 0

private fun hMul(x1: Int, x2: Int, x3: Int) = add(x1, x3)

private fun mul(x: Int, y: Int): Int = when (y) {
    0 -> gMul(x)
    else -> hMul(x, y - 1, mul(x, y - 1))
}


private fun gPot(x: Int) = 1

private fun hPot(x1: Int, x2: Int, x3: Int) = mul(x1, x3)

private fun pot(x: Int, y: Int): Int = when (y) {
    0 -> gPot(x)
    else -> hPot(x, y - 1, pot(x, y - 1))
}
