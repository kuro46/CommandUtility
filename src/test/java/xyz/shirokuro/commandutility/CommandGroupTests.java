package xyz.shirokuro.commandutility;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandGroupTests {

    @Test
    public void findCommandTestWithSomeCommands() {
        final CommandGroup.FindResult foundData = new CommandGroup()
            .add("foo bar", "")
            .add("foo buz", "")
            .findCommand(Arrays.asList("foo", "bar", "buz"));
        assertTrue(foundData.getNode() instanceof CommandNode);
        assertEquals(Collections.singletonList("buz"), foundData.getUnused());
    }

    @Test
    public void findCommandTestWithTooManyCandidates() {
        final CommandGroup.FindResult foundData = new CommandGroup()
            .add("foo bar hoge", "")
            .add("foo bar buz", "")
            .findCommand(Arrays.asList("foo", "bar"));
        assertTrue(foundData.getNode() instanceof BranchNode);
        assertEquals(2, ((BranchNode) foundData.getNode()).getChildren().size());
    }
}
