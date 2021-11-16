package lib

import lib.Action.*
import lib.SLRParserResult.Failure.*
import lib.SLRParserResult.Success


sealed interface SLRParserResult {
    class Success(val reversedSteps: List<ContextFreeProduction>) : SLRParserResult
    enum class Failure : SLRParserResult { WordCannotBeDerived, ActionNotDefined, ActionConflict }
}


fun ContextFreeGrammar.slrParse(word: TerminalWord): SLRParserResult {
    require(terminalAlphabet.containsAll(word)) {
        "Not all symbols of word ${word.wordToString()} are elements of terminal alphabet ${terminalAlphabet.setToString()}"
    }

    val sPrimeToDotS = ContextFreeProduction(extendedStartSymbol, right = Dot + startSymbol)

    val stack = Stack<CanonicalCollectionElement>()
    stack.push(canonicalCollection.single { sPrimeToDotS in it })

    val wordPlusEOF = word + EOF
    var index = 0

    val reversedSteps = mutableListOf<ContextFreeProduction>()

    while (true) {
        val currentCanonicalCollectionElement = stack.top ?: error("SLR-Parser: stack emtpy")
        val currentSymbol = wordPlusEOF[index]

        when (val action = actionTable[currentCanonicalCollectionElement, currentSymbol]) {
            is Accept -> {
                reversedSteps += ContextFreeProduction(extendedStartSymbol, startSymbol)
                return Success(reversedSteps)
            }
            is Shift -> {
                stack.push(action.canonicalCollectionElement)
                index++
            }
            is Reduce -> {
                repeat(action.production.right.size) { stack.pop() }
                val stackTop = stack.top ?: error("SLR-Parser: stack emtpy")
                stack.push(goToTable[stackTop, action.production.nonterminalLeft] ?: return WordCannotBeDerived)
                reversedSteps += action.production
            }
            is Conflict -> return ActionConflict
            null -> return ActionNotDefined
        }
    }
}


val ContextFreeGrammar.isSLRParsable by LazyExtensionProperty {
    (canonicalCollection cross terminalAlphabet).none { (cce, t) -> actionTable[cce, t] is Conflict }
}
