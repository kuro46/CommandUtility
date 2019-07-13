package com.github.kuro46.commandutility.syntax

import com.github.kuro46.commandutility.ParseError

/**
 * A CommandSyntax that expresses syntax of command.
 *
 * @constructor
 *
 * @property requiredArgs a list of required arguments
 * @property optionalArgs a list of optional arguments
 * @property longArg a long argument. `null` if this command doesn't want long argument
 */
class CommandSyntax(
    val requiredArgs: List<RequiredArgument>,
    val optionalArgs: List<OptionalArgument>,
    val longArg: LongArgument?
) {
    val arguments: List<Argument> = ArrayList<Argument>().apply {
        addAll(requiredArgs)
        addAll(optionalArgs)
        longArg?.let { add(longArg) }
    }

    init {
        fun validateSyntax() {
            if (optionalArgs.isNotEmpty() && (longArg != null && longArg.isRequired)) {
                throw CommandSyntaxException("Some arguments are optional, but long argument is required.")
            }

            val names = HashSet<String>()

            for (argument in arguments) {
                if (names.contains(argument.name)) {
                    throw CommandSyntaxException("Arguments named '${argument.name}' is duplicated.")
                }
                names.add(argument.name)
            }
        }

        validateSyntax()
    }

    fun parseArguments(rawArguments: List<String>): ParseResult {
        val parsed = HashMap<String, String>()

        arguments.forEachIndexed { index, argumentSyntax ->
            val rawArgument = rawArguments.getOrNull(index)

            val value = when (argumentSyntax) {
                is RequiredArgument -> {
                    rawArgument ?: return ParseResult.Error(ParseError.ARGUMENTS_NOT_ENOUGH)
                }
                is OptionalArgument -> rawArgument
                is LongArgument -> {
                    if (argumentSyntax.isRequired && rawArgument == null) {
                        return ParseResult.Error(ParseError.ARGUMENTS_NOT_ENOUGH)
                    }
                    rawArgument?.let { rawArguments.drop(index).joinToString(" ") }
                }
            }

            if (value != null) {
                parsed[argumentSyntax.name] = value
            }
        }

        return ParseResult.Success(parsed)
    }

    /**
     * Returns a String representation of this syntax.
     *
     * @return String representation of this syntax.
     */
    override fun toString(): String {
        fun getAllInfo(): List<Argument> {
            return ArrayList<Argument>().apply {
                addAll(requiredArgs)
                addAll(optionalArgs)
                longArg?.let { add(it) }
            }
        }

        fun getCharsToSurround(info: Argument): Pair<String, String> {
            return when (info) {
                is RequiredArgument -> Pair("<", ">")
                is OptionalArgument -> Pair("[", "]")
                is LongArgument -> {
                    if (info.isRequired) Pair("<", ">") else Pair("[", "]")
                }
            }
        }

        val builder = StringBuilder()

        for (info in getAllInfo()) {
            if (builder.isNotEmpty()) {
                builder.append(' ')
            }

            val (left, right) = getCharsToSurround(info)

            builder.append(left).append(info.name).append(right)
        }

        return builder.toString()
    }
}
