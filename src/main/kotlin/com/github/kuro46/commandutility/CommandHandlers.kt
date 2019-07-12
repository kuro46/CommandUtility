package com.github.kuro46.commandutility

import java.util.concurrent.ConcurrentHashMap

class CommandHandlers {

    private val handlers = ConcurrentHashMap<Command, CommandHandler>()
    private val handlerTreeCache = ValueCache<CommandTreeEntry>()

    operator fun get(command: Command): CommandHandler? {
        return handlers[command]
    }

    operator fun set(command: Command, handler: CommandHandler) {
        handlers[command] = handler
        handlerTreeCache.clear()
    }

    fun getHandlerTree(): CommandTreeEntry {
        return handlerTreeCache.getOrSet { buildHandlerTree() }
    }

    private fun buildHandlerTree(): CommandTreeEntry {
        val commands = handlers.keys.toList()
        val treeRoot = CommandTreeEntry(null, null)

        for (command in commands) {
            var currentTree = treeRoot
            for (commandElement in command) {
                if (!currentTree.children.containsKey(commandElement)) {
                    currentTree.children[commandElement] = CommandTreeEntry(null, currentTree)
                }

                currentTree = currentTree.children.getValue(commandElement)
            }
            currentTree.command = command
        }

        return treeRoot
    }
}

// TODO: make immutable
class CommandTreeEntry(
    var command: Command?,
    val parent: CommandTreeEntry?
) {

    val children = HashMap<String, CommandTreeEntry>()
}
