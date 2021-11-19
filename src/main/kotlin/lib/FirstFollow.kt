package lib


fun ContextFreeGrammar.first(word: Word): Set<Terminal>? {
    require(alphabet.containsAll(word)) {
        "Not all symbols of word ${word.wordToString()} are elements of alphabet ${alphabet.setToString()}"
    }
    require(productions.any { it.nonterminalLeft == word.singleOrNull() || it.right containsSubWord word }) {
        "first only takes nonterminal left-hand sides or sub-words of right-hand sides of context-free productions"
    }

    // avoid infinite recursion
    val visited = mutableSetOf<Nonterminal>()


    fun uncheckedFirst(word: Word): Set<Terminal>? = when (val symbol = word.firstOrNull()) {
        // epsilon -> undefined
        null -> null

        is Terminal -> setOf(symbol)

        is Nonterminal -> productions
            .also { visited += symbol }
            .filter { it.nonterminalLeft == symbol && it.right.firstOrNull() !in visited }
            .mapNotNull { uncheckedFirst(it.right) }
            .reduceOrNull(Set<Terminal>::union)
    }


    return uncheckedFirst(word)
}


private fun ContextFreeGrammar.follow(nonterminal: Nonterminal): Set<Terminal>? {
    require(nonterminal in nonterminalAlphabet) {
        "Nonterminal $nonterminal is no element of nonterminal alphabet ${nonterminalAlphabet.setToString()}"
    }

    // avoid infinite recursion
    val visited = mutableSetOf<Nonterminal>()


    fun uncheckedFollow(nonterminal: Nonterminal): Set<Terminal>? = if (nonterminal in visited) null else productions
        .also { visited += nonterminal }
        .filter { nonterminal in it.right }
        .flatMap { production ->
            val all = mutableSetOf<Set<Terminal>>()

            with(production) {
                for (index in 0 until right.lastIndex) {
                    // nonterminal at any index (except last) -> first(everything after index)
                    if (right[index] == nonterminal) first(right.drop(index + 1))?.also(all::add)
                }

                if (right endsWith nonterminal) {
                    // nonterminal at last index -> follow(nonterminal left)
                    uncheckedFollow(nonterminalLeft)?.also(all::add)
                }
            }

            all
        }
        .reduceOrNull(Set<Terminal>::union)


    return uncheckedFollow(nonterminal)
}


val ContextFreeGrammar.followTable by LazyExtensionProperty {
    Function1Table(
        domainX = nonterminalAlphabet,
        function = ::follow,
        name = "Follow",
        yToString = { it?.setToString() ?: "⊥" },
    )
}
