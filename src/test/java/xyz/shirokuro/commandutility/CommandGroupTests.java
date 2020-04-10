package xyz.shirokuro.commandutility;

import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandGroupTests {
    private static final CommandHandler NOOP_HANDLER = CommandGroupTests::noopHandler;

    private static void noopHandler(CommandSender sender, CommandNode command, Map<String, String> args) {
    }

    @Test
    public void findCommandTestWithSomeCommands() {
        final CommandGroup.FindResult foundData = new CommandGroup()
            .add(NOOP_HANDLER, "foo bar", "")
            .add(NOOP_HANDLER, "foo buz", "")
            .findCommand(Arrays.asList("foo", "bar", "buz"));
        assertTrue(foundData.getNode() instanceof CommandNode);
        assertEquals(Collections.singletonList("buz"), foundData.getUnused());
    }

    @Test
    public void findCommandTestWithTooManyCandidates() {
        final CommandGroup.FindResult foundData = new CommandGroup()
            .add(NOOP_HANDLER, "foo bar hoge", "")
            .add(NOOP_HANDLER, "foo bar buz", "")
            .findCommand(Arrays.asList("foo", "bar"));
        assertTrue(foundData.getNode() instanceof BranchNode);
        assertEquals(2, ((BranchNode) foundData.getNode()).getChildren().size());
    }
}
