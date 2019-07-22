package com.github.kuro46.commandutility

import com.github.kuro46.commandutility.syntax.CommandSyntaxBuilder
import com.github.kuro46.commandutility.syntax.CompletionData
import com.github.kuro46.commandutility.syntax.LongArgument
import org.bukkit.command.CommandSender

abstract class FallbackCommandHandler : CommandHandler() {

    override val commandSyntax = CommandSyntaxBuilder().apply {
        addArgument(LongArgument("args", false))
    }.build()

    abstract override val senderType: CommandSenderType

    override fun handleCommand(
        caller: CommandHandlerManager,
        sender: CommandSender,
        command: Command,
        args: Map<String, String>
    ) {
        handleFallback(
            caller,
            sender,
            command,
            args.getOrElse("args") { "" }.split(" ")
        )
    }

    abstract fun handleFallback(
        caller: CommandHandlerManager,
        sender: CommandSender,
        command: Command,
        args: List<String>
    )

    override fun handleTabComplete(
        caller: CommandHandlerManager,
        sender: CommandSender,
        command: Command,
        completionData: CompletionData
    ): List<String> {
        return caller.getCandidatesByCommand(command)
    }
}
