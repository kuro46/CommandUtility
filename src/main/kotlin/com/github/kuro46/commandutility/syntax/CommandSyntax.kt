package com.github.kuro46.commandutility.syntax

import arrow.core.Either

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
                throw CommandSyntaxException(
                    "Some arguments are optional, but long argument is required."
                )
            }

            val names = HashSet<String>()

            for (argument in arguments) {
                if (names.contains(argument.name)) {
                    throw CommandSyntaxException(
                        "Arguments named '${argument.name}' is duplicated."
                    )
                }
                names.add(argument.name)
            }
        }

        validateSyntax()
    }

    fun parseCompleting(
        argsWithoutSpace: List<String>
    ): Either<ParseErrorReason, CompletionData> {
        val parsed = when (val result = parse(argsWithoutSpace)) {
            is Either.Left -> {
                val (reason, parsed) = result.a
                if (reason == ParseErrorReason.ARGUMENTS_NOT_ENOUGH) {
                    parsed
                } else {
                    return Either.left(reason)
                }
            }
            is Either.Right -> result.b
        }

        val completingArgument = if (arguments.isNotEmpty()) {
            val name = arguments.getOrNull(argsWithoutSpace.lastIndex)
                ?.name
                ?: arguments.last().name
            val value = parsed.getValue(name)
            CompletingArgument(name, value)
        } else null

        return Either.right(CompletionData(parsed, completingArgument))
    }

    fun parse(
        raw: List<String>
    ): Either<Pair<ParseErrorReason, ParsedArgs>, ParsedArgs> {
        val parsed = HashMap<String, String>()

        var lastParsedIndex = -1
        arguments.forEachIndexed { index, syntax ->
            val value = when (val result = syntax.parse(index, raw)) {
                is Either.Left -> return Either.left(Pair(result.a, parsed))
                is Either.Right -> result.b
            }

            if (value != null) {
                lastParsedIndex = value.usedIndexes.last
                parsed[syntax.name] = value.value
            }
        }

        return if (lastParsedIndex != raw.lastIndex) {
            Either.left(Pair(ParseErrorReason.TOO_MANY_ARGUMENTS, parsed))
        } else Either.right(parsed)
    }

    override fun toString(): String {
        return arguments.joinToString(" ")
    }

    companion object {

        @JvmStatic
        fun builder(): CommandSyntaxBuilder = CommandSyntaxBuilder()
    }
}

typealias ParsedArgs = Map<String, String>

data class CompletionData(
    val completedArgs: ParsedArgs,
    val completingArgument: CompletingArgument?
)

data class CompletingArgument(val name: String, val value: String)
