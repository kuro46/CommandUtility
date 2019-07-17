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
    ): Either<ParseErrorReason, Result?>

    data class Result(val value: String, val usedIndexes: IntRange)
}

/**
 * Information of a required argument.
 *
 * @constructor
 *
 * @property name name of this argument
 */
data class RequiredArgument(override val name: String) : Argument() {

    override fun parse(index: Int, rawArguments: List<String>): Either<ParseErrorReason, Result?> {
        val rawArgument = rawArguments.getOrNull(index)
        return if (rawArgument != null)
            Either.right(Result(rawArgument, index..index))
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
    override fun parse(index: Int, rawArguments: List<String>): Either<ParseErrorReason, Result?> {
        return Either.right(
            rawArguments.getOrNull(index)?.let {
                Result(
                    it,
                    index..index
                )
            }
        )
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

    override fun parse(index: Int, rawArguments: List<String>): Either<ParseErrorReason, Result?> {
        if (rawArguments.getOrNull(index) == null) {
            return if (isRequired)
                Either.Left(ParseErrorReason.ARGUMENTS_NOT_ENOUGH)
            else
                Either.Right(null)
        }

        val dropped = rawArguments.drop(index)
        val start = index + 1
        val end = rawArguments.lastIndex

        return Either.right(Result(dropped.joinToString(" "), start..end))
    }
}
