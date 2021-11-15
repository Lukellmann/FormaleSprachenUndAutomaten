package lib


fun ContextFreeGrammar.first(word: Word): Set<Terminal>? {
    require(alphabet.containsAll(word)) {
        "Not all symbols of word ${word.wordToString()} are elements of alphabet ${alphabet.setToString()}"
    }
    require(productions.any { it.nonterminalLeft == word.singleOrNull() || it.right containsSubWord word }) {
        "first only takes nonterminal left-hand sides or sub-words of right-hand sides of context-free productions"
    }

    return when (val symbol = word.firstOrNull()) {
        // epsilon -> undefined
        null -> null

        is Terminal -> setOf(symbol)

        is Nonterminal -> productions
            .filter { it.nonterminalLeft == symbol && !(it.right startsWith symbol) }
            .mapNotNull { first(it.right) }
            .reduceOrNull(Set<Terminal>::union)
    }
}
