package dev.shirokuro.commandutility.platform;

import java.util.Map;
import dev.shirokuro.commandutility.CommandCompleter;

public interface Platform {

    void registerHandler(final String firstCommand, final PlatformCommandHandler handler) throws CommandNotExistsException;

    Map<String, CommandCompleter> defaultCompleters();
}
