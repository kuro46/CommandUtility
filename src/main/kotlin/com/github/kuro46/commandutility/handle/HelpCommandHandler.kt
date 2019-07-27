package com.github.kuro46.commandutility.handle

import com.github.kuro46.commandutility.syntax.CommandSyntaxBuilder
import org.bukkit.command.CommandSender

open class HelpCommandHandler(val sectionsToSend: CommandSections) : CommandHandler() {

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
        sender.sendMessageIfNonNull(createFirstLine(sectionsToSend))
        commandTree.forEach {
            val command = it.command ?: return@forEach
            sender.sendMessageIfNonNull(createCommandLine(command))
        }
        sender.sendMessageIfNonNull(createLastLine(sectionsToSend))
    }

    open fun createFirstLine(sections: CommandSections): String? =
        "Help for $sections ----------"

    open fun createLastLine(sections: CommandSections): String? = null

    open fun createCommandLine(command: Command): String? {
        if (command.handler is FallbackCommandHandler) return null
        val description = command.description ?: "No description provided"
        return "/${command.sections} ${command.handler.commandSyntax} - $description"
    }
}

private fun CommandSender.sendMessageIfNonNull(message: String?) {
    message?.let { sendMessage(it) }
}
