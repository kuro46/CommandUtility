package xyz.shirokuro.commandutility;

import java.util.Objects;
import java.util.Optional;

public final class ArgumentInfo {
    private final String name;
    private final String completerName;

    public ArgumentInfo(final String name, final String completerName) {
        this.name = Objects.requireNonNull(name);
        this.completerName = completerName;
    }

    public static ArgumentInfo fromString(final String str) {
        final int index = str.indexOf(':');
        if (index == -1) {
            return new ArgumentInfo(str, null);
        }
        final String name = str.substring(0, index);
        final String completerName;
        if (index == str.length() - 1) {
            completerName = "";
        } else {
            completerName = str.substring(index + 1, str.length());
        }
        return new ArgumentInfo(name, completerName);
    }

    public String getName() {
        return name;
    }

    public Optional<String> getCompleterName() {
        return Optional.ofNullable(completerName);
    }

    @Override
    public String toString() {
        return name + ":" + completerName;
    }
}


