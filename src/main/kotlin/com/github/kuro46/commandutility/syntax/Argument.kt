package com.github.kuro46.commandutility.syntax

import arrow.core.Either
import com.github.kuro46.commandutility.ParseErrorReason

/**
 * Information of an argument.
 */
sealed class Argument {
    /**
     * Name of this argument.
     */
    abstract val name: String

    abstract fun parse(
        index: Int,
        rawArguments: List<String>
    ): Either<ParseErrorReason, String?>
}

/**
 * Information of a required argument.
 *
 * @constructor
 *
 * @property name name of this argument
 */
data class RequiredArgument(override val name: String) : Argument() {

    override fun parse(index: Int, rawArguments: List<String>): Either<ParseErrorReason, String?> {
        val rawArgument = rawArguments.getOrNull(index)
        return if (rawArgument != null)
            Either.Right(rawArgument)
        else
            Either.Left(ParseErrorReason.ARGUMENTS_NOT_ENOUGH)
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
    override fun parse(index: Int, rawArguments: List<String>): Either<ParseErrorReason, String?> {
        return Either.Right(rawArguments.getOrNull(index))
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

    override fun parse(index: Int, rawArguments: List<String>): Either<ParseErrorReason, String?> {
        if (rawArguments.getOrNull(index) == null) {
            return if (isRequired)
                Either.Left(ParseErrorReason.ARGUMENTS_NOT_ENOUGH)
            else
                Either.Right(null)
        }

        return Either.Right(rawArguments.drop(index).joinToString(" "))
    }
}
