package xyz.shirokuro.commandutility;

import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface CommandHandler {

    void execute(CommandSender sender, CommandNode command, Map<String, String> args);

    default List<String> complete(CommandSender sender, CommandNode command, String name, String value) {
        return Collections.emptyList();
    }
}
