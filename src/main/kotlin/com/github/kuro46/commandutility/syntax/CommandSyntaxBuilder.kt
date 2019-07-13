package com.github.kuro46.commandutility.syntax

/**
 * A builder class for CommandSyntax.
 */
class CommandSyntaxBuilder {
    private val args = ArrayList<Argument>()

    /**
     * Adds information of an argument.
     *
     * @throws CommandSyntaxException if argument order is illegal
     * @param argument the information of an argument to add.
     */
    fun addArgument(argument: Argument) {
        checkArgumentOrder(args.lastIndex + 1, argument)
        args.add(argument)
    }

    /**
     * Sets information of an argument to specified index.
     *
     * @throws CommandSyntaxException If argument order is illegal.
     * @param argument information of an argument to set.
     */
    fun setArgument(index: Int, argument: Argument) {
        checkArgumentOrder(index, argument)
        args[index] = argument
    }

    /**
     * Returns the list of arguments.
     *
     * @return the list of arguments
     */
    fun arguments(): List<Argument> = args.toList()

    private fun checkArgumentOrder(index: Int, argument: Argument) {
        if (index == 0) {
            return
        }

        val prevIndex = index - 1
        val prevArgument = args[prevIndex]

        if (prevArgument is RequiredArgument) return

        if (prevArgument is LongArgument) {
            throw CommandSyntaxException("Cannot set any arguments after a long argument.")
        }

        if (prevArgument is OptionalArgument) {
            val isArgumentRequired =
                argument is RequiredArgument ||
                    (argument is LongArgument && argument.isRequired)

            if (isArgumentRequired) {
                throw CommandSyntaxException("Cannot set required arguments after optional arguments.")
            }
        }
    }

    /**
     * Builds the CommandSyntax.
     *
     * @return the CommandSyntax.
     */
    fun build(): CommandSyntax {
        val requiredArgs = ArrayList<RequiredArgument>()
        val optionalArgs = ArrayList<OptionalArgument>()
        var longArg: LongArgument? = null

        for (arg in args) {
            when (arg) {
                is RequiredArgument -> requiredArgs.add(arg)
                is OptionalArgument -> optionalArgs.add(arg)
                is LongArgument -> longArg = arg
            }
        }

        return CommandSyntax(
            requiredArgs,
            optionalArgs,
            longArg
        )
    }
}
