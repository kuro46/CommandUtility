package dev.shirokuro.commandutility.platform;

import java.util.*;
import org.bukkit.command.CommandSender;

public interface PlatformCommandHandler {

    void execute(final CommandSender dispatcher, final List<String> line);

    List<String> complete(final CommandSender dispatcher, final CompletingPosition completingPos, final List<String> currentLine);
}
