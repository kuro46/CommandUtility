package com.github.kuro46.commandutility.handle

/**
 * A command tree.
 *
 * @property command A command in this tree.
 * @property children Children of this tree.
 */
data class CommandTree(
    val command: Command,
    override val children: Map<CommandSection, CommandTree>
) : CommandTreeEntry() {
    override fun toString(): String {
        return "command: '$command' chidlren: '$children'"
    }
}

/**
 * A root of [CommandTree].
 *
 * @property children Children of this tree.
 */
data class CommandTreeRoot(
    override val children: Map<CommandSection, CommandTree>
) : CommandTreeEntry() {

    /**
     * Find a tree of specified command name.
     *
     * @param sections Sections to find.
     */
    fun findTree(sections: CommandSections): CommandTreeEntry {
        var currentTree: CommandTreeEntry = this
        for (section in sections) {
            currentTree = currentTree.children[section] ?: break
        }
        return currentTree
    }

    override fun toString(): String {
        return children.toString()
    }
}

/**
 * A tree entry.
 */
sealed class CommandTreeEntry {

    /**
     * Children of this tree.
     */
    abstract val children: Map<CommandSection, CommandTree>
}
