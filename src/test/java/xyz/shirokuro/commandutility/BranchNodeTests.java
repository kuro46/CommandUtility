package xyz.shirokuro.commandutility;

import xyz.shirokuro.commandutility.annotation.Executor;
import com.google.common.base.Splitter;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public final class BranchNodeTests {

    @Test
    public void walkNodeTreeTest() {
        final CommandGroup group = new CommandGroup().addAll(new NoOpHandler());
        final List<CommandNode> nodes = group.getRoot().walkNodeTree();
        final String expect = "a\n" +
            "b a a\n" +
            "b a b\n" +
            "b b a\n" +
            "b b b";
        final String actual = nodes.stream().map(CommandNode::sections).collect(Collectors.joining("\n"));
        assertEquals(expect, actual);
    }

    private static final class NoOpHandler {
        @Executor(command = "b a a", description = "")
        public void executeHPF(ExecutionData data) {
        }

        @Executor(command = "b a b", description = "")
        public void executeHPB(ExecutionData data) {

        }

        @Executor(command = "b b a", description = "")
        public void executeHFF(ExecutionData data) {

        }

        @Executor(command = "b b b", description = "")
        public void executeHFB(ExecutionData data) {

        }

        @Executor(command = "a", description = "")
        public void executeP(ExecutionData data) {

        }
    }
}
