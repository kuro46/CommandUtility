package dev.shirokuro.commandutility;

import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;
import dev.shirokuro.commandutility.annotation.Completer;
import dev.shirokuro.commandutility.annotation.Executor;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class CommandGroupTests {
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

        @Executor("foo bar")
        public void execute(ExecutionData data) {
        }

        @Completer("foo bar")
        public void complete(CompletionData data) {
        }
    }

    public static final class AnnotationExecutorOnly {

        @Executor("foo bar")
        public void execute(ExecutionData data) {
        }
    }

    public static final class AnnotationCompleterOnly {

        @Completer("foo bar")
        public void complete(CompletionData data) {
        }
    }

    public static final class AnnotationIncorrectParameters {

        @Executor("foo bar")
        public void execute() {
        }
    }
}
