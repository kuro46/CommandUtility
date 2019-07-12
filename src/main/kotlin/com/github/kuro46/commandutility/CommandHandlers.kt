package com.github.kuro46.commandutility

import java.util.concurrent.ConcurrentHashMap

class CommandHandlers {

    private val handlers = ConcurrentHashMap<Command, CommandHandler>()
    private val handlerTreeCache = ValueCache<ImmutableCommandTreeEntry>()

    operator fun get(command: Command): CommandHandler? {
        return handlers[command]
    }

    operator fun set(command: Command, handler: CommandHandler) {
        handlers[command] = handler
        handlerTreeCache.clear()
    }

    fun getHandlerTree(): ImmutableCommandTreeEntry {
        return handlerTreeCache.getOrSet { buildHandlerTree() }
    }

    private fun buildHandlerTree(): ImmutableCommandTreeEntry {
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

        return treeRoot.toImmutable()
    }
}

class CommandTreeEntry(
    var command: Command?,
    val parent: CommandTreeEntry?
) {

    val children = HashMap<String, CommandTreeEntry>()

    fun toImmutable(): ImmutableCommandTreeEntry {
        val map = HashMap<String, ImmutableCommandTreeEntry>()

        for ((key, value) in children) {
            map[key] = value.toImmutable()
        }

        return ImmutableCommandTreeEntry(
            command,
            parent?.toImmutable(),
            map
        )
    }
}

class ImmutableCommandTreeEntry(
    val command: Command?,
    val parent: ImmutableCommandTreeEntry?,
    val children: Map<String, ImmutableCommandTreeEntry>
)
