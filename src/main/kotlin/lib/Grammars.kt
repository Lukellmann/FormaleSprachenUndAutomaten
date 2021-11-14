package lib


enum class ChomskyHierarchyType(private val value: Int) {
    Type0(0), Type1(1), Type2(2), Type3(3);

    fun toInt() = value
}


data class Grammar<out P : UnrestrictedProduction>(
    val nonterminalAlphabet: NonterminalAlphabet,
    val terminalAlphabet: TerminalAlphabet,
    val productions: Set<P>,
    val startSymbol: Nonterminal,
) {
    val isRegular = productions.all { it is RegularProduction }
    val isContextFree = isRegular || productions.all { it is ContextFreeProduction }
    val isContextSensitive = isContextFree || productions.all { it is ContextSensitiveProduction }
    val isUnrestricted get() = true

    init {
        require(Dot !in nonterminalAlphabet) { "Dot is not allowed in nonterminal alphabet" }
        require(EOF !in terminalAlphabet) { "EOF is not allowed in terminal alphabet" }

        require(startSymbol in nonterminalAlphabet) {
            "\nStart symbol $startSymbol is no element of the nonterminal alphabet ${nonterminalAlphabet.setToString()} of grammar\n$this"
        }

        val symbols = nonterminalAlphabet union terminalAlphabet
        require(productions.all { symbols.containsAll(it.left) && symbols.containsAll(it.right) }) {
            "\nA production of grammar\n$this\nuses a symbol that is no element of the terminal or nonterminal alphabet"
        }

        if (isContextSensitive) { // -> epsilon only allowed as right from startSymbol
            val startSymbolWord = wordOf(startSymbol)
            val (start, other) = productions.partition { it.left == startSymbolWord }

            require(other.none { it.right.isEpsilon }) {
                "\nA production of context-sensitive grammar \n$this\nthat does not have the start symbol as its lef-hand side has epsilon as its right-hand side"
            }

            if (start.any { it.right.isEpsilon }) require(productions.none { startSymbol in it.right }) {
                "\nA production of context-sensitive grammar \n$this\nhas the start symbol on its right-hand side but another production with the start symbol as its left-hand side has epsilon as its right-hand side"
            }
        }
    }

    val chomskyHierarchyType
        get() = when {
            isRegular -> ChomskyHierarchyType.Type3
            isContextFree -> ChomskyHierarchyType.Type2
            isContextSensitive -> ChomskyHierarchyType.Type1
            else -> ChomskyHierarchyType.Type0
        }

    override fun toString() =
        "(\n  ${
            nonterminalAlphabet.setToString()
        },\n  ${
            terminalAlphabet.setToString()
        },\n  ${
            productions.joinToString(separator = ",\n    ", prefix = "{\n    ", postfix = "\n  }")
        },\n  $startSymbol\n)"
}


typealias UnrestrictedGrammar = Grammar<UnrestrictedProduction>
typealias ContextSensitiveGrammar = Grammar<ContextSensitiveProduction>
typealias ContextFreeGrammar = Grammar<ContextFreeProduction>
typealias RegularGrammar = Grammar<RegularProduction>

val ContextFreeGrammar.isInChomskyNormalForm by LazyExtensionProperty { productions.all { it.isInChomskyNormalForm } }
