package xyz.shirokuro.commandutility;

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
            infoList,
            "",
            NOOP_HANDLER);
        assertEquals(cmd1, cmd2);
    }

    @Test
    public void fromStringTestWithSingle() {
        final CommandNode cmd1 = createCommand("foo", "");
        final CommandNode cmd2 = new CommandNode(new BranchNode("root"),
            "foo",
            Collections.emptyList(),
            "",
            NOOP_HANDLER);
        assertEquals(cmd1, cmd2);
    }

    @Test
    public void sectionsTestWithSome() {
        final CommandNode cmd = createCommand("foo bar buz", "");
        assertEquals("foo bar buz", cmd.sections());
    }

    @Test
    public void getArgumentAtTestWithRequiredRange() {
        final CommandNode cmd = createCommand("foo <bar> [buz]", "");
        assertEquals(new ArgumentInfo("bar", null, true), cmd.getArgumentAt(0));
    }

    @Test
    public void getArgumentAtTestWithOptionalRange() {
        final CommandNode cmd = createCommand("foo <bar> [buz]", "");
        assertEquals(new ArgumentInfo("buz", null, false), cmd.getArgumentAt(1));
    }

    @Test
    public void parseArgsTestWithRequiredOnlyAndSomeNotEnoughInput() {
        final CommandNode cmd = createCommand("foo <foo> <bar>", "");
        assertThrows(CommandNode.ArgumentNotEnoughException.class, () -> cmd.parseArgs(Collections.singletonList("val1"), false));
    }

    @Test
    public void parseArgsTestWithOptionalOnlyAndSomeNotEnoughInput() {
        final CommandNode cmd = createCommand("foo [foo] [bar]", "");
        assertDoesNotThrow(() -> {
            final Map<String, String> result = cmd.parseArgs(Collections.singletonList("val1"), false);
            assertEquals("val1", result.get("foo"));
        });
    }

    @Test
    public void parseArgsTestWithRequiredOnlyAndNoInput() {
        final CommandNode cmd = createCommand("foo <foo> <bar>", "");
        assertThrows(CommandNode.ArgumentNotEnoughException.class, () -> cmd.parseArgs(Collections.emptyList(), false));
    }

    @Test
    public void parseArgsTestWithNoArgsAndSomeInput() {
        final CommandNode cmd = createCommand("foo", "");
        assertDoesNotThrow(() -> {
            cmd.parseArgs(Arrays.asList("val1", "val2"), false);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"foo <foo> <bar>", "foo [foo] [bar]"})
    public void parseArgsTestWithRequiredOnlyAndCorrect(final String command) {
        final CommandNode cmd = createCommand(command, "");
        assertDoesNotThrow(() -> {
            final Map<String, String> result = cmd.parseArgs(Arrays.asList("val1", "val2"), false);
            assertEquals("val1", result.get("foo"));
            assertEquals("val2", result.get("bar"));
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"foo <foo> <bar>", "foo [foo] [bar]"})
    public void parseArgsTestWithRequiredOnlyAndManyArgs(final String command) {
        final CommandNode cmd = createCommand(command, "");
        assertDoesNotThrow(() -> {
            final Map<String, String> result = cmd.parseArgs(Arrays.asList("val1", "val2", "val3"), false);
            assertEquals("val1", result.get("foo"));
            assertEquals("val2 val3", result.get("bar"));
        });
    }

    private CommandNode createCommand(final String command, final String description) {
        final CommandGroup group = new CommandGroup()
            .add(NOOP_HANDLER, command, description);
        return (CommandNode) group.findCommand(Splitter.on(' ').splitToList(command)).getNode();
    }
}
