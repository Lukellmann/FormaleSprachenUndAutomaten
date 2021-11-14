package lib

import kotlin.annotation.AnnotationTarget.CLASS


@DslMarker
@Target(CLASS)
annotation class GrammarDsl


@GrammarDsl
sealed class ProductionsBuildScope<P : UnrestrictedProduction> private constructor(
    private val nonterminalAlphabet: NonterminalAlphabet,
    private val terminalAlphabet: TerminalAlphabet,
) {
    protected val productions = mutableSetOf<P>()
    internal fun build(): Set<P> = productions

    protected fun getNonterminal(symbol: String) = requireNotNull(nonterminalAlphabet.firstOrNull { it symEq symbol }) {
        "Nonterminal alphabet ${nonterminalAlphabet.setToString()} does not contain symbol $symbol"
    }

    protected fun getTerminal(symbol: String) = requireNotNull(terminalAlphabet.firstOrNull { it symEq symbol }) {
        "Terminal alphabet ${terminalAlphabet.setToString()} does not contain symbol $symbol"
    }

    protected fun getSymbol(symbol: String): Symbol {
        val nonterminal = nonterminalAlphabet.firstOrNull { it symEq symbol }
        val terminal = terminalAlphabet.firstOrNull { it symEq symbol }
        when {
            nonterminal != null && terminal != null -> throw IllegalArgumentException(
                "Both terminal ${
                    terminalAlphabet.setToString()
                } and nonterminal alphabet ${
                    nonterminalAlphabet.setToString()
                } contain symbol $symbol"
            )
            nonterminal == null && terminal == null -> throw IllegalArgumentException(
                "Both terminal ${
                    terminalAlphabet.setToString()
                } and nonterminal alphabet ${
                    nonterminalAlphabet.setToString()
                } do not contain symbol $symbol"
            )
            else -> return nonterminal ?: terminal!!
        }
    }
}


@GrammarDsl
class UnrestrictedProductionsBuildScope internal constructor(
    nonterminalAlphabet: NonterminalAlphabet,
    terminalAlphabet: TerminalAlphabet,
) : ProductionsBuildScope<UnrestrictedProduction>(nonterminalAlphabet, terminalAlphabet) {

    class Left internal constructor(internal val leftSymbols: Array<out String>)

    fun left(vararg leftSymbols: String) = Left(leftSymbols)

    fun Left.toRight(vararg rightSymbols: String) {
        productions += UnrestrictedProduction(leftSymbols.map(::getSymbol), rightSymbols.map(::getSymbol))
    }
}


@GrammarDsl
class ContextSensitiveProductionsBuildScope internal constructor(
    nonterminalAlphabet: NonterminalAlphabet,
    terminalAlphabet: TerminalAlphabet,
) : ProductionsBuildScope<ContextSensitiveProduction>(nonterminalAlphabet, terminalAlphabet) {

    class Left internal constructor(internal val leftSymbols: Array<out String>)

    fun left(vararg leftSymbols: String) = Left(leftSymbols)

    fun Left.toRight(vararg rightSymbols: String) {
        productions += ContextSensitiveProduction(leftSymbols.map(::getSymbol), rightSymbols.map(::getSymbol))
    }
}


@GrammarDsl
class ContextFreeProductionsBuildScope internal constructor(
    nonterminalAlphabet: NonterminalAlphabet,
    terminalAlphabet: TerminalAlphabet,
) : ProductionsBuildScope<ContextFreeProduction>(nonterminalAlphabet, terminalAlphabet) {

    fun String.toRight(vararg rightSymbols: String) {
        productions += ContextFreeProduction(getNonterminal(this), rightSymbols.map(::getSymbol))
    }
}


@GrammarDsl
class RegularProductionsBuildScope internal constructor(
    nonterminalAlphabet: NonterminalAlphabet,
    terminalAlphabet: TerminalAlphabet,
) : ProductionsBuildScope<RegularProduction>(nonterminalAlphabet, terminalAlphabet) {

    fun String.toRight() {
        productions += RegularProduction(getNonterminal(this))
    }

    fun String.toRight(terminalRight: String) {
        productions += RegularProduction(getNonterminal(this), getTerminal(terminalRight))
    }

    fun String.toRight(terminalRight: String, nonterminalRight: String) {
        productions += RegularProduction(
            getNonterminal(this),
            getTerminal(terminalRight),
            getNonterminal(nonterminalRight),
        )
    }
}


@GrammarDsl
class GrammarBuildScope<P : UnrestrictedProduction, PBS : ProductionsBuildScope<P>> internal constructor(
    private val productionsBuildScopeSupplier: (NonterminalAlphabet, TerminalAlphabet) -> PBS,
) {
    private var nonterminalAlphabet: Array<out String> = emptyArray()
    fun nonterminalAlphabet(vararg symbols: String) {
        nonterminalAlphabet = symbols
    }

    private var terminalAlphabet: Array<out String> = emptyArray()
    fun terminalAlphabet(vararg symbols: String) {
        terminalAlphabet = symbols
    }

    private var productionsBlock: PBS.() -> Unit = {}
    fun productions(block: PBS.() -> Unit) {
        productionsBlock = block
    }

    lateinit var startSymbol: String

    internal fun build(): Grammar<P> {
        require(::startSymbol.isInitialized) { "Missing start symbol" }

        val nonterminals = nonterminalAlphabet.map { it.asNonterminal() }.toSet()
        val terminals = terminalAlphabet.map { it.asTerminal() }.toSet()
        val productions = productionsBuildScopeSupplier(nonterminals, terminals).apply(productionsBlock).build()
        val start = requireNotNull(nonterminals.firstOrNull { it symEq startSymbol }) {
            "Nonterminal alphabet ${nonterminals.setToString()} does not contain start symbol $startSymbol"
        }

        return Grammar(nonterminals, terminals, productions, start)
    }
}


typealias UnrestrictedGrammarBuildScope = GrammarBuildScope<UnrestrictedProduction, UnrestrictedProductionsBuildScope>
typealias ContextSensitiveGrammarBuildScope = GrammarBuildScope<ContextSensitiveProduction, ContextSensitiveProductionsBuildScope>
typealias ContextFreeGrammarBuildScope = GrammarBuildScope<ContextFreeProduction, ContextFreeProductionsBuildScope>
typealias RegularGrammarBuildScope = GrammarBuildScope<RegularProduction, RegularProductionsBuildScope>

fun unrestrictedGrammar(block: UnrestrictedGrammarBuildScope.() -> Unit): UnrestrictedGrammar =
    UnrestrictedGrammarBuildScope(::UnrestrictedProductionsBuildScope).apply(block).build()

fun contextSensitiveGrammar(block: ContextSensitiveGrammarBuildScope.() -> Unit): ContextSensitiveGrammar =
    ContextSensitiveGrammarBuildScope(::ContextSensitiveProductionsBuildScope).apply(block).build()

fun contextFreeGrammar(block: ContextFreeGrammarBuildScope.() -> Unit): ContextFreeGrammar =
    ContextFreeGrammarBuildScope(::ContextFreeProductionsBuildScope).apply(block).build()

fun regularGrammar(block: RegularGrammarBuildScope.() -> Unit): RegularGrammar =
    RegularGrammarBuildScope(::RegularProductionsBuildScope).apply(block).build()
