package com.github.kuro46.commandutility

import com.github.kuro46.commandutility.syntax.CommandSyntax
import com.github.kuro46.commandutility.syntax.CommandSyntaxException
import com.github.kuro46.commandutility.syntax.LongArgument
import com.github.kuro46.commandutility.syntax.OptionalArgument
import com.github.kuro46.commandutility.syntax.RequiredArgument
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class CommandSyntaxTest {

    @Test
    fun `set OptionalArgument and required-LongArgument`() {
        val exception = assertThrows<CommandSyntaxException> {
            CommandSyntax(
                emptyList(),
                listOf(OptionalArgument("argument1")),
                LongArgument("argument2", true)
            )
        }

        assertEquals("Some arguments are optional, but long argument is required.", exception.message)
    }

    @Test
    fun `make name of arguments duplicated`() {
        val exception = assertThrows<CommandSyntaxException> {
            CommandSyntax(
                listOf(RequiredArgument("argument")),
                listOf(OptionalArgument("argument")),
                LongArgument("argument", false)
            )
        }

        assertEquals("Arguments named 'argument' is duplicated.", exception.message)
    }

    // TODO: test for parse
}
