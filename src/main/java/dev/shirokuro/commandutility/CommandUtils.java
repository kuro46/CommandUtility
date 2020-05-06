package dev.shirokuro.commandutility;

import java.util.Locale;
import java.util.List;
import java.util.Collection;
import java.util.Objects;
import java.util.function.*;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.shirokuro.commandutility.CommandExecutionException;
import dev.shirokuro.commandutility.CompletionData;

public final class CommandUtils {
    private CommandUtils() {
    }

    /**
     * Convert {@code source} to {@code Collection&lt;Strnig&gt;} with {@code mappingFunc} and
     * filter with {@code data.getCurrentValue}.
     */
    public static <T> List<String> convertToCandidates(
            final CompletionData data,
            final Collection<T> source,
            final Function<T, String> mappingFunc) {
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(mappingFunc, "mappingFunc");
        return source.stream()
            .map(mappingFunc)
            .filter(s -> s.startsWith(data.getCurrentValue()))
            .collect(Collectors.toList());
    }

    /**
     * Asserts {@code target} has {@code permission}.
     * If it hasn't, throws {@code CommandExecutionException}
     * with specified message (or default message if message is {@code null}).
     *
     * @param target target to assert
     * @param permission required permission
     * @param message message for {@code CommandExecutionException}. {@code null} for default
     */
    public static void assertPermission(
            final CommandSender target,
            final String permission,
            String message) throws CommandExecutionException {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(permission, "permission");
        message = message == null
            ? "You don't have permission."
            : message;
        if (!target.hasPermission(permission)) {
            throw new CommandExecutionException(message);
        }
    }

    /**
     * Converts {@code source} to {@code int}.
     * If failure, throws {@code CommandExecutionException} with specified message (or default if {@code null}).
     *
     * @param source conversion source
     * @param messageFunc {@code Function} for generate message via {@code source}
     * @return integer
     */
    public static int toInt(final String source, Function<String, String> messageFunc) throws CommandExecutionException {
        Objects.requireNonNull(source, "source");
        messageFunc = messageFunc == null
            ? s -> "'" + s + "' is a invalid number"
            : messageFunc;
        try {
            return Integer.parseInt(source);
        } catch (final NumberFormatException ignored) {
            throw new CommandExecutionException(messageFunc.apply(source));
        }
    }

    /**
     * Converts {@code source} to {@code double}.
     * If failure, throws {@code CommandExecutionException} with specified message (or default if {@code null}).
     *
     * @param source conversion source
     * @param messageFunc {@code Function} for generate message via {@code source}
     * @return double
     */
    public static double toDouble(final String source, Function<String, String> messageFunc) throws CommandExecutionException {
        Objects.requireNonNull(source, "source");
        messageFunc = messageFunc == null
            ? s -> "'" + s + "' is a invalid number"
            : messageFunc;
        try {
            return Double.parseDouble(source);
        } catch (final NumberFormatException ignored) {
            throw new CommandExecutionException(messageFunc.apply(source));
        }
    }

    public static Player toPlayer(final String source, Function<String, String> messageFunc) throws CommandExecutionException {
        Objects.requireNonNull(source, "source");
        messageFunc = messageFunc == null
            ? s -> "Cannot find player '" + s + "'"
            : messageFunc;
        @SuppressWarnings("deprecation")
        final Player player = Bukkit.getPlayer(source);
        if (player == null) {
            throw new CommandExecutionException(messageFunc.apply(source));
        }
        return player;
    }

    public static World toWorld(final String source, Function<String, String> messageFunc) throws CommandExecutionException {
        Objects.requireNonNull(source, "source");
        messageFunc = messageFunc == null
            ? s -> "Cannot find world '" + s +"'"
            : messageFunc;
        final World world = Bukkit.getWorld(source);
        if (world == null) {
            throw new CommandExecutionException(messageFunc.apply(source));
        }
        return world;
    }

    public static <T> T requireNonNull(final T original, final String message) throws CommandExecutionException {
        Objects.requireNonNull(message, "message");
        if (original == null) {
            throw new CommandExecutionException(message);
        }
        return original;
    }

    /**
     * Convert supplied string to {@code Enum}.
     *
     * @param clazz {@code Class} instance of Enum type
     * @param source string to convert
     * @param messageFunc for generate message from source string. {@code null} for default message.
     * @throws CommandExecutionException if failed to convert source to {@code Enum}
     * @return converted enum. this mustn't be null.
     */
    public static <E extends Enum<E>> E toEnum(
        final Class<E> clazz,
        final String source,
        Function<String, String> messageFunc) throws CommandExecutionException {

        Objects.requireNonNull(clazz, "clazz");
        Objects.requireNonNull(source, "source");
        messageFunc = messageFunc == null
            ? s -> "No enum constant named '" + s + "' exists"
            : messageFunc;
        try {
            return Enum.valueOf(clazz, source.toUpperCase(Locale.ENGLISH));
        } catch (final IllegalArgumentException ignored) {
            throw new CommandExecutionException(messageFunc.apply(source));
        }
    }
}
