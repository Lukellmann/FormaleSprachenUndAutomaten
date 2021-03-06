package lib

import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


fun Set<*>.setToString() = joinToString(separator = ", ", prefix = "{", postfix = "}")


fun CanonicalCollection.canonicalCollectionToString() =
    joinToString(separator = ",\n  ", prefix = "{\n  ", postfix = "\n}") { each ->
        each.joinToString(separator = ",\n    ", prefix = "{\n    ", postfix = "\n  }")
    }

fun CanonicalCollection.canonicalCollectionToCompactString() =
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


class Stack<T> {
    private val stack = ArrayDeque<T>()
    val top get() = stack.firstOrNull()
    fun push(element: T) = stack.addFirst(element)
    fun pop() = stack.removeFirstOrNull()
}


class LazyExtensionProperty<in T, out V : Any>(private val initializer: T.() -> V) : ReadOnlyProperty<T, V> {
    private val propertyValues = ConcurrentHashMap<T, V>()
    override operator fun getValue(thisRef: T, property: KProperty<*>) =
        propertyValues.computeIfAbsent(thisRef, initializer)
}


infix fun <A, B> Set<A>.cross(other: Set<B>) = flatMap { a -> other.map { b -> Pair(a, b) } }.toSet()


class Function1Table<in X, out Y>(
    private val domainX: Set<X>,
    private val xToString: (X) -> String = Any?::toString,
    private val yToString: (Y) -> String = Any?::toString,
    private val name: String = "",
    function: (X) -> Y,
) {
    private val table = domainX.associateWith { x -> function(x) }

    operator fun get(x: X): Y {
        require(x in domainX)
        @Suppress("UNCHECKED_CAST")
        return table[x] as Y
    }

    override fun toString(): String {
        val xs = domainX.toList()
        if (xs.isEmpty()) return "$name: table empty"

        var xStrings = xs.map(xToString)
        val maxX = max(name.length, xStrings.maxOf { it.length }).coerceAtLeast(1)
        xStrings = xStrings.map { it.padEnd(maxX) }

        val yStrings = table
            .toList()
            .sortedBy { (x) -> xs.indexOf(x) }
            .map { (_, y) -> yToString(y) }
        val maxY = yStrings.maxOf { it.length }.coerceAtLeast(1)

        check(xStrings.size == yStrings.size)

        return buildString {
            fun appendTableLineSeparator() {
                appendLine()
                append("-".repeat(maxX)); append("-+-"); append("-".repeat(maxY))
                appendLine()
            }

            append(name.padEnd(maxX)); append(" |")
            appendTableLineSeparator()
            for (index in xs.indices) {
                append(xStrings[index]); append(" | "); append(yStrings[index])
                if (index != xs.lastIndex) appendTableLineSeparator()
            }
        }
    }
}


class Function2Table<in X, in Y, out Z>(
    private val domainX: Set<X>,
    private val domainY: Set<Y>,
    private val xToString: (X) -> String = Any?::toString,
    private val yToString: (Y) -> String = Any?::toString,
    private val zToString: (Z) -> String = Any?::toString,
    private val name: String = "",
    function: (X, Y) -> Z,
) {
    private val table = (domainX cross domainY).associateWith { (x, y) -> function(x, y) }

    operator fun get(x: X, y: Y): Z {
        require(x in domainX && y in domainY)
        @Suppress("UNCHECKED_CAST")
        return table[Pair(x, y)] as Z
    }

    override fun toString(): String {
        val xs = domainX.toList()
        val ys = domainY.toList()
        if (xs.isEmpty() || ys.isEmpty()) return "$name: table empty"

        var xStrings = xs.map(xToString)
        val maxX = max(name.length, xStrings.maxOf { it.length }).coerceAtLeast(1)
        xStrings = xStrings.map { it.padEnd(maxX) }

        var yStrings = ys.map(yToString)
        var zStrings = table
            .mapKeys { (xy) ->
                val (x, y) = xy
                Pair(xs.indexOf(x), ys.indexOf(y))
            }
            .mapValues { (_, z) -> zToString(z) }
        val maxYOrZForYIndex = yStrings.mapIndexed { yIndex, yString ->
            max(
                yString.length,
                zStrings
                    .filter { (xy) ->
                        val (_, y) = xy
                        y == yIndex
                    }
                    .maxOf { (_, zString) -> zString.length },
            ).coerceAtLeast(1)
        }
        yStrings = yStrings.mapIndexed { yIndex, yString -> yString.padStart(maxYOrZForYIndex[yIndex]) }
        zStrings = zStrings.mapValues { (xy, zString) ->
            val (_, yIndex) = xy
            zString.padStart(maxYOrZForYIndex[yIndex])
        }

        return buildString {
            fun appendTableLineSeparator() {
                appendLine()
                append("-".repeat(maxX))
                ys.indices.forEach { yIndex -> append("-+-"); append("-".repeat(maxYOrZForYIndex[yIndex])) }
                appendLine()
            }

            append(name.padEnd(maxX)); yStrings.forEach { append(" | "); append(it) }
            appendTableLineSeparator()
            xs.indices.forEach { xIndex ->
                append(xStrings[xIndex])
                ys.indices.forEach { yIndex -> append(" | "); append(zStrings[Pair(xIndex, yIndex)]) }
                if (xIndex != xs.lastIndex) appendTableLineSeparator()
            }
        }
    }
}
