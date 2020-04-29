package xyz.shirokuro.commandutility;

import xyz.shirokuro.commandutility.annotation.Executor;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
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
    public void walkTestWithAUnreachable() {
        final BranchNode root = new BranchNode("root", null);
        root.branch("foo").branch("bar");
        root.branch("foo").branch("buz");
        final BranchNode.WalkResult result = root.walk("foo", "bar", "buz");
        assertEquals(Collections.singletonList("buz"), result.getUnreachablePaths());
    }

    @Test
    public void walkTestWithSomeChildren() {
        final BranchNode root = new BranchNode("root", null);
        root.branch("foo").branch("bar").branch("hoge");
        root.branch("foo").branch("bar").branch("buz");
        final BranchNode.WalkResult result = root.walk("foo", "bar");
        assertEquals(root.branch("foo").branch("bar"), Iterables.getLast(result.getBranches()));
    }

    @Test
    public void walkTestWithNotEnough() {
        final BranchNode root = new BranchNode("root", null);
        root.branch("foo").branch("bar");
        final BranchNode.WalkResult result = root.walk("foo", "ba");
        assertEquals(root.branch("foo"), Iterables.getLast(result.getBranches()));
        assertEquals(Collections.singletonList("ba"), result.getUnreachablePaths());
    }

    @Test
    public void walkNodeTreeTest() {
        final CommandGroup group = new CommandGroup().addAll(new NoOpHandler());
        final List<CommandNode> nodes = group.getRoot().walkNodeTree();
        final String expect = "a\n" +
            "b a a\n" +
            "b a b\n" +
            "b b a\n" +
            "b b b";
        final String actual = nodes.stream()
            .map(CommandNode::getCommand)
            .map(cmd -> String.join(" ", cmd.getSections()))
            .collect(Collectors.joining("\n"));
        assertEquals(expect, actual);
    }

    private static final class NoOpHandler {
        @Executor("b a a")
        public void executeHPF(ExecutionData data) {
        }

        @Executor("b a b")
        public void executeHPB(ExecutionData data) {

        }

        @Executor("b b a")
        public void executeHFF(ExecutionData data) {

        }

        @Executor("b b b")
        public void executeHFB(ExecutionData data) {

        }

        @Executor("a")
        public void executeP(ExecutionData data) {

        }
    }
}
