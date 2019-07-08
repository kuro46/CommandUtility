package com.github.kuro46.commandutility

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import org.bukkit.Bukkit
import org.bukkit.command.Command as BukkitCommand
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.plugin.Plugin

/**
 * A class for manages CommandHandler.
 *
 * Thread-Safe
 */
abstract class CommandHandlerManager(val plugin: Plugin, val executor: Executor) {

    private val handlers = ConcurrentHashMap<Command, CommandHandler>()
    private val commandExecutor = CommandExecutorImpl()
    private val tabCompleter = TabCompleterImpl()

    fun registerHandler(command: String, handler: CommandHandler) {
        @Suppress("NAME_SHADOWING")
        val command = Command.fromString(command)
        val name = command[0]

        val needRegisterCommand = handlers.filterKeys { it[0] == name }.isEmpty()

        handlers[command] = handler

        if (needRegisterCommand) {
            val bukkitCommand = Bukkit.getPluginCommand(name)
            bukkitCommand.executor = commandExecutor
            bukkitCommand.tabCompleter = tabCompleter
            registerHandler(name, newRootCommandHandler(command))
        }
    }

    abstract fun newRootCommandHandler(command: Command): CommandHandler

    private fun buildCommandTree(): CommandTreeEntry {
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

    private fun findRegisteredCommand(
        commandWithArgs: CommandWithArgs
    ): Command {
        var currentTree = buildCommandTree()
        var lastNonNullCommand: Command? = null
        for (element in commandWithArgs) {
            currentTree = currentTree.children[element] ?: break
            currentTree.command?.let { lastNonNullCommand = it }
        }

        return lastNonNullCommand!!
    }

    private fun executeCommandAsync(
        sender: CommandSender,
        command: BukkitCommand,
        args: Array<String>
    ) {
        val commandWithArgs = CommandWithArgs.fromCommandAndArgs(
            command.name,
            args
        )
        val foundCommand = findRegisteredCommand(commandWithArgs)

        val handler = handlers.getValue(foundCommand)
        @Suppress("NAME_SHADOWING")
        val args = foundCommand.getArgsFromList(commandWithArgs)
        val parsedArgs = handler.commandSyntax.parseArguments(args)

        handler.executionThread.executeAtSyncOrCurrentThread(plugin) {
            handler.handleCommand(
                this,
                sender,
                foundCommand,
                parsedArgs
            )
        }
    }

    fun executeTabCompletion(
        sender: CommandSender,
        bukkitCommand: BukkitCommand,
        args: Array<String>
    ): List<String> {
        val argsWithoutSpace = args.filter { !it.contains(' ') }
        val commandWithArgs = CommandWithArgs.fromCommandAndArgs(
            bukkitCommand.name,
            argsWithoutSpace.toTypedArray()
        )
        val command = findRegisteredCommand(commandWithArgs)

        @Suppress("NAME_SHADOWING")
        val args = command.getArgsFromList(argsWithoutSpace)

        val handler = handlers.getValue(command)

        val completedArgs = if (args.isNotEmpty()) {
            args.dropLast(1)
        } else { emptyList() }

        val uncompletedArg = if (args.isNotEmpty()) {
            args.last()
        } else { "" }

        return handler.handleTabComplete(
            this,
            sender,
            command,
            completedArgs,
            uncompletedArg
        )
    }

    private inner class TabCompleterImpl : TabCompleter {

        override fun onTabComplete(
            sender: CommandSender,
            bukkitCommand: BukkitCommand,
            alias: String,
            args: Array<String>
        ): List<String> {
            return executeTabCompletion(sender, bukkitCommand, args)
        }
    }

    private inner class CommandExecutorImpl : CommandExecutor {
        override fun onCommand(
            sender: CommandSender,
            command: BukkitCommand,
            label: String,
            args: Array<String>
        ): Boolean {
            executor.execute {
                executeCommandAsync(
                    sender,
                    command,
                    args
                )
            }

            return true
        }
    }
}

private class CommandWithArgs private constructor(list: List<String>) :
    List<String> by list {

    companion object {

        fun fromCommandAndArgs(command: String, args: Array<String>): CommandWithArgs {
            return CommandWithArgs(
                ArrayList<String>().apply {
                    add(command.toLowerCase())
                    for (e in args) {
                        add(e.toLowerCase())
                    }
                }
            )
        }
    }
}

private class CommandTreeEntry(
    var command: Command?,
    val parent: CommandTreeEntry?
) {

    val children = HashMap<String, CommandTreeEntry>()
}
