package xyz.shirokuro.commandutility;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.*;

/**
 * <p>Class Command represents description of a command.</p>
 * <p>
 * <pre>
 * /foo bar baz &lt;buzz&gt; [fizz]
 *  \-section-/ \---args----/
 * </pre>
 * </p>
 */
public final class CommandNode implements Node {

    private final BranchNode parent;
    private final String name;
    private final List<ArgumentInfo> requiredArgs;
    private final List<ArgumentInfo> optionalArgs;
    private final String description;
    private final CommandHandler handler;

    public CommandNode(
        final BranchNode parent,
        final String name,
        final List<ArgumentInfo> requiredArgs,
        final List<ArgumentInfo> optionalArgs,
        final String description,
        final CommandHandler handler) {

        this.parent = parent;
        this.name = Objects.requireNonNull(name);
        this.requiredArgs = ImmutableList.copyOf(Objects.requireNonNull(requiredArgs));
        this.optionalArgs = ImmutableList.copyOf(Objects.requireNonNull(optionalArgs));
        this.description = Objects.requireNonNull(description);
        this.handler = Objects.requireNonNull(handler);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<Node> getParent() {
        return Optional.ofNullable(parent);
    }

    public List<ArgumentInfo> getRequiredArgs() {
        return requiredArgs;
    }

    public List<ArgumentInfo> getOptionalArgs() {
        return optionalArgs;
    }

    public String getDescription() {
        return description;
    }

    public String sections() {
        final Deque<String> deque = new ArrayDeque<>();
        Node current = this;
        while (current.getParent().isPresent()) {
            deque.addFirst(current.getName());
            current = current.getParent().get();
        }
        return String.join(" ", deque);
    }

    public ArgumentInfo getArgumentAt(int index, final boolean includeSection) {
        if (includeSection) {
            Node current = this;
            while (current.getParent().isPresent()) {
                current = current.getParent().get();
                index--;
            }
        }
        if (index < requiredArgs.size()) {
            return requiredArgs.get(index);
        } else {
            return optionalArgs.get(index - requiredArgs.size());
        }
    }

    public ArgumentInfo getArgumentAt(final int index) {
        return getArgumentAt(index, false);
    }

    public Map<String, String> parseArgs(final List<String> argumentsList, final boolean ignoreNotEnough) throws ArgumentNotEnoughException {
        Objects.requireNonNull(argumentsList);
        if (requiredArgs.isEmpty() && optionalArgs.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<String, String> result = new HashMap<>();
        final Iterator<String> iterator = argumentsList.iterator();
        for (ArgumentInfo requiredName : requiredArgs) {
            if (!iterator.hasNext()) {
                if (ignoreNotEnough) {
                    break;
                } else {
                    throw new ArgumentNotEnoughException();
                }
            }
            result.put(requiredName.getName(), iterator.next());
        }
        for (ArgumentInfo optionalName : optionalArgs) {
            if (!iterator.hasNext()) {
                break;
            }
            result.put(optionalName.getName(), iterator.next());
        }
        if (iterator.hasNext()) {
            result.compute(getArgumentAt(result.size() - 1), (key, value) -> {
                final StringJoiner joiner = new StringJoiner(" ");
                joiner.add(value);
                while (iterator.hasNext()) {
                    joiner.add(iterator.next());
                }
                return joiner.toString();
            });
        }
        return ImmutableMap.copyOf(result);
    }

    public CommandHandler getHandler() {
        return handler;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandNode command = (CommandNode) o;
        return Objects.equals(parent, command.parent) &&
            Objects.equals(name, command.name) &&
            Objects.equals(requiredArgs, command.requiredArgs) &&
            Objects.equals(optionalArgs, command.optionalArgs) &&
            Objects.equals(description, command.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, name, requiredArgs, optionalArgs, description);
    }

    @Override
    public String toString() {
        return "Command{" +
            "parent=" + parent +
            ", name='" + name + '\'' +
            ", requiredArgs=" + requiredArgs +
            ", optionalArgs=" + optionalArgs +
            ", description='" + description + '\'' +
            '}';
    }

    public static class ArgumentNotEnoughException extends Exception {
        public ArgumentNotEnoughException() {
            super();
        }
    }
}

