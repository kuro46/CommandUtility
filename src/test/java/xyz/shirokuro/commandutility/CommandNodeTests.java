package xyz.shirokuro.commandutility;

import com.google.common.base.Splitter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CommandNodeTests {

    @Test
    public void fromStringTestWithAll() {
        final CommandNode cmd1 = createCommand("foo bar buz <hoge> [piyo]", "");
        final CommandNode cmd2 = new CommandNode(new BranchNode("root").branch("foo").branch("bar"),
            "buz",
            Collections.singletonList("hoge"),
            Collections.singletonList("piyo"),
            "");
        assertEquals(cmd1, cmd2);
    }

    @Test
    public void fromStringTestWithSingle() {
        final CommandNode cmd1 = createCommand("foo", "");
        final CommandNode cmd2 = new CommandNode(new BranchNode("root"),
            "foo",
            Collections.emptyList(),
            Collections.emptyList(),
            "");
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
        assertEquals("bar", cmd.getArgumentAt(0));
    }

    @Test
    public void getArgumentAtTestWithOptionalRange() {
        final CommandNode cmd = createCommand("foo <bar> [buz]", "");
        assertEquals("buz", cmd.getArgumentAt(1));
    }

    @Test
    public void parseArgsTestWithRequiredOnlyAndSomeNotEnoughInput() {
        final CommandNode cmd = createCommand("foo <foo> <bar>", "");
        assertThrows(IllegalArgumentException.class, () -> cmd.parseArgs(Collections.singletonList("val1")));
    }

    @Test
    public void parseArgsTestWithOptionalOnlyAndSomeNotEnoughInput() {
        final CommandNode cmd = createCommand("foo [foo] [bar]", "");
        assertDoesNotThrow(() -> {
            final Map<String, String> result = cmd.parseArgs(Collections.singletonList("val1"));
            assertEquals("val1", result.get("foo"));
        });
    }

    @Test
    public void parseArgsTestWithRequiredOnlyAndNoInput() {
        final CommandNode cmd = createCommand("foo <foo> <bar>", "");
        assertThrows(IllegalArgumentException.class, () -> cmd.parseArgs(Collections.emptyList()));
    }

    @Test
    public void parseArgsTestWithNoArgsAndSomeInput() {
        final CommandNode cmd = createCommand("foo", "");
        assertDoesNotThrow(() -> {
            cmd.parseArgs(Arrays.asList("val1", "val2"));
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"foo <foo> <bar>", "foo [foo] [bar]"})
    public void parseArgsTestWithRequiredOnlyAndCorrect(final String command) {
        final CommandNode cmd = createCommand(command, "");
        assertDoesNotThrow(() -> {
            final Map<String, String> result = cmd.parseArgs(Arrays.asList("val1", "val2"));
            assertEquals("val1", result.get("foo"));
            assertEquals("val2", result.get("bar"));
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"foo <foo> <bar>", "foo [foo] [bar]"})
    public void parseArgsTestWithRequiredOnlyAndManyArgs(final String command) {
        final CommandNode cmd = createCommand(command, "");
        assertDoesNotThrow(() -> {
            final Map<String, String> result = cmd.parseArgs(Arrays.asList("val1", "val2", "val3"));
            assertEquals("val1", result.get("foo"));
            assertEquals("val2 val3", result.get("bar"));
        });
    }

    private CommandNode createCommand(final String command, final String description) {
        final CommandGroup group = new CommandGroup()
            .add(command, description);
        return (CommandNode) group.findCommand(Splitter.on(' ').splitToList(command)).getNode();
    }
}
