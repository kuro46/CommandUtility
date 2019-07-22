package com.github.kuro46.commandutility

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
     * If any of these are false, [CommandHandlerManager.handleCastError] and/or [CommandHandlerManager.handleParseError] are called and this method is not called.
     */
    abstract fun handleCommand(
        caller: CommandHandlerManager,
        sender: CommandSender,
        command: Command,
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
     * In default implementation, this method returns list of subcommands using [CommandHandlerManager.getCandiatesByCommand]
     */
    open fun handleTabComplete(
        caller: CommandHandlerManager,
        sender: CommandSender,
        command: Command,
        completionData: CompletionData
    ): List<String> {
        return caller.getCandiatesByCommand(command)
    }
}
