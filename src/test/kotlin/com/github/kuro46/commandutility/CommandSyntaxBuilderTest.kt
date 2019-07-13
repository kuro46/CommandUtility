package com.github.kuro46.commandutility

import com.github.kuro46.commandutility.syntax.Argument
import com.github.kuro46.commandutility.syntax.CommandSyntaxBuilder
import com.github.kuro46.commandutility.syntax.CommandSyntaxException
import com.github.kuro46.commandutility.syntax.LongArgument
import com.github.kuro46.commandutility.syntax.OptionalArgument
import com.github.kuro46.commandutility.syntax.RequiredArgument
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class CommandSyntaxBuilderTest {

    @Test
    fun `add a required-argument after added an OptionalArgument`() {
        fun test(argument: Argument) {
            val builder = CommandSyntaxBuilder()
                .addArgument(OptionalArgument("argument1"))

            val exception = assertThrows<CommandSyntaxException> {
                builder.addArgument(argument)
            }

            assertEquals("Cannot set required arguments after optional arguments.", exception.message)
        }

        test(RequiredArgument("argument2"))
        test(LongArgument("argument2", true))
    }

    @Test
    fun `add an argument after added a LongArgument`() {
        fun test(isRequired: Boolean, argument: Argument) {
            val builder = CommandSyntaxBuilder()
                .addArgument(LongArgument("argument1", isRequired))

            val exception = assertThrows<CommandSyntaxException> {
                builder.addArgument(argument)
            }
            assertEquals("Cannot set any arguments after a long argument.", exception.message)
        }

        val argName = "argument2"
        test(true, RequiredArgument(argName))
        test(false, RequiredArgument(argName))
        test(true, OptionalArgument(argName))
        test(false, OptionalArgument(argName))
    }

    @Test
    fun `add an argument after added a RequiredArgument`() {
        fun test(argument: Argument) {
            val builder = CommandSyntaxBuilder()
                .addArgument(RequiredArgument("argument1"))

            assertDoesNotThrow {
                builder.addArgument(argument)
            }
        }

        val argName = "argument2"
        test(RequiredArgument(argName))
        test(OptionalArgument(argName))
        test(LongArgument(argName, true))
        test(LongArgument(argName, false))
    }

    @Test
    fun `build a CommandSyntax`() {
        val builder = CommandSyntaxBuilder()
        for (i in 1..5) {
            builder.addArgument(RequiredArgument(builder.arguments().size.toString()))
        }
        for (i in 1..5) {
            builder.addArgument(OptionalArgument(builder.arguments().size.toString()))
        }
        builder.addArgument(LongArgument(builder.arguments().size.toString(), false))

        val syntax = builder.build()

        assertEquals(11, syntax.arguments.size)
    }
}
