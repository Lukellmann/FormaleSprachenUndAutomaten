package lib.chomsky01


private const val sigmaSize = 2 // sigma = {a, b}


fun main() {
    val seen = mutableSetOf<String>()

    (0..10000).forEach { n ->
        val palindrome = palindrome(n)

        println(palindrome)

        require(palindrome !in seen)
        seen += palindrome

        val length = palindrome.length
        require(length >= 2)
        require(length % 2 == 0)
        require(palindrome.substring(0, length / 2) == palindrome.substring(length / 2, length).reversed())
    }
}


// surjective (and injective -> bijective) recursive function
private fun palindrome(n: Int): String {
    require(n >= 0)

    return when (n) {
        0 -> "aa"
        1 -> "bb"
        else -> {
            val group = (1..Int.MAX_VALUE)
                .asSequence()
                .takeWhile { j -> groupIndex(j) <= n }
                .last()

            if (n - groupIndex(group) < sigmaSize.pow(group)) {
                "a${palindrome(groupIndex(group - 1) + n - groupIndex(group))}a"
            } else {
                "b${palindrome(groupIndex(group - 1) + n - groupIndex(group) - sigmaSize.pow(group))}b"
            }
        }
    }
}


private fun groupIndex(group: Int): Int {
    require(group >= 0)

    return when (group) {
        0 -> 0
        else -> (1..group).sumOf { i -> sigmaSize.pow(i) }
    }
}


private fun Int.pow(exp: Int): Int {
    require(exp >= 0)

    var result = 1
    repeat(exp) { result *= this }
    return result
}
