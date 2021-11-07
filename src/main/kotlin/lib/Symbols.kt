package lib


typealias Symbol = Any


data class Terminal(val identifier: String) : Symbol() {
    override fun toString() = identifier
}

val Symbol.isTerminal get() = this is Terminal


typealias Nonterminal = Symbol

val Symbol.isNonterminal get() = this !is Terminal


object Dot : Symbol() {
    override fun toString() = "."
}


typealias Alphabet = Set<Symbol>
typealias TerminalAlphabet = Set<Terminal>
typealias NonterminalAlphabet = Alphabet

fun <S : Symbol> alphabetOf(vararg symbols: S) = setOf(*symbols)
fun <S : Symbol> emptyAlphabet() = emptySet<S>()
fun TerminalAlphabet.word(vararg symbols: String) =
    symbols.map { symbol -> this.first { it.identifier == symbol } }


typealias Word = List<Symbol>
typealias TerminalWord = List<Terminal>
typealias NonterminalWord = Word

fun <S : Symbol> wordOf(vararg symbols: S) = listOf(*symbols)
fun <S : Symbol> emptyWord() = emptyList<S>()
val Word.isEpsilon get() = isEmpty()
fun Word.wordToString() = if (isEpsilon) "ε" else joinToString(separator = "")
