package lib


private fun ContextFreeGrammar.closure(seed: Set<ContextFreeProduction>): CanonicalCollectionElement {
    require(seed.all { it.isLR0Item }) { "closure only takes LR(0) items" }

    return transitiveClosure(
        seed = seed,
        step = { lr0Item ->
            val nonterminalAfterDot = with(lr0Item.right) { getOrNull(indexOf(Dot) + 1) as? Nonterminal }

            if (nonterminalAfterDot != null) productions
                .filter { it.nonterminalLeft == nonterminalAfterDot }
                .map { it.copyContextFree(right = Dot + it.right) }
            else emptySet()
        },
    )
}


private fun ContextFreeGrammar.goTo(
    fromCanonicalCollectionElement: CanonicalCollectionElement,
    withSymbol: Symbol,
): CanonicalCollectionElement? {
    require(withSymbol in alphabet) { "Symbol $withSymbol is no element of alphabet ${alphabet.setToString()}" }
    require(fromCanonicalCollectionElement.all { it.isLR0Item }) { "goTo only takes LR(0) items" }

    return fromCanonicalCollectionElement
        .mapNotNull { lr0Item ->
            val symbolAfterDot = with(lr0Item.right) { getOrNull(indexOf(Dot) + 1) }

            if (symbolAfterDot == withSymbol) lr0Item.copyContextFree(
                right = with(lr0Item.right) {
                    subWordBefore(Dot) + symbolAfterDot + Dot + subWordAfter(Dot).dropFirst()
                }
            ) else null
        }
        .toSet()
        .takeIf { it.isNotEmpty() }
        ?.let { closure(seed = it) }
}


val ContextFreeGrammar.goToTable by LazyExtensionProperty {
    Function2Table(
        domainX = canonicalCollection,
        domainY = alphabet,
        function = ::goTo,
        name = "GoTo",
        xToString = Set<*>::setToString,
        zToString = { it?.setToString() ?: "⊥" },
    )
}


typealias CanonicalCollection = Set<CanonicalCollectionElement>
typealias CanonicalCollectionElement = Set<ContextFreeProduction>

private fun ContextFreeGrammar.canonicalCollectionAndNewStartSymbol(): Pair<CanonicalCollection, Nonterminal> {

    val newStartSymbol = generateSequence("$startSymbol'") { "$it'" }
        .first { startSymbolPrime -> nonterminalAlphabet.none { it symEq startSymbolPrime } }
        .asNonterminal()

    val sPrimeToDotS = ContextFreeProduction(newStartSymbol, right = Dot + startSymbol)

    return Pair(
        first = transitiveClosure(
            seed = setOf(closure(seed = setOf(sPrimeToDotS))),
            step = { i ->
                alphabet.mapNotNull { x ->
                    goTo(fromCanonicalCollectionElement = i, withSymbol = x)
                }
            },
        ),
        second = newStartSymbol,
    )
}


private val ContextFreeGrammar.canonicalCollectionAndNewStartSymbol
        by LazyExtensionProperty { canonicalCollectionAndNewStartSymbol() }

val ContextFreeGrammar.canonicalCollection get() = canonicalCollectionAndNewStartSymbol.first

val ContextFreeGrammar.extendedStartSymbol get() = canonicalCollectionAndNewStartSymbol.second
