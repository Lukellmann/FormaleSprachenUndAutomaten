package lib

fun Set<*>.setToString() = joinToString(separator = ", ", prefix = "{", postfix = "}")


fun Set<Set<ContextFreeProduction>>.canonicalCollectionToString() =
    joinToString(separator = ",\n  ", prefix = "{\n  ", postfix = "\n}") { each ->
        each.joinToString(separator = ",\n    ", prefix = "{\n    ", postfix = "\n  }")
    }

fun Set<Set<ContextFreeProduction>>.canonicalCollectionToCompactString() =
    joinToString(separator = ",\n  ", prefix = "{\n  ", postfix = "\n}") { it.setToString() }


fun <T> transitiveClosure(seed: Set<T>, step: (T) -> Iterable<T>): Set<T> {
    if (seed.isEmpty()) return seed

    val result = seed.toMutableSet()
    var newElements = seed

    while (true) {
        newElements = newElements.flatMap { step(it) }.toSet() - result
        if (newElements.isEmpty()) break
        result.addAll(newElements)
    }

    return result
}
