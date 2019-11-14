package com.github.kuro46.commandutility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class CandidateBuilder {

    private final Map<String, CandidateFactory> factories = new HashMap<>();

    public CandidateBuilder when(final String name, final CandidateFactory factory) {
        factories.put(name, factory);
        return this;
    }

    public List<String> build(final String name, final String currentValue) {
        final CandidateFactory factory = factories.get(name);
        return Optional.ofNullable(factory)
            .map(f -> f.create(currentValue))
            .orElse(null);
    }
}
