package com.github.kuro46.commandutility.handle

import arrow.core.Either
import com.github.kuro46.commandutility.StringConverters
import com.github.kuro46.commandutility.syntax.ParseErrorReason
import org.bukkit.Bukkit
import org.bukkit.command.Command as BukkitCommand
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

abstract class CommandManager(
    val fallbackHandler: FallbackCommandHandler,
    val stringConverters: StringConverters
) {

    private val commands = Commands()
    private val tabExecutorImpl = TabExecutorImpl()

    val commandsView: Map<CommandSections, Command>
        get() = commands.commandsView
    val commandTree: CommandTreeRoot
        get() = commands.commandTree

    /**
     * Registers a command.
     * If same named command has already registered, a command is not overwrite.
     *
     * @param command Command to register.
     * @return `true` if registered. `false` if not registered.
     */
    fun registerCommand(command: Command): Boolean {
        val sections = command.sections

        if (commands.contains(sections)) {
            return false
        }

        val firstSection = sections.first()

        val needsRegisterFallback =
            !commandTree.children.containsKey(firstSection) &&
                sections.toString() != firstSection.toString()
        if (needsRegisterFallback) {
            registerCommand(
                Command(CommandSections(listOf(firstSection)), fallbackHandler)
            )
        }

        val needsHookToBukkit = !commandTree.children.containsKey(firstSection)

        commands[sections] = command

        if (needsHookToBukkit) {
            val bukkitCommand = Bukkit.getPluginCommand(firstSection.toString())
            bukkitCommand.executor = tabExecutorImpl
            bukkitCommand.tabCompleter = tabExecutorImpl
        }

        return true
    }

    abstract fun handleParseError(
        sender: CommandSender,
        error: ParseErrorReason
    )

    abstract fun handleCastError(sender: CommandSender, error: CastError)

    private fun validateCommandSender(
        expectedType: CommandSenderType,
        sender: CommandSender
    ): Boolean {
        val actualType = when (sender) {
            is Player -> CommandSenderType.PLAYER
            is ConsoleCommandSender -> CommandSenderType.CONSOLE
            else -> CommandSenderType.ANY
        }

        if (
            expectedType == actualType ||
            expectedType == CommandSenderType.ANY
        ) {
            return true
        }

        val error = when (expectedType) {
            CommandSenderType.PLAYER -> CastError.CANNOT_CAST_TO_PLAYER
            CommandSenderType.CONSOLE -> CastError.CANNOT_CAST_TO_CONSOLE
            else -> throw IllegalStateException()
        }

        handleCastError(sender, error)

        return false
    }

    private fun getTreeByRawSections(rawSections: List<String>): CommandTree {
        val sectionsToFind = CommandSections.fromStrings(rawSections)
        return when (val treeEntry = commandTree.findTree(sectionsToFind)) {
            is CommandTreeRoot -> throw IllegalArgumentException(
                "'$sectionsToFind' has not registered yet."
            )
            is CommandTree -> treeEntry
        }
    }
    private fun executeCommand(
        sender: CommandSender,
        bukkitCommand: BukkitCommand,
        args: Array<String>
    ) {
        val rawSections = listOf(bukkitCommand.name) + args
        val command = when (
            val entry = getTreeByRawSections(rawSections).backWhileCommandIsNull()
        ) {
            is CommandTreeRoot -> throw IllegalStateException(
                "Non-null section not found! ($rawSections)"
            )
            is CommandTree -> entry.command!!
        }
        val handler = command.handler

        if (!validateCommandSender(handler.senderType, sender)) {
            return
        }

        val commandSections = command.sections

        val parsedArgs = run {
            val actualArgs = rawSections.drop(commandSections.size)
            when (val result = handler.commandSyntax.parse(actualArgs)) {
                is Either.Left -> {
                    val (reason, _) = result.a
                    handleParseError(sender, reason)
                    return
                }
                is Either.Right -> result.b
            }
        }

        handler.handleCommand(this, sender, commandSections, parsedArgs)
    }

    private fun executeTabComplete(
        sender: CommandSender,
        bukkitCommand: BukkitCommand,
        args: Array<String>
    ): List<String> {
        val argsWithoutSpace = args.filter { it.isNotEmpty() }
        val rawSections = listOf(bukkitCommand.name) + argsWithoutSpace
        val command = getTreeByRawSections(rawSections).command

        return if (command != null) {
            getCandidatesByHandler(sender, rawSections, args, command)
        } else {
            val completing = args.lastOrNull() ?: ""
            getCandidatesByTree(CommandSections.fromStrings(rawSections), completing)
        }
    }

    fun getCandidatesByTree(sections: CommandSections, completing: String): List<String> {
        return commandTree
            .findTree(sections)
            .children
            .keys
            .map { it.toString() }
            .filter { it.startsWith(completing, true) }
    }

    private fun getCandidatesByHandler(
        sender: CommandSender,
        rawSections: List<String>,
        rawArgs: Array<String>,
        command: Command
    ): List<String> {
        val handler = command.handler
        val commandSections = command.sections

        if (!validateCommandSender(handler.senderType, sender)) {
            return emptyList()
        }

        val completionData = run {
            val actualArgs = rawSections.drop(commandSections.size)
                .let {
                    if (it.isEmpty() || rawArgs.lastOrNull()?.isEmpty() ?: false) {
                        val mutableIt = it.toMutableList()
                        mutableIt.add("")
                        mutableIt
                    } else it
                }
            when (val result = handler.commandSyntax.parseCompleting(actualArgs)) {
                is Either.Left -> {
                    val reason = result.a
                    handleParseError(sender, reason)
                    return emptyList()
                }
                is Either.Right -> result.b
            }
        }

        return handler.handleTabComplete(
            this,
            sender,
            commandSections,
            completionData
        )
    }

    private inner class TabExecutorImpl : TabExecutor {

        override fun onCommand(
            sender: CommandSender,
            command: BukkitCommand,
            label: String,
            args: Array<String>
        ): Boolean {
            executeCommand(sender, command, args)
            return true
        }

        override fun onTabComplete(
            sender: CommandSender,
            command: BukkitCommand,
            alias: String,
            args: Array<String>
        ): List<String> {
            return executeTabComplete(sender, command, args)
        }
    }
}
