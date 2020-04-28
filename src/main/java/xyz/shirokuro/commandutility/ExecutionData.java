package xyz.shirokuro.commandutility;

import com.google.common.collect.ImmutableMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;

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
    public Player getSenderAsPlayer() {
        return sender instanceof Player
            ? (Player) sender
            : null;
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
    public String get(final String argumentName) {
        return args.get(Objects.requireNonNull(argumentName));
    }
}
