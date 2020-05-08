package dev.shirokuro.commandutility;

import com.google.common.base.Splitter;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CommandNodeTests {
    private static final CommandHandler NOOP_HANDLER = CommandNodeTests::noopHandler;

    private static void noopHandler(ExecutionData data) {
    }

    @Test
    public void fromStringTestWithAll() {
        final CommandNode cmd1 = createCommand("foo bar buz <hoge> [piyo]", "");
        final List<ArgumentInfo> infoList = Arrays.asList(ArgumentInfo.fromString("<hoge>").get(), ArgumentInfo.fromString("[piyo]").get());
        final CommandNode cmd2 = new CommandNode(new BranchNode("root").branch("foo").branch("bar"),
            "buz",
            new Command(
                Arrays.asList("foo", "bar", "buz"),
                infoList,
                NOOP_HANDLER,
                ""));
        assertEquals(cmd1, cmd2);
    }

    @Test
    public void fromStringTestWithSingle() {
        final CommandNode cmd1 = createCommand("foo", "");
        final CommandNode cmd2 = new CommandNode(new BranchNode("root"),
            "foo",
            new Command(
                Arrays.asList("foo"),
                Collections.emptyList(),
                NOOP_HANDLER, ""));
        assertEquals(cmd1, cmd2);
    }

    @Test
    public void illegalOrderArgsTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            createCommand("foo [hoge] <piyo>", "");
        });
    }

    @Test
    public void sectionsTestWithSome() {
        final Command cmd = createCommand("foo bar buz", "").getCommand();
        assertEquals("foo bar buz", String.join(" ", cmd.getSections()));
    }

    @Test
    public void parseArgsTestWithRequiredOnlyAndSomeNotEnoughInput() {
        final Command cmd = createCommand("foo <foo> <bar>", "").getCommand();
        assertThrows(Command.ArgumentNotEnoughException.class, () -> cmd.parseArgs(Collections.singletonList("val1"), false));
    }

    @Test
    public void parseArgsTestWithOptionalOnlyAndSomeNotEnoughInput() {
        final Command cmd = createCommand("foo [foo] [bar]", "").getCommand();
        assertDoesNotThrow(() -> {
            final Map<String, String> result = cmd.parseArgs(Collections.singletonList("val1"), false);
            assertEquals("val1", result.get("foo"));
        });
    }

    @Test
    public void parseArgsTestWithRequiredOnlyAndNoInput() {
        final Command cmd = createCommand("foo <foo> <bar>", "").getCommand();
        assertThrows(Command.ArgumentNotEnoughException.class, () -> cmd.parseArgs(Collections.emptyList(), false));
    }

    @Test
    public void parseArgsTestWithNoArgsAndSomeInput() {
        final Command cmd = createCommand("foo", "").getCommand();
        assertDoesNotThrow(() -> {
            cmd.parseArgs(Arrays.asList("val1", "val2"), false);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"foo <foo> <bar>", "foo [foo] [bar]"})
    public void parseArgsTestWithRequiredOnlyAndCorrect(final String command) {
        final Command cmd = createCommand(command, "").getCommand();
        assertDoesNotThrow(() -> {
            final Map<String, String> result = cmd.parseArgs(Arrays.asList("val1", "val2"), false);
            assertEquals("val1", result.get("foo"));
            assertEquals("val2", result.get("bar"));
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"foo <foo> <bar>", "foo [foo] [bar]"})
    public void parseArgsTestWithRequiredOnlyAndManyArgs(final String command) {
        final Command cmd = createCommand(command, "").getCommand();
        assertDoesNotThrow(() -> {
            final Map<String, String> result = cmd.parseArgs(Arrays.asList("val1", "val2", "val3"), false);
            assertEquals("val1", result.get("foo"));
            assertEquals("val2 val3", result.get("bar"));
        });
    }

    private CommandNode createCommand(final String command, final String description) {
        final CommandGroup group = new CommandGroup(new TestPlatform())
            .add(NOOP_HANDLER, command, description);
        return group.getRoot().walk(Splitter.on(' ').splitToList(command)).getCommand().get();
    }
}
