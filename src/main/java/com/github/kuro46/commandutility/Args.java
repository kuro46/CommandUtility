package com.github.kuro46.commandutility;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.UnmodifiableIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;

/**
 * List of arguments.
 */
public final class Args implements Iterable<Arg> {

    private static final Args EMPTY = new Args(ImmutableList.of());

    private final ImmutableList<Arg> args;

    public Args(@NonNull final List<Arg> args) {
        Args.validate(args);

        this.args = ImmutableList.copyOf(args);
    }

    public static Args empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    private static void validate(final List<Arg> args) {
        // Checks duplication
        final Set<String> names = new HashSet<>();
        for (final Arg arg : args) {
            final String name = arg.getName();
            if (!names.add(name)) {
                final String message = String.format("Duplicated argument: %s", name);
                throw new IllegalArgumentException(message);
            }
        }
        // Validates argument order
        boolean prevIsRequired = true;
        for (final Arg arg : args) {
            final boolean currentIsRequired = arg.isRequired();
            if (currentIsRequired && !prevIsRequired) {
                throw new IllegalArgumentException("Invalid argument order");
            }
            prevIsRequired = arg.isRequired();
        }
    }

    public ImmutableList<Arg> asList() {
        return args;
    }

    @Override
    public UnmodifiableIterator<Arg> iterator() {
        return args.iterator();
    }

    public Optional<ParsedArgs> parse(final List<String> raw) {
        return new Parser().parse(raw);
    }

    @Override
    public String toString() {
        return args.stream()
            .map(arg -> String.format("%s", arg))
            .collect(Collectors.joining(" "));
    }

    private class Parser {

        public Optional<ParsedArgs> parse(final List<String> raw) {
            if (args.isEmpty()) return Optional.of(new ParsedArgs(Collections.emptyMap()));
            final List<Pair<String, String>> preparsed = preparse(raw).orElse(null);
            if (preparsed == null) {
                return Optional.empty();
            }
            final Map<String, String> squashed = squash(preparsed);

            return Optional.of(new ParsedArgs(squashed));
        }

        private Optional<List<Pair<String, String>>> preparse(final List<String> parts) {
            final List<Pair<String, String>> preparsed = new ArrayList<>();
            int index = 0;
            Arg prevArg = null;
            while (true) {
                if (Iterables.get(args, index, null) == null
                        && Iterables.get(parts, index, null) == null) {
                    break;
                }
                final Arg currentArg = Iterables.get(args, index, prevArg);

                final String value = Iterables.get(parts, index, null);
                if (value == null && currentArg.isRequired()) {
                    return Optional.empty();
                }

                if (value != null) {
                    preparsed.add(Pair.of(currentArg.getName(), value));
                }

                // finalize
                index++;
                prevArg = currentArg;
            }

            return Optional.of(preparsed);
        }

        private Map<String, String> squash(final List<Pair<String, String>> preparsed) {
            final Map<String, String> squashed = new HashMap<>();

            for (final Pair<String, String> pair : preparsed) {
                final String name = pair.left;
                final String value = pair.right;
                if (!squashed.containsKey(name)) {
                    squashed.put(name, value);
                } else {
                    final String appended = squashed.get(name) + " " + value;
                    squashed.put(name, appended);
                }
            }

            return squashed;
        }
    }

    public static class Builder {

        private final ImmutableList.Builder<Arg> args = ImmutableList.builder();

        public Builder add(@NonNull final Arg arg) {
            args.add(arg);
            return this;
        }

        public Builder add(@NonNull final String name, final boolean required) {
            return add(new Arg(name, required));
        }

        public Builder required(@NonNull final String name) {
            return add(new Arg(name, true));
        }

        public Builder optional(@NonNull final String name) {
            return add(new Arg(name, false));
        }

        public Builder optionalArgs(@NonNull final String... names) {
            for (String name : names) optional(name);
            return this;
        }

        public Builder requiredArgs(@NonNull final String... names) {
            for (String name : names) required(name);
            return this;
        }

        public Args build() {
            return new Args(args.build());
        }
    }

    private static final class Pair<L, R> {

        final L left;
        final R right;

        Pair(final L left, final R right) {
            this.left = left;
            this.right = right;
        }

        static <L, R> Pair<L, R> of(final L left, final R right) {
            return new Pair<>(left, right);
        }
    }
}
