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


fun ContextFreeGrammar.jump(start: Set<ContextFreeProduction>, symbol: Symbol) = closure(
    start.apply { require(all { it.isLR0Item }) { "jump only takes LR(0) items" } }
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
)


data class CanonicalCollectionAndNewStartSymbol(
    val canonicalCollection: Set<Set<ContextFreeProduction>>,
    val newStartSymbol: Nonterminal,
)

fun ContextFreeGrammar.canonicalCollectionAndNewStartSymbol(): CanonicalCollectionAndNewStartSymbol {

    val allSymbols = nonterminalAlphabet + terminalAlphabet

    val newStartSymbol = generateSequence("$startSymbol'") { "$it'" }
        .first { startSymbolPrime -> nonterminalAlphabet.none { it symEq startSymbolPrime } }
        .asNonterminal()

    val sPrimeToDotS = ContextFreeProduction(newStartSymbol, right = Dot + startSymbol)

    return CanonicalCollectionAndNewStartSymbol(
        canonicalCollection = transitiveClosure(
            seed = setOf(closure(setOf(sPrimeToDotS))),
            step = { i -> allSymbols.mapNotNull { x -> jump(i, x).takeIf { it.isNotEmpty() } } },
        ),
        newStartSymbol = newStartSymbol,
    )
}

fun ContextFreeGrammar.canonicalCollection() = canonicalCollectionAndNewStartSymbol().canonicalCollection
