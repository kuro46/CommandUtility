package dev.shirokuro.commandutility;

import java.util.List;

@FunctionalInterface
public interface CommandCompleter {

    List<String> complete(CompletionData data);
}


