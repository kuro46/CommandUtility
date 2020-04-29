package xyz.shirokuro.commandutility;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

public final class Command {
    private final List<String> sections;
    private final List<ArgumentInfo> args;
    private final CommandHandler handler;
    private final String description;

    public Command(final List<String> sections, final List<ArgumentInfo> args, final CommandHandler handler, final String description) {
        Objects.requireNonNull(sections, "sections");
        Objects.requireNonNull(args, "args");
        Objects.requireNonNull(handler, "handler");
        this.sections = ImmutableList.copyOf(sections);
        this.args = ImmutableList.copyOf(args);
        this.handler = handler;
        this.description = description;
        validateArgsOrder();
    }

    public static Command fromString(final CommandHandler handler, final String command, final String description) {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(handler, "handler");
        if (command.trim().isEmpty()) {
            throw new IllegalArgumentException("command is empty!");
        }
        final List<ArgumentInfo> args = new ArrayList<>();
        final List<String> sections = new ArrayList<>();
        for (final String part : Splitter.on(' ').split(command)) {
            final Optional<ArgumentInfo> optionalInfo = ArgumentInfo.fromString(part);
            if (optionalInfo.isPresent()) {
                args.add(optionalInfo.get());
            } else {
                if (!args.isEmpty()) {
                    throw new RuntimeException("Found command part after argument part");
                }
                sections.add(part);
            }
        }
        return new Command(sections, args, handler, description);
    }

    private void validateArgsOrder() {
        for (int i = 1; i < args.size(); i++) {
            if (args.get(i).isRequired() && args.get(i - 1).isOptional()) {
                throw new IllegalArgumentException("Found required argument after optional argument");
            }
        }
    }

    public Map<String, String> parseArgs(final List<String> argumentsList, final boolean ignoreNotEnough) throws ArgumentNotEnoughException {
        Objects.requireNonNull(argumentsList);
        if (args.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<String, String> result = new HashMap<>();
        final Iterator<String> iterator = argumentsList.iterator();
        for (ArgumentInfo info : args) {
            if (!iterator.hasNext()) {
                if (ignoreNotEnough || info.isOptional()) {
                    break;
                } else {
                    throw new ArgumentNotEnoughException();
                }
            }
            result.put(info.getName(), iterator.next());
        }
        if (iterator.hasNext()) {
            result.compute(args.get(result.size() - 1).getName(), (key, value) -> {
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

    public String getDescription() {
        return description;
    }

    public List<String> getSections() {
        return sections;
    }

    public List<ArgumentInfo> getArgs() {
        return args;
    }

    public CommandHandler getHandler() {
        return handler;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || !(other instanceof Command)) {
            return false;
        }
        final Command command = (Command) other;
        return Objects.equals(sections, command.sections) &&
            Objects.equals(args, command.args) &&
            Objects.equals(handler, command.handler) &&
            Objects.equals(description, command.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sections, args, handler, description);
    }

    @Override
    public String toString() {
        return "Command{sections:'" + sections +
            "',args='" + args +
            "',handler='" + handler +
            "',description='" + description + "'}";
    }

    public static class ArgumentNotEnoughException extends Exception {
        public ArgumentNotEnoughException() {
            super();
        }
    }
}


