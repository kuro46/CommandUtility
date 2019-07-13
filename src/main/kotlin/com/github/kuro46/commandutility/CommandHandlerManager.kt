package com.github.kuro46.commandutility

import com.github.kuro46.commandutility.syntax.ParseResult
import java.util.concurrent.Executor
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
abstract class CommandHandlerManager(val plugin: Plugin, val executor: Executor) {

    private val handlers = CommandHandlers()
    private val commandExecutor = CommandExecutorImpl()
    private val tabCompleter = TabCompleterImpl()

    fun registerHandler(command: String, handler: CommandHandler) {
        @Suppress("NAME_SHADOWING")
        val command = Command.fromString(command)
        val name = command[0]

        val needRegisterCommand = handlers.getHandlerTree().children.containsKey(name)

        handlers[command] = handler

        if (needRegisterCommand) {
            val bukkitCommand = Bukkit.getPluginCommand(name)
            bukkitCommand.executor = commandExecutor
            bukkitCommand.tabCompleter = tabCompleter
            registerHandler(name, newRootCommandHandler(command))
        }
    }

    abstract fun newRootCommandHandler(command: Command): CommandHandler

    abstract fun handleCastError(sender: CommandSender, castError: CastError)

    abstract fun handleParseError(
        sender: CommandSender,
        parseError: ParseError
    )

    fun getCandiatesByCommand(command: Command): List<String> {
        val commandWithArgs = CommandWithArgs.fromCommand(command)
        val tree = handlers.getHandlerTree().findTree(commandWithArgs)

        return tree.children.keys.toList()
    }

    private fun findRegisteredCommand(
        commandWithArgs: CommandWithArgs
    ): Command {
        var tree = handlers.getHandlerTree().findTree(commandWithArgs)
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

        val handler = handlers[foundCommand]!!
        @Suppress("NAME_SHADOWING")
        val args = foundCommand.getArgsFromList(commandWithArgs)
        val parseResult = handler.commandSyntax.parseArguments(args)

        if (parseResult is ParseResult.Error) {
            handleParseError(sender, parseResult.error)
            return
        }

        validateCommandSender(sender, handler.senderType)?.let {
            handleCastError(sender, it)
            return
        }

        handler.executionThread.executeAtSyncOrCurrentThread(plugin) {
            handler.handleCommand(
                this,
                sender,
                foundCommand,
                (parseResult as ParseResult.Success).args
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

        val handler = handlers[command]!!

        val completedArgs = if (args.isNotEmpty()) args.dropLast(1) else emptyList()

        val uncompletedArg = if (args.isNotEmpty()) args.last() else ""

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
