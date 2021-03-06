package lib


open class UnrestrictedProduction(val left: Word, val right: Word) {

    fun copyUnrestricted(left: Word = this.left, right: Word = this.right) = UnrestrictedProduction(left, right)

    final override fun equals(other: Any?) =
        this === other || other is UnrestrictedProduction && this.left == other.left && this.right == other.right

    final override fun hashCode() = 31 * left.hashCode() + right.hashCode()

    final override fun toString() = "${left.wordToString()} -> ${right.wordToString()}"
}


open class ContextSensitiveProduction : UnrestrictedProduction {

    constructor(left: Word, right: Word) : this(left, right, skipChecks = false)

    protected constructor(left: Word, right: Word, skipChecks: Boolean) : super(left, right) {
        if (skipChecks) return

        require(left.any { it is Nonterminal }) {
            "Left-hand side of context-sensitive production $this has no nonterminal symbol"
        } // -> left.size >= 1

        if (right.isEpsilon) {
            require(left.size == 1) { // only allowed for startSymbol -> left size 1
                "Left-hand side of context-sensitive production $this with epsilon as right-hand side has more than one symbol"
            }
        } else {
            require(left.size <= right.size) {
                "Right-hand side of context-sensitive production $this is shorter than left-hand side"
            }

            val sizeDiff = right.size - left.size // always >= 0
            val indicesOfNonterminals =
                left.mapIndexedNotNull { index, symbol -> index.takeIf { symbol is Nonterminal } }

            require(indicesOfNonterminals.any { index ->
                val samePrefix = left.subList(0, index) == right.subList(0, index)
                val samePostfix = left.subList(index + 1, left.size) == right.subList(index + 1 + sizeDiff, right.size)
                samePrefix && samePostfix
            }) { "Context-sensitive production $this does not have form uAv -> uβv" }
        }
    }

    fun copyContextSensitive(left: Word = this.left, right: Word = this.right) = ContextSensitiveProduction(left, right)
}


open class ContextFreeProduction(
    nonterminalLeft: Nonterminal,
    right: Word,                        // skip checks, constraints guaranteed by types and parameters
) : ContextSensitiveProduction(wordOf(nonterminalLeft), right, skipChecks = true) {

    constructor(
        nonterminalLeft: Nonterminal,
        vararg rightSymbols: Symbol,
    ) : this(nonterminalLeft, wordOf(*rightSymbols))

    val nonterminalLeft get() = left.single() as Nonterminal

    val singleTerminalRightOrNull get() = right.singleOrNull() as? Terminal

    val nonterminalPairRightOrNull
        get() = if (right.size == 2) {
            val (first, second) = right
            if (first is Nonterminal && second is Nonterminal) Pair(first, second) else null
        } else null

    val isInChomskyNormalForm
        get() = when (right.size) {
            1 -> right.single() is Terminal
            2 -> right.all { it is Nonterminal }
            else -> false
        }

    val isLR0Item get() = right.singleOrNull { it == Dot } != null

    fun copyContextFree(nonterminalLeft: Nonterminal = this.nonterminalLeft, right: Word = this.right) =
        ContextFreeProduction(nonterminalLeft, right)
}


class RegularProduction : ContextFreeProduction {

    constructor(nonterminalLeft: Nonterminal) : super(nonterminalLeft)

    constructor(nonterminalLeft: Nonterminal, terminalRight: Terminal) : super(nonterminalLeft, terminalRight)

    constructor(
        nonterminalLeft: Nonterminal,
        terminalRight: Terminal,
        nonterminalRight: Nonterminal,
    ) : super(nonterminalLeft, terminalRight, nonterminalRight)

    val terminalRightOrNull get() = right.firstOrNull() as Terminal?
    val nonterminalRightOrNull get() = right.getOrNull(1) as Nonterminal?

    fun copyRegular(
        nonterminalLeft: Nonterminal = this.nonterminalLeft,
        terminalRight: Terminal? = this.terminalRightOrNull,
        nonterminalRight: Nonterminal? = this.nonterminalRightOrNull,
    ) = when {
        nonterminalRight != null -> RegularProduction(
            nonterminalLeft,
            requireNotNull(terminalRight) {
                "A nonterminal on the right-hand side of a regular production cannot exist without a terminal"
            },
            nonterminalRight,
        )
        terminalRight != null -> RegularProduction(nonterminalLeft, terminalRight)
        else -> RegularProduction(nonterminalLeft)
    }
}
