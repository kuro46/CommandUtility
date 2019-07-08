package com.github.kuro46.commandutility.syntax

/**
 * A CommandSyntaxException is thrown if a syntax of command is broken.
 *
 * @constructor
 *
 * @param message The detail message.
 */
class CommandSyntaxException(message: String) : Exception(message)
