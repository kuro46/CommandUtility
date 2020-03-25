package xyz.shirokuro.commandutility;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode
public final class CommandName implements Comparable<CommandName> {

    private final String name;

    public CommandName(@NonNull final String name) {
        this.name = name.toLowerCase();
    }

    public static CommandName of(@NonNull final String name) {
        return new CommandName(name);
    }

    @Override
    public int compareTo(@NonNull final CommandName other) {
        return this.name.compareTo(other.name);
    }

    @Override
    public String toString() {
        return name;
    }
}
