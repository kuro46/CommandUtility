package xyz.shirokuro.commandutility.util;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.shirokuro.commandutility.CommandExecutionException;
import xyz.shirokuro.commandutility.CompletionData;

public final class CommandUtils {
    private CommandUtils() {
    }

    /**
     * Convert {@code source} to {@code List&lt;Strnig&gt;} with {@code mappingFunc} and
     * filter with {@code data.getCurrentValue}.
     */
    public static <T> List<String> convertToCandidates(
            final CompletionData data,
            final List<T> source,
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
     * If hasn't, throw {@code CommandExecutionException} with specified message.
     */
    public static void assertPermission(
            final CommandSender target,
            final String permission,
            final String message) throws CommandExecutionException {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(permission, "permission");
        if (!target.hasPermission(permission)) {
            throw new CommandExecutionException(message);
        }
    }

    /**
     * {@code assertPermission} with default message.
     * @see assertPermission
     */
    public static void assertPermission(
            final CommandSender target,
            final String permission) throws CommandExecutionException {
        assertPermission(target, permission, "You don't have permission.");
    }

    /**
     * {@code toInt} with default message.
     * @see toInt
     */
    public static int toInt(final String data) throws CommandExecutionException {
        return toInt(data, "'<data>' is a invalid number");
    }

    /**
     * Converts {@code data} to int. If failure, throw {@code CommandExecutionException} with specified message.
     *
     * @param message exception message. {@code &lt;data&gt;} will be replaced to {@code data}.
     */
    public static int toInt(final String data, final String message) throws CommandExecutionException {
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(message, "message");
        try {
            return Integer.parseInt(data);
        } catch (final NumberFormatException ignored) {
            throw new CommandExecutionException(message.replace("<data>", data));
        }
    }

    /**
     * {@code toDouble} with default message.
     * @see toDouble
     */
    public static double toDouble(final String data) throws CommandExecutionException {
        return toDouble(data, "'<data>' is a invalid number");
    }

    /**
     * Converts {@code data} to double. If failure, throw {@code CommandExecutionException} with specified message.
     *
     * @param message exception message. {@code &lt;data&gt;} will be replaced to {@code data}.
     */
    public static double toDouble(final String data, final String message) throws CommandExecutionException {
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(message, "message");
        try {
            return Double.parseDouble(data);
        } catch (final NumberFormatException ignored) {
            throw new CommandExecutionException(message.replace("<data>", data));
        }
    }

    /**
     * Converts {@code data} to {@code Player}. If failure, throw {@code CommandExecutionException} with specified message.
     *
     * @param message exception message. {@code &lt;data&gt;} will be replaced to {@code data}.
     */
    public static Player toPlayer(final String data, final String message) throws CommandExecutionException {
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(message, "message");
        @SuppressWarnings("deprecation")
        final Player player = Bukkit.getPlayer(data);
        if (player == null) {
            throw new CommandExecutionException(data.replace("<data>", data));
        }
        return player;
    }

    /**
     * {@code toPlayer} with default message.
     * @see toPlayer
     */
    public static Player toPlayer(final String data) throws CommandExecutionException {
        return toPlayer(data, "Cannot find player '<data>'");
    }
}
