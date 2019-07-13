package com.github.kuro46.commandutility.syntax

import com.github.kuro46.commandutility.ParseErrorReason

sealed class ParseResult<T> {

    data class Error<T>(val reason: ParseErrorReason) : ParseResult<T>()

    data class Success<T>(val value: T) : ParseResult<T>()
}
