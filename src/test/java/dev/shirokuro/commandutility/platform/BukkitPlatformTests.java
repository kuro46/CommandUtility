package dev.shirokuro.commandutility.platform;

import be.seeseemelk.mockbukkit.*;
import com.google.common.base.Splitter;
import dev.shirokuro.commandutility.TestPlugin;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.*;

public final class BukkitPlatformTests {

    private static BukkitPlatform platform;
    private static ServerMock server;
    private static HandlerImpl handler;

    @BeforeAll
    public static void setup() {
        server = MockBukkit.mock();
        TestPlugin.load();
        platform = new BukkitPlatform();
        try {
            handler = new HandlerImpl();
            platform.registerHandler("foo", handler);
        } catch (CommandNotExistsException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    public static void shutdown() {
        MockBukkit.unmock();
    }

    @ParameterizedTest
    @CsvSource({
            "foo bar",
            "foo"
    })
    public void test_execute(final String commandLine) {
        server.getCommandMap().dispatch(Bukkit.getConsoleSender(), commandLine);
        assertThat(handler.lastCommandLine).isEqualTo(Splitter.on(' ').splitToList(commandLine));
    }

    @ParameterizedTest
    @CsvSource({
            "'foo bar ',NEXT",
            "foo bar,CURRENT",
            "'foo   bar  ',NEXT",
    })
    public void test_complete(final String commandLine, final CompletingPosition completingPos) {
        server.getCommandMap().tabComplete(Bukkit.getConsoleSender(), commandLine);
        assertThat(handler.lastCompletingPos).isEqualTo(completingPos);
        assertThat(handler.lastCommandLine).isEqualTo(Splitter.on(' ').omitEmptyStrings().splitToList(commandLine));
    }

    private static final class HandlerImpl implements PlatformCommandHandler {

        private CompletingPosition lastCompletingPos;
        private List<String> lastCommandLine;

        @Override
        public void execute(final CommandSender sender, final List<String> commandLine) {
            lastCommandLine = commandLine;
        }

        @Override
        public List<String> complete(final CommandSender sender, final CompletingPosition completingPos, final List<String> commandLine) {
            lastCompletingPos = completingPos;
            lastCommandLine = commandLine;
            return Collections.emptyList();
        }
    }
}
