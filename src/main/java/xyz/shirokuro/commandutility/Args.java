package xyz.shirokuro.commandutility;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.UnmodifiableIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
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
        checkDuplication(args);
        checkOrder(args);
    }

    private static void checkDuplication(@NonNull final List<Arg> args) {
        final Set<Arg> set = new HashSet<>();
        for (final Arg arg : args) {
            if (!set.add(arg)) {
                final String message = String.format("Duplicated argument: %s", arg.getName());
                throw new IllegalArgumentException(message);
            }
        }
    }

    private static void checkOrder(@NonNull final List<Arg> args) {
        boolean prevIsRequired = true;
        for (final Arg arg : args) {
            final boolean currentIsRequired = arg.isRequired();
            if (currentIsRequired && !prevIsRequired) {
                throw new IllegalArgumentException("Invalid argument order");
            }
            prevIsRequired = currentIsRequired;
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
            if (args.isEmpty()) {
                return Optional.of(new ParsedArgs(Collections.emptyMap()));
            }
            final List<Pair<String, String>> preparsed = preparse(raw).orElse(null);
            if (preparsed == null) {
                return Optional.empty();
            }
            final Map<String, String> squashed = squash(preparsed);
            return Optional.of(new ParsedArgs(squashed));
        }

        private Optional<List<Pair<String, String>>> preparse(final List<String> parts) {
            final long reqArgCount = args.stream()
                .filter(Arg::isRequired)
                .count();
            if (reqArgCount > parts.size()) { // Parts isn't enough
                return Optional.empty();
            }
            final List<Pair<String, String>> preparsed = new ArrayList<>();
            for (final ListIterator<String> it = parts.listIterator(); it.hasNext();) {
                final int valueIndex = it.nextIndex();
                final String value = it.next();
                // Find a preferred argument for 'value'.
                // Fallback to the last argument if not exists.
                final Arg arg = Iterables.get(args, valueIndex, Iterables.getLast(args));
                // Add to 'preparsed'.
                preparsed.add(Pair.of(arg.getName(), value));
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

        public Builder required(@NonNull final String... names) {
            for (final String name : names) {
                args.add(new Arg(name, true));
            }
            return this;
        }

        public Builder optional(@NonNull final String... names) {
            for (final String name : names) {
                args.add(new Arg(name, false));
            }
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
