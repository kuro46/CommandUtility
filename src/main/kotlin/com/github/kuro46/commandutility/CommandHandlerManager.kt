package com.github.kuro46.commandutility

import arrow.core.Either
import org.bukkit.Bukkit
import org.bukkit.command.Command as BukkitCommand
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/**
 * A class for manages CommandHandler.
 *
 * Thread-Safe
 */
abstract class CommandHandlerManager(val plugin: Plugin) {

    private val handlers = CommandHandlers()
    private val commandExecutor = CommandExecutorImpl()
    private val tabCompleter = TabCompleterImpl()

    abstract val fallbackHandler: FallbackCommandHandler

    val converters = StringConverters()

    fun registerHandler(command: String, handler: CommandHandler) {
        @Suppress("NAME_SHADOWING")
        val command = Command.fromString(command)
        val name = command[0]

        val needRegisterCommand = !handlers.handlerTree.children.containsKey(name)

        handlers[command] = handler

        if (needRegisterCommand) {
            val bukkitCommand = Bukkit.getPluginCommand(name)
            bukkitCommand.executor = commandExecutor
            bukkitCommand.tabCompleter = tabCompleter
            registerHandler(name, fallbackHandler)
        }
    }

    fun getHandlers(): Map<Command, CommandHandler> = handlers.handlersView

    fun getHandlerTree(): CommandTreeEntry = handlers.handlerTree

    abstract fun handleCastError(sender: CommandSender, error: CastError)

    abstract fun handleParseError(
        sender: CommandSender,
        error: ParseErrorReason
    )

    fun getCandiatesByCommand(command: Command): List<String> {
        val commandWithArgs = CommandWithArgs.fromCommand(command)
        val tree = handlers.handlerTree.findTree(commandWithArgs)

        return tree.children.keys.toList()
    }

    private fun findRegisteredCommand(
        commandWithArgs: CommandWithArgs
    ): Command {
        var tree = handlers.handlerTree.findTree(commandWithArgs)
        var lastNonNullCommand: Command? = tree.command

        while (lastNonNullCommand == null) {
            tree = tree.parent ?: break
            lastNonNullCommand = tree.command
        }

        return lastNonNullCommand!!
    }

    private fun validateCommandSender(
        sender: CommandSender,
        expected: CommandSenderType
    ): CastError? {
        return if (
            expected == CommandSenderType.PLAYER &&
            sender !is Player
        ) {
            CastError.CANNOT_CAST_TO_PLAYER
        } else if (
            expected == CommandSenderType.CONSOLE &&
            sender !is ConsoleCommandSender
        ) {
            CastError.CANNOT_CAST_TO_CONSOLE
        } else {
            null
        }
    }

    private fun executeCommand(
        sender: CommandSender,
        command: BukkitCommand,
        args: Array<String>
    ) {
        val commandWithArgs = CommandWithArgs.fromCommandAndArgs(
            command.name,
            args
        )
        val foundCommand = findRegisteredCommand(commandWithArgs)

        val handler = handlers[foundCommand]!!
        @Suppress("NAME_SHADOWING")
        val args = foundCommand.getArgsFromList(commandWithArgs)

        val parsed = when (val result = handler.commandSyntax.parse(args)) {
            is Either.Left -> {
                val reason = result.a
                handleParseError(sender, reason)
                return
            }
            is Either.Right -> result.b
        }

        validateCommandSender(sender, handler.senderType)?.let {
            handleCastError(sender, it)
            return
        }

        handler.handleCommand(
            this,
            sender,
            foundCommand,
            parsed
        )
    }

    fun executeTabCompletion(
        sender: CommandSender,
        bukkitCommand: BukkitCommand,
        args: Array<String>
    ): List<String> {
        val argsWithoutSpace = args
            .filter { it.isNotEmpty() }
            .let {
                val last = it.lastOrNull()
                if (last == null || last.isEmpty()) {
                    val mutable = it.toMutableList()
                    mutable.add("")
                    mutable
                } else {
                    it
                }
            }
        val commandWithArgs = CommandWithArgs.fromCommandAndArgs(
            bukkitCommand.name,
            argsWithoutSpace.toTypedArray()
        )
        val command = findRegisteredCommand(commandWithArgs)

        @Suppress("NAME_SHADOWING")
        val args = command.getArgsFromList(command + argsWithoutSpace)

        val handler = handlers[command]!!

        val completionData = when (val result = handler.commandSyntax.parseCompleting(args)) {
            is Either.Left -> {
                val reason = result.a
                handleParseError(sender, reason)
                return emptyList()
            }
            is Either.Right -> result.b
        }

        return handler.handleTabComplete(
            this,
            sender,
            command,
            completionData
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
            executeCommand(
                sender,
                command,
                args
            )

            return true
        }
    }
}

private class CommandWithArgs private constructor(list: List<String>) :
    List<String> by list {

    companion object {

        fun fromCommandAndArgs(
            command: String,
            args: Array<String>
        ): CommandWithArgs {
            return CommandWithArgs(
                ArrayList<String>().apply {
                    add(command.toLowerCase())
                    for (e in args) {
                        add(e.toLowerCase())
                    }
                }
            )
        }

        fun fromCommand(command: Command): CommandWithArgs {
            return CommandWithArgs(command.command)
        }
    }
}
