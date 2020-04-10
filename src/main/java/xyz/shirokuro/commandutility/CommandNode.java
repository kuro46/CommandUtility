package xyz.shirokuro.commandutility;

import com.google.common.collect.ImmutableList;

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
    private final List<String> requiredNames;
    private final List<String> optionalNames;
    private final String description;
    private final CommandHandler handler;

    public CommandNode(
        final BranchNode parent,
        final String name,
        final List<String> requiredNames,
        final List<String> optionalNames,
        final String description,
        final CommandHandler handler) {

        this.parent = parent;
        this.name = Objects.requireNonNull(name);
        this.requiredNames = ImmutableList.copyOf(Objects.requireNonNull(requiredNames));
        this.optionalNames = ImmutableList.copyOf(Objects.requireNonNull(optionalNames));
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

    public List<String> getRequiredNames() {
        return requiredNames;
    }

    public List<String> getOptionalNames() {
        return optionalNames;
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

    public String getArgumentAt(int index, final boolean includeSection) {
        if (includeSection) {
            Node current = this;
            while (current.getParent().isPresent()) {
                current = current.getParent().get();
                index--;
            }
        }
        if (index < requiredNames.size()) {
            return requiredNames.get(index);
        } else {
            return optionalNames.get(index - requiredNames.size());
        }
    }

    public String getArgumentAt(final int index) {
        return getArgumentAt(index, false);
    }

    public Map<String, String> parseArgs(final List<String> argumentsList, final boolean ignoreNotEnough) throws ArgumentNotEnoughException {
        Objects.requireNonNull(argumentsList);
        if (requiredNames.isEmpty() && optionalNames.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<String, String> result = new HashMap<>();
        final Iterator<String> iterator = argumentsList.iterator();
        for (String requiredName : requiredNames) {
            if (!iterator.hasNext()) {
                if (ignoreNotEnough) {
                    break;
                } else {
                    throw new ArgumentNotEnoughException();
                }
            }
            result.put(requiredName, iterator.next());
        }
        for (String optionalName : optionalNames) {
            if (!iterator.hasNext()) {
                break;
            }
            result.put(optionalName, iterator.next());
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
        return result;
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
            Objects.equals(requiredNames, command.requiredNames) &&
            Objects.equals(optionalNames, command.optionalNames) &&
            Objects.equals(description, command.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, name, requiredNames, optionalNames, description);
    }

    @Override
    public String toString() {
        return "Command{" +
            "parent=" + parent +
            ", name='" + name + '\'' +
            ", requiredNames=" + requiredNames +
            ", optionalNames=" + optionalNames +
            ", description='" + description + '\'' +
            '}';
    }

    public static class ArgumentNotEnoughException extends Exception {
        public ArgumentNotEnoughException() {
            super();
        }
    }
}

