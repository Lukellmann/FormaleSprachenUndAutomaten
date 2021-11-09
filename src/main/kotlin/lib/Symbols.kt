package lib


sealed interface Symbol {
    infix fun symEq(other: Any?): Boolean
}

infix fun Any?.symEq(symbol: Symbol) = symbol symEq this


sealed interface Terminal : Symbol


private data class TerminalWrapper(private val wrapped: String) : Terminal {
    override fun toString() = wrapped
    override fun symEq(other: Any?) = (other is TerminalWrapper && this.wrapped == other.wrapped) || wrapped == other
}

fun String.asTerminal(): Terminal {
    require(isNotEmpty()) { "Terminal can not be empty" }
    return TerminalWrapper(this)
}


sealed interface Nonterminal : Symbol


private data class NonterminalWrapper(private val wrapped: Any?) : Nonterminal {
    override fun toString() = wrapped.toString()
    override fun symEq(other: Any?) = (other is NonterminalWrapper && this.wrapped == other.wrapped) || wrapped == other
}

fun Any?.asNonterminal() = when (this) {
    is Nonterminal -> this
    is Terminal -> throw IllegalArgumentException("Terminal can not be converted to nonterminal")
    else -> {
        if (this is CharSequence) require(isNotEmpty()) { "Nonterminal can not be empty" }
        NonterminalWrapper(this)
    }
}


object Dot : Nonterminal {
    override fun toString() = "."
    override fun symEq(other: Any?) = this === other
}


typealias Alphabet = Set<Symbol>
typealias TerminalAlphabet = Set<Terminal>
typealias NonterminalAlphabet = Set<Nonterminal>

fun <S : Symbol> alphabetOf(vararg symbols: S) = setOf(*symbols)
fun <S : Symbol> emptyAlphabet() = emptySet<S>()
fun TerminalAlphabet.word(vararg symbols: String): TerminalWord = symbols.map { symbol ->
    firstOrNull { it symEq symbol }
        ?: throw IllegalArgumentException("Terminal alphabet ${
            this.setToString()
        } does not contain all symbols ${
            symbols.toSet().setToString()
        }")
}


typealias Word = List<Symbol>
typealias TerminalWord = List<Terminal>
typealias NonterminalWord = List<Nonterminal>

fun <S : Symbol> wordOf(vararg symbols: S) = listOf(*symbols)
fun <S : Symbol> emptyWord() = emptyList<S>()
val Word.isEpsilon get() = isEmpty()
fun Word.wordToString() = if (isEpsilon) "ε" else joinToString(separator = "")
fun <S : Symbol> List<S>.subWord(fromIndex: Int, toIndex: Int) = subList(fromIndex, toIndex)
operator fun <S : Symbol> S.plus(symbol: S) = wordOf(this, symbol)
operator fun <S : Symbol> S.plus(word: List<S>) = wordOf(this) + word
