package com.github.kuro46.commandutility.handle

import com.github.kuro46.commandutility.syntax.CommandSyntaxBuilder
import com.github.kuro46.commandutility.syntax.CompletionData
import com.github.kuro46.commandutility.syntax.LongArgument
import org.bukkit.command.CommandSender

open class HelpCommandHandler(val rootSections: CommandSections) : CommandHandler() {

    override val commandSyntax = CommandSyntaxBuilder().apply {
        addArgument(LongArgument("children", false))
    }.build()

    override val senderType = CommandSenderType.ANY

    override fun handleCommand(
        caller: CommandManager,
        sender: CommandSender,
        commandSections: CommandSections,
        args: Map<String, String>
    ) {
        val sections = rootSections.let {
            if (args.containsKey("children")) {
                val childrenSections = CommandSections.fromString(args.getValue("children"))
                CommandSections(it + childrenSections)
            } else it
        }
        val commandTreeEntry = caller.commandTree.findTree(sections)
        val commandTree = when (commandTreeEntry) {
            is CommandTreeRoot -> throw IllegalArgumentException("Tree: '$commandSections' not found!")
            is CommandTree -> commandTreeEntry
        }
        sender.sendMessageIfNonNull(createFirstLine(sections))
        commandTree.forEach {
            val command = it.command ?: return@forEach
            sender.sendMessageIfNonNull(createCommandLine(command))
        }
        sender.sendMessageIfNonNull(createLastLine(sections))
    }

    open fun createFirstLine(sections: CommandSections): String? =
        "Help for '$sections' ----------"

    open fun createLastLine(sections: CommandSections): String? = null

    open fun createCommandLine(command: Command): String? {
        if (command.handler is FallbackCommandHandler) return null
        val description = command.description ?: "No description provided"
        return "/${command.sections} ${command.handler.commandSyntax} - $description"
    }

    override fun handleTabComplete(
        caller: CommandManager,
        sender: CommandSender,
        commandSections: CommandSections,
        completionData: CompletionData
    ): List<String> {
        val completing = completionData.completedArgs.getValue("children")
        val additionalSections = CommandSections.fromString(completing)

        val adddedSections = CommandSections(rootSections + additionalSections)

        return caller
            .commandTree
            .findTree(adddedSections)
            .children
            .keys
            .map { it.toString() }
    }
}

private fun CommandSender.sendMessageIfNonNull(message: String?) {
    message?.let { sendMessage(it) }
}
