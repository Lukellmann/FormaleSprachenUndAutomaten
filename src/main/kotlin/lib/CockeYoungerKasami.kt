@file:Suppress("SpellCheckingInspection")

package lib


typealias CYKAction = (d1: Int, i1: Int, j1: Int, k1: Int, matrix: Array<out Array<out Set<Nonterminal>>>, new: Set<Nonterminal>) -> Unit

fun ContextFreeGrammar.cockeYoungerKasami(
    word: TerminalWord,
    action: CYKAction = { _, _, _, _, _, _ -> },
): Boolean {
    require(terminalAlphabet.containsAll(word)) {
        "Not all symbols of word ${word.wordToString()} are elements of terminal alphabet ${terminalAlphabet.setToString()}"
    }
    require(isInChomskyNormalForm) { "\nGrammar\n$this\nis not in Chomsky normal form" }

    if (word.isEpsilon) return false // no grammar in Chomsky normal form can produce an empty word

    val n = word.size
    val matrix =
        Array(n) {
            Array(n) {
                emptySet<Nonterminal>()
            }
        }

    for (i1 in 1..n) {
        val i0 = i1 - 1
        matrix[i0][i0] = productions
            .filter { it.singleTerminalRightOrNull == word[i0] }
            .map { it.nonterminalLeft }
            .toSet()
    }

    for (d1 in 1 until n) {
        for (i1 in 1..(n - d1)) {
            val j1 = i1 + d1
            for (k1 in i1 until j1) {

                val i0 = i1 - 1
                val j0 = j1 - 1
                val k0 = k1 - 1

                val new = productions
                    .filter {
                        val (b, c) = it.nonterminalPairRightOrNull ?: return@filter false
                        b in matrix[i0][k0] && c in matrix[k0 + 1][j0]
                    }
                    .map { it.nonterminalLeft }
                    .toSet()
                matrix[i0][j0] += new

                action(d1, i1, j1, k1, matrix, new)
            }
        }
    }

    return startSymbol in matrix[0][n - 1]
}


fun ContextFreeGrammar.cockeYoungerKasamiWithOutput(word: TerminalWord): Boolean {
    var lastD1: Int? = null
    var lastI1: Int? = null
    var lastJ1: Int? = null

    return cockeYoungerKasami(word) { d1, i1, j1, k1, matrix, new ->
        val i0 = i1 - 1
        val j0 = j1 - 1
        val k0 = k1 - 1

        var someChanged = false
        println(
            "${
                if (d1 != lastD1) "d = $d1; ".also { someChanged = true } else " ".repeat(7)
            }${
                if (someChanged || i1 != lastI1) "i = $i1; ".also { someChanged = true } else " ".repeat(7)
            }${
                if (someChanged || j1 != lastJ1) "j = $j1:   ".also { someChanged = true } else " ".repeat(9)
            }${
                if (someChanged || i1 != lastI1 || j1 != lastJ1) "N$i1,$j1: " else " ".repeat(6)
            }k = $k1:   N$i1,$k1: ${matrix[i0][k0]}; N${k1 + 1},$j1: ${matrix[k0 + 1][j0]} -> N$i1,$j1: $new"
        )

        lastD1 = d1
        lastI1 = i1
        lastJ1 = j1
    }
}
