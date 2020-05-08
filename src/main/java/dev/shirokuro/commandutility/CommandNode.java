package dev.shirokuro.commandutility;

import java.util.Objects;
import java.util.Optional;

public final class CommandNode implements Node {

    private final BranchNode parent;
    private final String name;
    private final Command command;

    public CommandNode(
            final BranchNode parent, // nullable
            final String name,
            final Command command) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(command, "command");
        this.parent = parent;
        this.name = name;
        this.command = command;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<Node> getParent() {
        return Optional.ofNullable(parent);
    }

    public Command getCommand() {
        return command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CommandNode commandNode = (CommandNode) o;
        return Objects.equals(parent, commandNode.parent) &&
            Objects.equals(name, commandNode.name) &&
            Objects.equals(command, commandNode.command);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, name, command);
    }

    @Override
    public String toString() {
        return "CommandNode{" +
            "parent=" + parent +
            ", name='" + name + '\'' +
            ", command='" + command + '\'' +
            '}';
    }
}

