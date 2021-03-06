package dev.shirokuro.commandutility;

import dev.shirokuro.commandutility.platform.Platform;
import dev.shirokuro.commandutility.platform.PlatformCommandHandler;
import java.util.Collections;
import java.util.Map;

public final class TestPlatform implements Platform {

    @Override
    public void registerHandler(final String command, final PlatformCommandHandler handler) {

    }

    @Override
    public Map<String, CommandCompleter> defaultCompleters() {
        return Collections.emptyMap();
    }
}
