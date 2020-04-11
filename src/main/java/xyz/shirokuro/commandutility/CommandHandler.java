package xyz.shirokuro.commandutility;

import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface CommandHandler {

    void execute(ExecutionData data);

    default List<String> complete(CompletionData data) {
        return Collections.emptyList();
    }
}
