package com.github.kuro46.commandutility.handle

import com.github.kuro46.commandutility.syntax.CommandSyntaxBuilder
import org.bukkit.command.CommandSender

class HelpCommandHandler(val sectionsToSend: CommandSections) : CommandHandler() {

    override val commandSyntax = CommandSyntaxBuilder().build()

    override val senderType = CommandSenderType.ANY

    override fun handleCommand(
        caller: CommandManager,
        sender: CommandSender,
        commandSections: CommandSections,
        args: Map<String, String>
    ) {
        val commandTreeEntry = caller.commandTree.findTree(sectionsToSend)
        val commandTree = when (commandTreeEntry) {
            is CommandTreeRoot -> throw IllegalArgumentException("Tree: '$commandSections' not found!")
            is CommandTree -> commandTreeEntry
        }
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
