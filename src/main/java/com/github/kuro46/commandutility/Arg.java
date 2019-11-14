package com.github.kuro46.commandutility;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * A representation of an argument.<br>
 * This class only contains name and necessity. Not includes value.<br>
 *
 * @see ParsedArgs
 */
@AllArgsConstructor
public final class Arg {

    /**
     * Name of this argument
     */
    @Getter
    @NonNull
    private final String name;

    /**
     * Necessity of this argument. {@code true} if this argument is necessary
     */
    @Getter
    private final boolean required;

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Arg)) {
            return false;
        }
        final Arg castedOther = (Arg) other;
        return name.equals(castedOther.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        final char[] surroundWith = required
            ? new char[] {'<', '>'}
            : new char[] {'[', ']'};
        return surroundWith[0] + name + surroundWith[1];
    }
}
