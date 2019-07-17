package com.github.kuro46.commandutility.syntax

import arrow.core.Either
import com.github.kuro46.commandutility.ParseErrorReason

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

    fun parseCompleting(argsWithoutSpace: List<String>): Either<ParseErrorReason, CompletionData> {
        val parsed = when (val result = parse(argsWithoutSpace)) {
            is Either.Left -> {
                val (_, reason) = result.a
                return Either.left(reason)
            }
            is Either.Right -> result.b
        }

        val notCompletedArgumentName = arguments.getOrNull(argsWithoutSpace.lastIndex)
            ?.name
            ?: arguments.last().name
        val notCompletedArgumentValue = argsWithoutSpace.last()

        val completionData = CompletionData(
            parsed,
            NotCompletedArg(
                notCompletedArgumentName,
                notCompletedArgumentValue
            )
        )
        return Either.right(completionData)
    }

    fun parse(
        raw: List<String>
    ): Either<Pair<ParsedArgs, ParseErrorReason>, ParsedArgs> {
        val parsed = HashMap<String, String>()

        var lastParsedIndex = -1
        arguments.forEachIndexed { index, syntax ->
            val value = when (val result = syntax.parse(index, raw)) {
                is Either.Left -> return Either.left(Pair(parsed, result.a))
                is Either.Right -> result.b
            }

            if (value != null) {
                lastParsedIndex = value.usedIndexes.endInclusive
                parsed[syntax.name] = value.value
            }
        }

        if (lastParsedIndex != raw.lastIndex) {
            return Either.left(Pair(parsed, ParseErrorReason.TOO_MANY_ARGUMENTS))
        }

        return Either.right(parsed)
    }

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

typealias ParsedArgs = Map<String, String>

data class CompletionData(
    val completedArgs: ParsedArgs,
    val notCompletedArg: NotCompletedArg
)

data class NotCompletedArg(val name: String, val value: String)
