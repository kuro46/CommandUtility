package com.github.kuro46.commandutility;

import java.util.List;

@FunctionalInterface
public interface CandidateFactory {

    List<String> create(final String currentValue);
}
