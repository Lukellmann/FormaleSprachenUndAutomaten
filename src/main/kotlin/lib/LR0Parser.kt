package lib

import lib.LR0ParserResult.Failure.*
import lib.LR0ParserResult.Success


sealed interface LR0ParserResult {
    class Success(val reversedSteps: List<ContextFreeProduction>) : LR0ParserResult
    enum class Failure : LR0ParserResult { WordCannotBeDerived, ShiftReduceConflict, ReduceReduceConflict }
}


fun ContextFreeGrammar.lr0Parse(word: TerminalWord): LR0ParserResult {
    require(terminalAlphabet.containsAll(word)) {
        "Not all symbols of word ${word.wordToString()} are elements of terminal alphabet ${terminalAlphabet.setToString()}"
    }

    val sPrimeToDotS = ContextFreeProduction(extendedStartSymbol, right = Dot + startSymbol)
    val sPrimeToSDot = ContextFreeProduction(extendedStartSymbol, right = startSymbol + Dot)

    val stack = Stack<Set<ContextFreeProduction>>()
    stack.push(canonicalCollection.single { sPrimeToDotS in it })

    val wordPlusEOF = word + EOF
    var index = 0

    val reversedSteps = mutableListOf<ContextFreeProduction>()

    while (true) {
        val currentProductions = stack.top ?: error("LR(0)-Parser: stack emtpy")
        val currentSymbol = wordPlusEOF[index]

        with(currentProductions) {
            when {
                // earlier iteration reduced to {S' -> S.}
                equals(setOf(sPrimeToSDot)) -> {
                    reversedSteps += ContextFreeProduction(extendedStartSymbol, startSymbol)
                    return Success(reversedSteps)
                }

                // shift
                none { it.right endsWith Dot } -> {
                    stack.push(goToTable[currentProductions, currentSymbol] ?: return WordCannotBeDerived)
                    index++
                }

                // shift-reduce-conflict
                !all { it.right endsWith Dot } -> return ShiftReduceConflict

                // reduce-reduce-conflict
                size != 1 -> return ReduceReduceConflict

                // reduce
                else -> {
                    val currentProduction = single()
                    val beta = currentProduction.right.dropLast(1) // drop Dot

                    repeat(beta.size) { stack.pop() }
                    val stackTop = stack.top ?: error("LR(0)-Parser: stack emtpy")
                    stack.push(goToTable[stackTop, currentProduction.nonterminalLeft] ?: return WordCannotBeDerived)
                    reversedSteps += productions.single { it.left == currentProduction.left && it.right == beta }
                }
            }
        }
    }
}


private class Stack<T> {
    private val stack = ArrayDeque<T>()
    val top get() = stack.firstOrNull()
    fun push(element: T) = stack.addFirst(element)
    fun pop() = stack.removeFirstOrNull()
}
