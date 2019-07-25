package com.github.kuro46.commandutility

import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class StringConverters {

    private val converters = ConcurrentHashMap<Class<*>, StringConverter<*>>()

    inline fun <reified T> registerConverter(converter: StringConverter<T>) {
        registerConverter(T::class.java, converter)
    }

    fun <T> registerConverter(clazz: Class<T>, converter: StringConverter<T>) {
        converters[clazz] = converter
    }

    inline fun <reified T> convert(sender: CommandSender, from: String): T? {
        return convert(T::class.java, sender, from)
    }

    fun <T> convert(clazz: Class<T>, sender: CommandSender, from: String): T? {
        val converter = converters[clazz]
            ?: throw IllegalArgumentException(
                "No converter found for class '$clazz'"
            )

        return converter.convert(sender, from)
            ?.let { clazz.cast(it) }
    }

    /**
     * Registers default converters.
     * Currently, Int, Double, Float, Player, and World are supported.
     */
    fun registerDefaults() {

        registerConverter(
            StringConverter.fromLambda<Int> { sender, from ->
                val intOrNull = from.toIntOrNull()
                if (intOrNull == null) {
                    sender.sendMessage("'$from' is not a valid number")
                }
                intOrNull
            }
        )
        registerConverter(
            StringConverter.fromLambda<Double> { sender, from ->
                val doubleOrNull = from.toDoubleOrNull()
                if (doubleOrNull == null) {
                    sender.sendMessage("'$from' is not a valid number")
                }
                doubleOrNull
            }
        )
        registerConverter(
            StringConverter.fromLambda<Float> { sender, from ->
                val floatOrNull = from.toFloatOrNull()
                if (floatOrNull == null) {
                    sender.sendMessage("'$from' is not a valid number")
                }
                floatOrNull
            }
        )
        registerConverter(
            StringConverter.fromLambda<Player> { sender, from ->
                @Suppress("deprecation")
                val playerOrNull = Bukkit.getPlayer(from)
                if (playerOrNull == null) {
                    sender.sendMessage("Player '$from' is not online")
                }
                playerOrNull
            }
        )
        registerConverter(
            StringConverter.fromLambda<World> { sender, from ->
                val worldOrNull = Bukkit.getWorld(from)
                if (worldOrNull == null) {
                    sender.sendMessage("World '$from' not found")
                }
                worldOrNull
            }
        )
    }
}
