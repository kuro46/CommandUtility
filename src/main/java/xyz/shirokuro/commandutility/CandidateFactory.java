package xyz.shirokuro.commandutility;

import java.util.List;

@FunctionalInterface
public interface CandidateFactory {

    List<String> create(final String currentValue);
}
