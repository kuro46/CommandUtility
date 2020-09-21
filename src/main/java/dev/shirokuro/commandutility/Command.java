package dev.shirokuro.commandutility;

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
    private final List<Parameter> parameters;
    private final CommandHandler handler;
    private final String description;

    public Command(final List<String> sections, final List<Parameter> parameters, final CommandHandler handler, final String description) {
        Objects.requireNonNull(sections, "sections");
        Objects.requireNonNull(parameters, "params");
        Objects.requireNonNull(handler, "handler");
        this.sections = ImmutableList.copyOf(sections);
        this.parameters = ImmutableList.copyOf(parameters);
        this.handler = handler;
        this.description = description;
        validateParameterOrder();
    }

    public static Command fromString(final CommandHandler handler, final String command, final String description) {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(handler, "handler");
        if (command.trim().isEmpty()) {
            throw new IllegalArgumentException("command is empty!");
        }
        final List<Parameter> parameters = new ArrayList<>();
        final List<String> sections = new ArrayList<>();
        for (final String part : Splitter.on(' ').split(command)) {
            final Optional<Parameter> optionalInfo = Parameter.fromString(part);
            if (optionalInfo.isPresent()) {
                parameters.add(optionalInfo.get());
            } else {
                if (!parameters.isEmpty()) {
                    throw new RuntimeException("Found command part after parameter part");
                }
                sections.add(part);
            }
        }
        return new Command(sections, parameters, handler, description);
    }

    private void validateParameterOrder() {
        for (int i = 1; i < parameters.size(); i++) {
            if (parameters.get(i).isRequired() && parameters.get(i - 1).isOptional()) {
                throw new IllegalArgumentException("Found a required parameter after optional parameters");
            }
        }
    }

    public Map<String, String> parseArgs(final List<String> args, final boolean ignoreNotEnough) throws ArgumentNotEnoughException {
        Objects.requireNonNull(args);
        if (parameters.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<String, String> result = new HashMap<>();
        final Iterator<String> iterator = args.iterator();
        for (final Parameter parameter : parameters) {
            if (!iterator.hasNext()) {
                if (ignoreNotEnough || parameter.isOptional()) {
                    break;
                } else {
                    throw new ArgumentNotEnoughException();
                }
            }
            result.put(parameter.getName(), iterator.next());
        }
        if (iterator.hasNext()) {
            result.compute(parameters.get(result.size() - 1).getName(), (key, value) -> {
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

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public List<String> getSections() {
        return sections;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public CommandHandler getHandler() {
        return handler;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Command)) {
            return false;
        }
        final Command command = (Command) other;
        return Objects.equals(sections, command.sections) &&
            Objects.equals(parameters, command.parameters) &&
            Objects.equals(handler, command.handler) &&
            Objects.equals(description, command.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sections, parameters, handler, description);
    }

    @Override
    public String toString() {
        return "Command{sections:'" + sections +
            "',parameters='" + parameters +
            "',handler='" + handler +
            "',description='" + description + "'}";
    }

    public static class ArgumentNotEnoughException extends Exception {
        public ArgumentNotEnoughException() {
            super();
        }
    }
}


