package com.github.kuro46.commandutility

class Command private constructor(
    val command: List<String>
) : List<String> by command {

    fun getArgsFromList(list: List<String>): List<String> {
        return list.drop(size)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Command) {
            return false
        }

        return command == other.command
    }

    override fun hashCode(): Int {
        return command.hashCode()
    }

    companion object {
        fun fromString(command: String): Command {
            return Command(command.toLowerCase().split(' '))
        }
    }
}
