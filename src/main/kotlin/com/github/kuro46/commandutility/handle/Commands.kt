package com.github.kuro46.commandutility.handle

class Commands {

    private val commands = hashMapOf<CommandSections, Command>()

    /**
     * View of commands.
     */
    val commandsView: Map<CommandSections, Command>
        get() = commands

    /**
     * Tree of commands.
     */
    var commandTree = buildCommandTree()
        private set

    fun contains(commandSections: CommandSections): Boolean = commands.containsKey(commandSections)

    fun put(command: Command): Command? {
        val put = commands.put(command.sections, command)
        updateCommandTree()
        return put
    }

    fun remove(commandSections: CommandSections): Command? {
        val removed = commands.remove(commandSections)
        updateCommandTree()
        return removed
    }

    operator fun get(commandSections: CommandSections): Command? = commands[commandSections]

    operator fun set(commandSections: CommandSections, command: Command) {
        commands[commandSections] = command
        updateCommandTree()
    }

    private fun updateCommandTree() {
        commandTree = buildCommandTree()
    }

    private fun buildCommandTree(): CommandTreeRoot {
        val root = MutableCommandTreeRoot()

        for ((sections, command) in commands) {
            var entry: MutableCommandTreeEntry = root
            for (section in sections) {
                entry = entry.children
                    .getOrPut(section) { MutableCommandTree(null) }
            }
            when (entry) {
                is MutableCommandTreeRoot -> throw IllegalStateException("Empty section found!")
                is MutableCommandTree -> entry.command = command
            }
        }

        return root.toImmutable()
    }
}

private data class MutableCommandTree(
    var command: Command?
) : MutableCommandTreeEntry() {
    override val children: MutableMap<CommandSection, MutableCommandTree> = HashMap()

    fun toImmutable(): CommandTree {
        val immutableChildren = hashMapOf<CommandSection, CommandTree>()
        children.forEach { section, mutableCommandTree ->
            immutableChildren[section] = mutableCommandTree.toImmutable()
        }
        return CommandTree(command!!, immutableChildren)
    }
}

private class MutableCommandTreeRoot : MutableCommandTreeEntry() {
    override val children: MutableMap<CommandSection, MutableCommandTree> = HashMap()

    fun toImmutable(): CommandTreeRoot {
        val immutableChildren = hashMapOf<CommandSection, CommandTree>()
        children.forEach { section, mutableCommandTree ->
            immutableChildren[section] = mutableCommandTree.toImmutable()
        }
        return CommandTreeRoot(immutableChildren)
    }
}

private sealed class MutableCommandTreeEntry {
    abstract val children: MutableMap<CommandSection, MutableCommandTree>
}
