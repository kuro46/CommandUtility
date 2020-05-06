package dev.shirokuro.commandutility;

import com.google.common.collect.ImmutableMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

public final class ExecutionData {

    private final CommandGroup group;
    private final CommandSender sender;
    private final CommandNode command;
    private final Map<String, String> args;

    public ExecutionData(CommandGroup group, CommandSender sender, CommandNode command, Map<String, String> args) {
        this.group = Objects.requireNonNull(group);
        this.sender = Objects.requireNonNull(sender);
        this.command = Objects.requireNonNull(command);
        this.args = ImmutableMap.copyOf(Objects.requireNonNull(args));
    }

    public CommandGroup getGroup() {
        return group;
    }

    public CommandSender getSender() {
        return sender;
    }

    /**
     * Returns {@code Player} instance if possible, otherwise {@code null}
     *
     * @return {@code Player} or {@code null}
     */
    public Player tryGetPlayer() {
        return sender instanceof Player
            ? (Player) sender
            : null;
    }

    /**
     * Returns {@code CommandSender} as a {@code Player} if possible.
     * Otherwise throw {@code CommandExecutionException} with specified message.
     *
     * @param message if {@code CommandSender} is not a instance of {@code Player}
     * @return {@code Player}
     */
    public Player getPlayer(final String message) throws CommandExecutionException {
        final Player player = tryGetPlayer();
        if (player == null) {
            throw new CommandExecutionException(message);
        }
        return player;
    }

    /**
     * Returns {@code CommandSender} as a {@code Player} if possible.
     * Otherwise throw {@code CommandExecutionException} with default message.<br>
     * Default message is <pre>You cannot perform this command from the console</pre>
     *
     * @return {@code Player}
     */
    public Player getPlayer() throws CommandExecutionException {
        return getPlayer("You cannot perform this command from the console");
    }

    public CommandNode getCommand() {
        return command;
    }

    /**
     * Returns args.
     *
     * @return args (immutable)
     */
    public Map<String, String> getArgs() {
        return args;
    }

    /**
     * Gets argument value by specified name.
     *
     * @param argumentName name of argument
     * @return value
     */
    public Optional<String> get(final String argumentName) {
        return Optional.ofNullable(args.get(Objects.requireNonNull(argumentName)));
    }

    public String getOrNull(final String argumentName) {
        return get(argumentName).orElse(null);
    }

    public String getOrFail(final String argumentName) {
        return get(argumentName).orElseThrow(() -> new NoSuchElementException("No value named " + argumentName + "exist"));
    }
}
