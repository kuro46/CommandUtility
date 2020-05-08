package dev.shirokuro.commandutility.platform;

public final class CommandNotExistsException extends Exception {

    private final String commandName;

    public CommandNotExistsException(final String commandName) {
        super("Command: '" + commandName + "' not exists");
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }
}
