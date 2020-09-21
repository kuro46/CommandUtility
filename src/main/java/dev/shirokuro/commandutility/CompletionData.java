package dev.shirokuro.commandutility;

import java.util.Objects;
import org.bukkit.command.CommandSender;

public final class CompletionData {

    private final CommandSender sender;
    private final CommandNode command;
    private final String parameterName;
    private final String currentValue;

    public CompletionData(CommandSender sender, CommandNode command, String parameterName, String currentValue) {
        this.sender = Objects.requireNonNull(sender);
        this.command = Objects.requireNonNull(command);
        this.parameterName = Objects.requireNonNull(parameterName);
        this.currentValue = Objects.requireNonNull(currentValue);
    }

    public CommandSender getSender() {
        return sender;
    }

    public CommandNode getCommand() {
        return command;
    }

    /**
     * Returns parameter name that completing now.
     *
     * @return parameter name
     */
    public String getParameterName() {
        return parameterName;
    }

    /**
     * @see #getParameterName()
     * @deprecated Please use {@link #getParameterName()} instead
     * @return parameter name
     */
    @Deprecated
    public String getName() {
        return parameterName;
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
