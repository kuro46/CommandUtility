package com.github.kuro46.commandutility.handle

import com.github.kuro46.commandutility.syntax.CommandSyntax
import com.github.kuro46.commandutility.syntax.CompletionData
import org.bukkit.command.CommandSender

/**
 * CommandHandler that processes tab completion and command execution.
 */
abstract class CommandHandler {

    /**
     * A command syntax to process.
     */
    abstract val commandSyntax: CommandSyntax

    /**
     * Type of CommandSender that acceptable by [handleCommand] and [handleTabComplete].
     */
    abstract val senderType: CommandSenderType

    /**
     * Processes a command.
     *
     * Called when all of below conditions are true:
     * - Type of [sender] is equal to [senderType]
     * - Arguments are matched with [commandSyntax]
     *
     * If any of these are false, [CommandManager.handleCastError] and/or [CommandManager.handleParseError] are called and this method is not called.
     */
    abstract fun handleCommand(
        caller: CommandManager,
        sender: CommandSender,
        commandSections: CommandSections,
        args: Map<String, String>
    )

    /**
     * Processes tab completion.
     *
     * Called when all of below conditions are true:
     * - Type of [sender] is equal to [senderType]
     * - Arguments are matched with [commandSyntax]
     *
     * If any of these are false, [CommandHandlerManager.handleCastError] and/or [CommandHandlerManager.handleParseError] are called and this method is not called.
     *
     * In default implementation, this method returns list of subcommands using [CommandHandlerManager.getCandidatesByCommand]
     */
    open fun handleTabComplete(
        caller: CommandManager,
        sender: CommandSender,
        commandSections: CommandSections,
        completionData: CompletionData
    ): List<String> {
        return getChildrenBySections(caller, commandSections)
    }

    fun getChildrenBySections(
        caller: CommandManager,
        sections: CommandSections
    ): List<String> {
        val tree = caller.commandTree.findTree(sections)
        return tree.children.keys.map { it.toString() }
    }
}
