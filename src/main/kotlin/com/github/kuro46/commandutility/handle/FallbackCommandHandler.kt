package com.github.kuro46.commandutility.handle

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
        caller: CommandManager,
        sender: CommandSender,
        commandSections: CommandSections,
        args: Map<String, String>
    ) {
        handleFallback(
            caller,
            sender,
            commandSections,
            args.getOrElse("args") { "" }.split(" ")
        )
    }

    abstract fun handleFallback(
        caller: CommandManager,
        sender: CommandSender,
        commandSections: CommandSections,
        args: List<String>
    )

    override fun handleTabComplete(
        caller: CommandManager,
        sender: CommandSender,
        commandSections: CommandSections,
        completionData: CompletionData
    ): List<String> {
        return super.getChildrenBySections(caller, commandSections)
    }
}
