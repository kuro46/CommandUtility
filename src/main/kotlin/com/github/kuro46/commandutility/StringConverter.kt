package com.github.kuro46.commandutility

import org.bukkit.command.CommandSender

interface StringConverter<T> {

    fun convert(sender: CommandSender, from: String): T?

    companion object {

        @JvmStatic
        fun <T> fromLambda(
            func: (CommandSender, String) -> T?
        ): StringConverter<T> {
            return object : StringConverter<T> {

                override fun convert(sender: CommandSender, from: String): T? {
                    return func(sender, from)
                }
            }
        }
    }
}
