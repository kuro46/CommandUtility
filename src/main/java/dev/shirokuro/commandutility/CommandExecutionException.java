package dev.shirokuro.commandutility;

public final class CommandExecutionException extends Exception {

    public CommandExecutionException() {
        this(null);
    }

    public CommandExecutionException(final String message) {
        super(message);
    }
}
