package com.github.kuro46.commandutility

import arrow.core.Either
import com.github.kuro46.commandutility.syntax.Argument
import com.github.kuro46.commandutility.syntax.CommandSyntax
import com.github.kuro46.commandutility.syntax.CommandSyntaxBuilder
import com.github.kuro46.commandutility.syntax.CommandSyntaxException
import com.github.kuro46.commandutility.syntax.LongArgument
import com.github.kuro46.commandutility.syntax.OptionalArgument
import com.github.kuro46.commandutility.syntax.ParsedArgs
import com.github.kuro46.commandutility.syntax.RequiredArgument
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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

    @Test
    fun `pass many arguments`() {
        val target = "aaa bbb ccc ddd".split(" ")
        fun test(vararg arguments: Argument) {
            val builder = CommandSyntaxBuilder()
            arguments.forEach { builder.addArgument(it) }
            val result = builder.build().parse(target)
            assertTrue(result.isLeft())
            val errorReason = (result as Either.Left).a
            assertTrue(errorReason == ParseErrorReason.TOO_MANY_ARGUMENTS)
        }
        fun testArgument(argumentFactory: (String) -> Argument) {
            test(argumentFactory("arg"))
            test(argumentFactory("arg"), argumentFactory("arg1"))
            test(argumentFactory("arg"), argumentFactory("arg1"), argumentFactory("arg2"))
        }

        testArgument { RequiredArgument(it) }
        testArgument { OptionalArgument(it) }
    }

    @Test
    fun `parse test`() {
        val expect = run {
            val syntax = CommandSyntaxBuilder().apply {
                addArgument(RequiredArgument("1"))
                addArgument(RequiredArgument("2"))
            }.build()
            expectResult { syntax.parse(it) }
        }

        expect(
            ParseResult.ARGUMENTS_NOT_ENOUGH,
            listOf("aaa")
        )

        expect(
            ParseResult.TOO_MANY_ARGUMENTS,
            listOf("aaa", "bbb", "ccc")
        )

        val parsedArgs = expect(
            ParseResult.SUCCESS,
            listOf("aaa", "bbb")
        )!!

        assertEquals("aaa", parsedArgs["1"])
        assertEquals("bbb", parsedArgs["2"])
    }

    private fun expectResult(
        parser: (List<String>) -> Either<ParseErrorReason, ParsedArgs>
    ): (ParseResult, List<String>) -> ParsedArgs? {
        return { expectedResult, argumentsToParse ->
            val either = parser(argumentsToParse)
            val actualResult = ParseResult.fromEither(either)
            assertEquals(expectedResult, actualResult)

            if (expectedResult != ParseResult.SUCCESS) {
                null
            } else {
                (either as Either.Right).b
            }
        }
    }

    private enum class ParseResult {

        ARGUMENTS_NOT_ENOUGH,
        TOO_MANY_ARGUMENTS,
        SUCCESS;

        companion object {

            fun fromEither(either: Either<ParseErrorReason, *>): ParseResult {
                return when (either) {
                    is Either.Left -> fromReason(either.a)
                    is Either.Right -> ParseResult.SUCCESS
                }
            }

            fun fromReason(reason: ParseErrorReason): ParseResult {
                return when (reason) {
                    ParseErrorReason.ARGUMENTS_NOT_ENOUGH -> ParseResult.ARGUMENTS_NOT_ENOUGH
                    ParseErrorReason.TOO_MANY_ARGUMENTS -> ParseResult.TOO_MANY_ARGUMENTS
                }
            }
        }
    }
}
