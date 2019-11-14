package com.github.kuro46.commandutility;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import lombok.ToString;

/**
 * A representation of parsed args
 */
@ToString
public final class ParsedArgs {

    @NonNull
    private final ImmutableMap<String, String> map;

    public ParsedArgs(@NonNull final Map<String, String> map) {
        this.map = ImmutableMap.copyOf(map);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<String> get(final String name) {
        return Optional.ofNullable(map.get(name));
    }

    public String getOrNull(final String name) {
        return get(name).orElse(null);
    }

    public String getOrFail(final String name) {
        return get(name).orElseThrow(() -> {
            final String message = String.format("Argument not exists: %s", name);
            return new IllegalArgumentException(message);
        });
    }

    public ImmutableMap<String, String> asMap() {
        return map;
    }

    public static class Builder {

        private final ImmutableMap.Builder<String, String> args = ImmutableMap.builder();

        public Builder put(@NonNull final String name, @NonNull final String value) {
            args.put(name, value);
            return this;
        }

        public ParsedArgs build() {
            return new ParsedArgs(args.build());
        }
    }
}
