package xyz.shirokuro.commandutility;

import org.junit.jupiter.api.Test;
import xyz.shirokuro.commandutility.annotation.Completer;
import xyz.shirokuro.commandutility.annotation.Executor;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class CommandGroupTests {
    private static final CommandHandler NOOP_HANDLER = CommandGroupTests::noopHandler;

    private static void noopHandler(ExecutionData data) {
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

    @Test
    public void addAllTestExecutorAndCompleter() {
        assertDoesNotThrow(() -> new CommandGroup().addAll(new AnnotationExecutorAndCompleter()));
    }

    @Test
    public void addAllTestExecutorOnly() {
        assertDoesNotThrow(() -> new CommandGroup().addAll(new AnnotationExecutorOnly()));
    }

    @Test
    public void addAllTestCompleterOnly() {
        assertThrows(IllegalArgumentException.class, () -> new CommandGroup().addAll(new AnnotationCompleterOnly()));
    }

    @Test
    public void addAllTestIncorrectParameters() {
        assertThrows(IllegalArgumentException.class, () -> new CommandGroup().addAll(new AnnotationIncorrectParameters()));
    }

    public static final class AnnotationExecutorAndCompleter {

        @Executor(command = "foo bar", description = "TODO")
        public void execute(ExecutionData data) {
        }

        @Completer(command = "foo bar")
        public void complete(CompletionData data) {
        }
    }

    public static final class AnnotationExecutorOnly {

        @Executor(command = "foo bar", description = "TODO")
        public void execute(ExecutionData data) {
        }
    }

    public static final class AnnotationCompleterOnly {

        @Completer(command = "foo bar")
        public void complete(CompletionData data) {
        }
    }

    public static final class AnnotationIncorrectParameters {

        @Executor(command = "foo bar", description = "TODO")
        public void execute() {
        }
    }
}
