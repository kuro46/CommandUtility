package com.github.kuro46.commandutility

class RawCommand(val rawCommand: List<String>) : List<String> by rawCommand {

    companion object {

        fun fromCommandAndArgs(command: String, args: Collection<String>): RawCommand {
            val appended = ArrayList<String>(1 + args.size).apply {
                add(command)
                addAll(args)
            }.map { it.toLowerCase() }

            return RawCommand(appended)
        }
    }
}
