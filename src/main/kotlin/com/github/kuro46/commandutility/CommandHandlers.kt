package com.github.kuro46.commandutility

import java.util.concurrent.ConcurrentHashMap

class CommandHandlers {

    private val handlers = ConcurrentHashMap<Command, CommandHandler>()

    var handlerTree = buildHandlerTree()
        private set

    operator fun get(command: Command): CommandHandler? {
        return handlers[command]
    }

    operator fun set(command: Command, handler: CommandHandler) {
        handlers[command] = handler
        handlerTree = buildHandlerTree()
    }

    private fun buildHandlerTree(): CommandTreeEntry {
        val commands = handlers.keys.toList()
        val treeRoot = MutableCommandTreeEntry(null, null)

        for (command in commands) {
            var currentTree = treeRoot
            for (commandElement in command) {
                if (!currentTree.children.containsKey(commandElement)) {
                    currentTree.children[commandElement] = MutableCommandTreeEntry(null, currentTree)
                }

                currentTree = currentTree.children.getValue(commandElement)
            }
            currentTree.command = command
        }

        return treeRoot.toImmutable()
    }
}

private class MutableCommandTreeEntry(
    var command: Command?,
    val parent: MutableCommandTreeEntry?
) {

    val children = HashMap<String, MutableCommandTreeEntry>()

    fun toImmutable(): CommandTreeEntry {
        val map = HashMap<String, CommandTreeEntry>()

        for ((key, value) in children) {
            map[key] = value.toImmutable()
        }

        return CommandTreeEntry(
            command,
            parent?.toImmutable(),
            map
        )
    }
}

class CommandTreeEntry(
    val command: Command?,
    val parent: CommandTreeEntry?,
    val children: Map<String, CommandTreeEntry>
) {

    fun findTree(strings: List<String>): CommandTreeEntry {
        var currentTree = this

        for (string in strings) {
            currentTree = currentTree.children[string] ?: break
        }

        return currentTree
    }
}
