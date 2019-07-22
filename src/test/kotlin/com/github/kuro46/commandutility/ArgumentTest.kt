package com.github.kuro46.commandutility

import arrow.core.Either
import com.github.kuro46.commandutility.syntax.LongArgument
import com.github.kuro46.commandutility.syntax.OptionalArgument
import com.github.kuro46.commandutility.syntax.RequiredArgument
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ArgumentTest {

    @Test
    fun `OptionalArgument test`() {
        val argument = OptionalArgument("arg1")
        val target = listOf("aaa")

        run {
            val either = argument.parse(0, target)
            assertTrue(either.isRight())
            val (value, indexes) = (either as Either.Right).b!!

            assertEquals("aaa", value)
            assertEquals(0..0, indexes)
        }

        run {
            val either = argument.parse(1, target)
            assertTrue(either.isRight())
            assertEquals(null, (either as Either.Right).b)
        }
    }

    @Test
    fun `RequiredArgument test`() {
        val argument = RequiredArgument("arg1")
        val target = listOf("aaa")

        run {
            val either = argument.parse(0, target)
            assertTrue(either.isRight())
            val (value, indexes) = (either as Either.Right).b!!

            assertEquals("aaa", value)
            assertEquals(0..0, indexes)
        }

        run {
            val either = argument.parse(1, target)
            assertTrue(either.isLeft())
            assertEquals(ParseErrorReason.ARGUMENTS_NOT_ENOUGH, (either as Either.Left).a)
        }
    }

    @Test
    fun `Required LongArgument test`() {
        val argument = LongArgument("arg1", true)
        val target = listOf("aaa", "bbb")

        run {
            val either = argument.parse(0, target)
            assertTrue(either.isRight())
            val (value, indexes) = (either as Either.Right).b!!

            assertEquals("aaa bbb", value)
            assertEquals(0..1, indexes)
        }

        run {
            val either = argument.parse(1, target)
            assertTrue(either.isRight())
            val (value, indexes) = (either as Either.Right).b!!

            assertEquals("bbb", value)
            assertEquals(1..1, indexes)
        }

        run {
            val either = argument.parse(2, target)
            assertTrue(either.isLeft())
            assertEquals(ParseErrorReason.ARGUMENTS_NOT_ENOUGH, (either as Either.Left).a)
        }
    }

    @Test
    fun `Optional LongArgument test`() {
        val argument = LongArgument("arg1", false)
        val target = listOf("aaa", "bbb")

        run {
            val either = argument.parse(0, target)
            assertTrue(either.isRight())
            val (value, indexes) = (either as Either.Right).b!!

            assertEquals("aaa bbb", value)
            assertEquals(0..1, indexes)
        }

        run {
            val either = argument.parse(1, target)
            assertTrue(either.isRight())
            val (value, indexes) = (either as Either.Right).b!!

            assertEquals("bbb", value)
            assertEquals(1..1, indexes)
        }

        run {
            val either = argument.parse(2, target)
            assertTrue(either.isRight())
            assertEquals(null, (either as Either.Right).b)
        }
    }
}
