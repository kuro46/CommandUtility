package com.github.kuro46.commandutility.syntax

import com.github.kuro46.commandutility.ParseErrorReason

/**
 * Information of an argument.
 */
sealed class Argument {
    /**
     * Name of this argument.
     */
    abstract val name: String

    abstract fun parse(index: Int, rawArguments: List<String>): ParseResult<String?>
}

/**
 * Information of a required argument.
 *
 * @constructor
 *
 * @property name name of this argument
 */
data class RequiredArgument(override val name: String) : Argument() {

    override fun parse(index: Int, rawArguments: List<String>): ParseResult<String?> {
        val rawArgument = rawArguments.getOrNull(index)
        return if (rawArgument != null)
            ParseResult.Success(rawArgument)
        else
            ParseResult.Error(ParseErrorReason.ARGUMENTS_NOT_ENOUGH)
    }
}

/**
 * Information of an optional argument.
 *
 * @constructor
 *
 * @property name name of this argument
 */
data class OptionalArgument(override val name: String) : Argument() {
    override fun parse(index: Int, rawArguments: List<String>): ParseResult<String?> {
        return ParseResult.Success(rawArguments.getOrNull(index))
    }
}

/**
 * Information of a long argument. This argument is placed at last of arguments.
 *
 * @constructor
 *
 * @property name name of this argument
 * @property isRequired `true` if this argument is required.
 * `false` if this argument is optional
 */
data class LongArgument(
    override val name: String,
    val isRequired: Boolean
) : Argument() {

    override fun parse(index: Int, rawArguments: List<String>): ParseResult<String?> {
        if (rawArguments.getOrNull(index) == null) {
            return if (isRequired)
                ParseResult.Error(ParseErrorReason.ARGUMENTS_NOT_ENOUGH)
            else
                ParseResult.Success(null)
        }

        return ParseResult.Success(rawArguments.drop(index).joinToString(" "))
    }
}
