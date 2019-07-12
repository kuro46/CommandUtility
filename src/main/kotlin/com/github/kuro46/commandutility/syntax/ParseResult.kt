package com.github.kuro46.commandutility.syntax

import com.github.kuro46.commandutility.ParseError

sealed class ParseResult {

    data class Error(val error: ParseError) : ParseResult()

    data class Success(val args: Map<String, String>) : ParseResult()
}
