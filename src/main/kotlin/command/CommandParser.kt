package de.olivermakesco.betacord.command

import dev.proxyfox.command.CommandContext
import dev.proxyfox.command.NodeHolder
import dev.proxyfox.command.node.CommandNode
import dev.proxyfox.command.node.builtin.LiteralNode
import kotlin.math.min

class CommandParser<T, C : CommandContext<T>> : NodeHolder<T, C>() {
    suspend fun parse(ctx: C): Boolean? {
        val literals = ArrayList<LiteralNode<T, C>>()
        for (node in nodes) {
            if (node is LiteralNode) literals.add(node)
            return tryParseNode(ctx.command, node, ctx) ?: continue
        }
        val test = ctx.command.split(" ")[0]
        val closest = getLevenshtein(test, literals) ?: return null
        if (closest.second == 0) return null
        ctx.respondFailure("Command `$test` not found. Did you mean `${closest.first}`? Closeness: ${closest.second}", true)
        return false
    }

    private suspend fun tryParseNode(input: String, node: CommandNode<T, C>, ctx: C): Boolean? {
        // Try parsing the node
        val idx = node.parse(input, ctx)
        // Return if parsing failed or there's no string left to consume
        if (idx == -1) return null
        if (idx >= input.length) return node.execute(ctx)
        // Iterate through sub nodes and try parsing them
        val substr = input.substring(idx).trim()
        val literals = ArrayList<LiteralNode<T, C>>()
        for (subNode in node.nodes) {
            if (subNode is LiteralNode) literals.add(subNode)
            return tryParseNode(substr, subNode, ctx) ?: continue
        }
        val test = substr.split(" ")[0]
        val closest = getLevenshtein(test, literals) ?: return node.execute(ctx)
        if (closest.second == 0) return node.execute(ctx)
        ctx.respondFailure("Command `$test` not found. Did you mean `${closest.first}`?", true)
        return false
    }

    private fun getLevenshtein(test: String, literals: ArrayList<LiteralNode<T, C>>): Pair<String, Int>? {
        var closest: Pair<String, Int>? = null
        for (literal in literals) {
            for (str in literal.literals) {
                val dist = test.levenshtein(str)
                if (closest == null) {
                    closest = Pair(str, dist)
                } else if (closest.second > dist) {
                    closest = Pair(str, dist)
                }
            }
        }
        return closest
    }
}

fun String.levenshtein(other: String): Int {
    if(this == other) { return 0 }
    if(isEmpty()) { return other.length }
    if(other.isEmpty()) { return length }

    val lhsLength = length + 1
    val rhsLength = other.length + 1

    var cost = Array(lhsLength) { it }
    var newCost = Array(lhsLength) { 0 }

    for (i in 1 until rhsLength) {
        newCost[0] = i

        for (j in 1 until lhsLength) {
            val match = if(this[j - 1] == other[i - 1]) 0 else 1

            val costReplace = cost[j - 1] + match
            val costInsert = cost[j] + 1
            val costDelete = newCost[j - 1] + 1

            newCost[j] = min(min(costInsert, costDelete), costReplace)
        }

        val swap = cost
        cost = newCost
        newCost = swap
    }

    return cost[lhsLength - 1]
}