package lib


private fun ContextFreeGrammar.closure(seed: Set<ContextFreeProduction>) = transitiveClosure(
    seed = seed.apply { require(all { it.isLR0Item }) { "closure only takes LR(0) items" } },
    step = { production ->
        val nonterminalAfterDot = with(production.right) { getOrNull(indexOf(Dot) + 1) as? Nonterminal }

        if (nonterminalAfterDot != null) productions
            .filter { it.nonterminalLeft == nonterminalAfterDot }
            .map { ContextFreeProduction(it.nonterminalLeft, right = Dot + it.right) }
        else emptySet()
    },
)


private fun ContextFreeGrammar.goTo(start: Set<ContextFreeProduction>, symbol: Symbol) =
    start.apply { require(all { it.isLR0Item }) { "goTo only takes LR(0) items" } }
        .mapNotNull { production ->
            val right = production.right
            val indexOfDot = right.indexOf(Dot)
            val symbolAfterDot = right.getOrNull(indexOfDot + 1)

            if (symbolAfterDot == symbol) ContextFreeProduction(
                nonterminalLeft = production.nonterminalLeft,
                right = with(right) { subWord(0, indexOfDot) + symbolAfterDot + Dot + subWord(indexOfDot + 2, size) },
            ) else null
        }
        .toSet()
        .takeIf { it.isNotEmpty() }
        ?.let { closure(it) }


val ContextFreeGrammar.goToTable by LazyExtensionProperty {
    FunctionTable(
        domainX = canonicalCollection,
        domainY = (nonterminalAlphabet union terminalAlphabet),
        function = ::goTo,
        name = "GoTo",
        xToString = Set<*>::setToString,
        zToString = { it?.setToString() ?: "⊥" },
    )
}


typealias CanonicalCollection = Set<Set<ContextFreeProduction>>

private fun ContextFreeGrammar.canonicalCollectionAndNewStartSymbol(): Pair<CanonicalCollection, Nonterminal> {

    val allSymbols = nonterminalAlphabet union terminalAlphabet

    val newStartSymbol = generateSequence("$startSymbol'") { "$it'" }
        .first { startSymbolPrime -> nonterminalAlphabet.none { it symEq startSymbolPrime } }
        .asNonterminal()

    val sPrimeToDotS = ContextFreeProduction(newStartSymbol, right = Dot + startSymbol)

    return Pair(
        first = transitiveClosure(
            seed = setOf(closure(setOf(sPrimeToDotS))),
            step = { i -> allSymbols.mapNotNull { x -> goTo(i, x) } },
        ),
        second = newStartSymbol,
    )
}


private val ContextFreeGrammar.canonicalCollectionAndNewStartSymbol
        by LazyExtensionProperty { canonicalCollectionAndNewStartSymbol() }

val ContextFreeGrammar.canonicalCollection get() = canonicalCollectionAndNewStartSymbol.first

val ContextFreeGrammar.extendedStartSymbol get() = canonicalCollectionAndNewStartSymbol.second
