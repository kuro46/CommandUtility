package dev.shirokuro.commandutility;

import dev.shirokuro.commandutility.annotation.Completer;
import dev.shirokuro.commandutility.annotation.Executor;
import dev.shirokuro.commandutility.platform.CompletingPosition;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class CommandGroupTests {
    @Test
    public void addAllTestExecutorAndCompleter() {
        assertDoesNotThrow(() -> new CommandGroup(new TestPlatform()).addAll(new AnnotationExecutorAndCompleter()));
    }

    @Test
    public void addAllTestExecutorOnly() {
        assertDoesNotThrow(() -> new CommandGroup(new TestPlatform()).addAll(new AnnotationExecutorOnly()));
    }

    @Test
    public void addAllTestCompleterOnly() {
        assertThrows(IllegalArgumentException.class, () -> new CommandGroup(new TestPlatform()).addAll(new AnnotationCompleterOnly()));
    }

    @Test
    public void addAllTestIncorrectParameters() {
        assertThrows(IllegalArgumentException.class, () -> new CommandGroup(new TestPlatform()).addAll(new AnnotationIncorrectParameters()));
    }

    @Test
    public void zeroParamTest() {
        final CommandGroup group = new CommandGroup(new TestPlatform());
        group.addAll(new ZeroParamTestHandler());
        group.complete(new CommandSenderImpl(), CompletingPosition.NEXT, Arrays.asList("foo", "bar", "hoge"));
    }

    @Test
    public void tryCompleteAfterIncorrectSubcommand() {
        final CommandGroup group = new CommandGroup(new TestPlatform());
        group.addAll(new FooBarAndFooBuzzHandler());
        final List<String> result =
                group.complete(new CommandSenderImpl(), CompletingPosition.NEXT, Arrays.asList("foo", "hoge"));
        assertTrue(result.isEmpty());
    }

    @Test
    public void tryCompleteSubcommandInTypingWord() {
        final CommandGroup group = new CommandGroup(new TestPlatform());
        group.addAll(new FooBarAndFooBuzzHandler());
        final List<String> result =
                group.complete(new CommandSenderImpl(), CompletingPosition.CURRENT, Arrays.asList("foo", "bu"));
        assertEquals(result, Collections.singletonList("buzz"));
    }

    @Test
    public void tryCompleteArgumentInTypingWord() {
        final CommandGroup group = new CommandGroup(new TestPlatform());
        group.addAll(new TryCompleteArgumentInTypingWordHandler(data -> assertEquals("ho", data.getCurrentValue())));
        group.complete(new CommandSenderImpl(), CompletingPosition.CURRENT, Arrays.asList("foo", "bar", "ho"));
    }

    public static final class TryCompleteArgumentInTypingWordHandler {
        private final Consumer<CompletionData> callback;

        public TryCompleteArgumentInTypingWordHandler(Consumer<CompletionData> callback) {
            this.callback = callback;
        }

        @Executor("foo bar <hoge>")
        public void fooBar(ExecutionData data) {
        }

        @Completer("foo bar <hoge>")
        public void completeFooBar(CompletionData data) {
            callback.accept(data);
        }
    }

    public static final class FooBarAndFooBuzzHandler {
        @Executor("foo bar")
        public void fooBar(ExecutionData data) {

        }

        @Executor("foo buzz")
        public void fooBuzz(ExecutionData data) {

        }
    }

    public static final class ZeroParamTestHandler {
        @Executor("foo bar")
        public void execute(ExecutionData data) {

        }
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
