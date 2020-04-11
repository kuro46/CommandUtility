package xyz.shirokuro.commandutility;

import org.bukkit.command.CommandSender;

import java.util.Objects;

public final class CompletionData {

    private final CommandSender sender;
    private final CommandNode command;
    private final String name;
    private final String currentValue;

    public CompletionData(CommandSender sender, CommandNode command, String name, String currentValue) {
        this.sender = Objects.requireNonNull(sender);
        this.command = Objects.requireNonNull(command);
        this.name = Objects.requireNonNull(name);
        this.currentValue = Objects.requireNonNull(currentValue);
    }

    public CommandSender getSender() {
        return sender;
    }

    public CommandNode getCommand() {
        return command;
    }

    /**
     * Returns name of an argument that completing now.
     *
     * @return name of an argument
     */
    public String getName() {
        return name;
    }

    /**
     * Returns value that completing now.
     *
     * @return value
     */
    public String getCurrentValue() {
        return currentValue;
    }
}
