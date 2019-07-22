package com.github.kuro46.commandutility

/**
 * A command that contains command name, sub commands, and arguments.
 *
 * @property command Internal property for delegation
 */
class Command private constructor(
    val command: List<String>
) : List<String> by command {

    /**
     * Returns a list that drops size of [list].
     */
    fun getArgsFromList(list: List<String>): List<String> {
        return list.drop(size)
    }

    /**
     * Checks whether to equal with [other]
     */
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Command) {
            return false
        }

        return command == other.command
    }

    /**
     * Returns hash of this.
     */
    override fun hashCode(): Int {
        return command.hashCode()
    }

    companion object {

        /**
         * Converts string to Command.
         */
        fun fromString(command: String): Command {
            return Command(command.toLowerCase().split(' '))
        }
    }
}
