package com.github.kuro46.commandutility.handle

import com.github.kuro46.commandutility.syntax.CommandSyntaxBuilder
import org.bukkit.command.CommandSender

class HelpCommandHandler(val commandTree: CommandTree) : CommandHandler() {

    override val commandSyntax = CommandSyntaxBuilder().build()

    override val senderType = CommandSenderType.ANY

    override fun handleCommand(
        caller: CommandManager,
        sender: CommandSender,
        commandSections: CommandSections,
        args: Map<String, String>
    ) {
        commandTree.forEach {
            val command = it.command
            if (command == null || command.handler is FallbackCommandHandler) {
                return@forEach
            }
            val description = command.description ?: "No description provided"
            sender.sendMessage("/${command.sections} ${command.handler.commandSyntax} - $description")
        }
    }
}
