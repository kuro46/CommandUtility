package com.github.kuro46.commandutility;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 * This class is a representation of the argument name.
 */
@EqualsAndHashCode
public final class ArgName implements Comparable<ArgName> {

    @NonNull
    private final String name;

    private ArgName(@NonNull final String name) {
        this.name = name.toLowerCase();
    }

    public static ArgName of(@NonNull final String name) {
        return new ArgName(name);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(@NonNull final ArgName other) {
        return this.name.compareTo(other.name);
    }
}
