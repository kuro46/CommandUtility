package com.github.kuro46.commandutility.syntax

/**
 * Information of an argument.
 */
sealed class Argument {
    /**
     * Name of this argument.
     */
    abstract val name: String
}

/**
 * Information of a required argument.
 *
 * @constructor
 *
 * @property name name of this argument
 */
data class RequiredArgument(override val name: String) : Argument()

/**
 * Information of an optional argument.
 *
 * @constructor
 *
 * @property name name of this argument
 */
data class OptionalArgument(override val name: String) : Argument()

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
) : Argument()
