package com.github.kuro46.commandutility

import com.github.kuro46.commandutility.syntax.CommandSyntax
import org.bukkit.command.CommandSender

abstract class CommandHandler {

    abstract val executionThread: ExecutionThreadType

    abstract val commandSyntax: CommandSyntax

    abstract val senderType: CommandSenderType

    abstract fun handleCommand(
        caller: CommandHandlerManager,
        sender: CommandSender,
        command: Command,
        args: Map<String, String>
    )

    open fun handleTabComplete(
        caller: CommandHandlerManager,
        sender: CommandSender,
        command: Command,
        completedArgs: List<String>,
        uncompletedArg: String
    ): List<String> {
        return caller.getCandiatesByCommand(command)
    }
}
