package lib

fun Set<*>.setToString() = joinToString(separator = ", ", prefix = "{", postfix = "}")


fun Set<Set<ContextFreeProduction>>.canonicalCollectionToString() =
    joinToString(separator = ",\n  ", prefix = "{\n  ", postfix = "\n}") { each ->
        each.joinToString(separator = ",\n    ", prefix = "{\n    ", postfix = "\n  }")
    }

fun Set<Set<ContextFreeProduction>>.canonicalCollectionToCompactString() =
    joinToString(separator = ",\n  ", prefix = "{\n  ", postfix = "\n}") { it.setToString() }


fun <T> transitiveClosure(start: Set<T>, step: (T) -> Iterable<T>): Set<T> {
    if (start.isEmpty()) return start

    val result = start.toMutableSet()
    var new = start

    while (true) {
        new = new.flatMap { step(it) }.toSet() - result
        if (new.isEmpty()) break
        result.addAll(new)
    }

    return result
}
