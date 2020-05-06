package dev.shirokuro.commandutility;

import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public final class ArgumentInfo {
    private static final Pattern ARGUMENT_STR_VALIDATOR =
        Pattern.compile("^[<\\[](.*?)(?::(.*))?[>\\]]$");

    private final String name;
    private final String completerName;
    private final boolean required;

    public ArgumentInfo(final String name, final String completerName, final boolean required) {
        this.name = Objects.requireNonNull(name);
        this.completerName = completerName;
        this.required = required;
    }

    /**
     * Create ArgumentInfo from string.
     *
     * If {@code str} is a invalid format, It will returns empty optional.
     *
     * @return ArgumentInfo. If {@code str} is a invalid format, It will returns empty optional.
     */
    public static Optional<ArgumentInfo> fromString(final String str) {
        final Matcher m = ARGUMENT_STR_VALIDATOR.matcher(str);
        if (!m.find()) {
            return Optional.empty();
        }
        final String name = m.group(1);
        final String completerName = m.group(2);
        final boolean required = str.charAt(0) == '<';
        return Optional.of(new ArgumentInfo(name, completerName, required));
    }

    public String getName() {
        return name;
    }

    public Optional<String> getCompleterName() {
        return Optional.ofNullable(completerName);
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isOptional() {
        return !required;
    }

    public String toString(final boolean includeCompleterName) {
        final String inner = includeCompleterName
            ? name + ":" + completerName
            : name;
        if (required) {
            return "<" + inner + ">";
        } else {
            return "[" + inner + "]";
        }
    }

    @Override
    public String toString() {
        return toString(true);
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null || !(other instanceof ArgumentInfo)) {
            return false;
        }
        final ArgumentInfo casted = (ArgumentInfo) other;
        return required == casted.required
            && Objects.equals(name, casted.name)
            && Objects.equals(completerName, casted.completerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, completerName, required);
    }
}


