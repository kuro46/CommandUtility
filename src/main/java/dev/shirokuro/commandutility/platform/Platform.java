package dev.shirokuro.commandutility.platform;

import dev.shirokuro.commandutility.CommandCompleter;
import java.util.Map;

public interface Platform {

    void registerHandler(final String firstCommand, final PlatformCommandHandler handler) throws CommandNotExistsException;

    Map<String, CommandCompleter> defaultCompleters();
}
