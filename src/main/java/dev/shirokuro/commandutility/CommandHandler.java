package dev.shirokuro.commandutility;

import java.util.Collections;
import java.util.List;

public interface CommandHandler extends CommandCompleter {

    void execute(ExecutionData data) throws CommandExecutionException;

    @Override
    default List<String> complete(CompletionData data) {
        return Collections.emptyList();
    }
}
