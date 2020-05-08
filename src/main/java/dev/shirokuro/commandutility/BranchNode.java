package dev.shirokuro.commandutility;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class BranchNode implements Node {

    private final Map<String, Node> children = new HashMap<>();
    private final String name;
    private final Node parent;

    public BranchNode(final String name, final Node parent) {
        this.name = Objects.requireNonNull(name);
        this.parent = parent;
    }

    public BranchNode(final String name) {
        this(name, null);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<Node> getParent() {
        return Optional.ofNullable(parent);
    }

    public Map<String, Node> getChildren() {
        return ImmutableMap.copyOf(children);
    }

    public void addChild(final Node node) {
        children.put(node.getName(), node);
    }

    /**
     * Walk the tree.
     *
     * @return List of CommandNode. Sorted.
     */
    public List<CommandNode> walkNodeTree() {
        final List<CommandNode> commands = new ArrayList<>();
        final Deque<BranchNode> branches = new ArrayDeque<>();
        branches.addFirst(this);
        while (true) {
            final BranchNode branch = branches.pollFirst();
            if (branch == null) {
                break;
            }
            branch.children.values().stream()
                    .sorted(Comparator.comparing(Node::getName))
                    .forEach(node -> {
                        if (node instanceof BranchNode) {
                            branches.addLast((BranchNode) node);
                        } else {
                            commands.add((CommandNode) node);
                        }
                    });
        }
        return commands;
    }

    /**
     * Find branch by specified name. If not exists, it creates new branch.
     *
     * @param name Name of branch
     * @return Branch
     */
    public BranchNode branch(final String name) {
        final Node node = children.computeIfAbsent(name, key -> new BranchNode(name, this));
        if (node instanceof BranchNode) {
            return (BranchNode) node;
        } else {
            throw new IllegalArgumentException("Node named '" + name + "' is not a branch.");
        }
    }

    public WalkResult walk(final String... paths) {
        return walk(Arrays.asList(paths));
    }

    public WalkResult walk(final List<String> paths) {
        final Deque<String> pathQueue = new ArrayDeque<>(paths);
        final List<BranchNode> branches = new ArrayList<>(paths.size());
        CommandNode commandNode = null;
        BranchNode current = this;
        while (true) {
            final String path = pathQueue.peekFirst();
            if (path == null) {
                break;
            }
            final Node child = current.getChildren().get(path);
            if (child == null) {
                break;
            }
            pathQueue.removeFirst();
            if (child instanceof BranchNode) {
                final BranchNode cb = (BranchNode) child;
                branches.add(cb);
                current = cb;
            } else {
                commandNode = (CommandNode) child;
                break;
            }
        }
        return new WalkResult(branches, pathQueue, commandNode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BranchNode branch = (BranchNode) o;
        return Objects.equals(name, branch.name) &&
            Objects.equals(parent, branch.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parent);
    }

    @Override
    public String toString() {
        return "Branch{" +
            "name='" + name + '\'' +
            ", parent=" + parent +
            '}';
    }

    public static final class WalkResult {

        private final List<BranchNode> branches;
        private final List<String> unreachablePaths;
        private final CommandNode command;

        public WalkResult(final Collection<BranchNode> branches, final Collection<String> unreachablePaths, final CommandNode command) {
            Objects.requireNonNull(branches, "branches");
            Objects.requireNonNull(unreachablePaths, "unreachablePaths");
            this.branches = ImmutableList.copyOf(branches);
            this.unreachablePaths = ImmutableList.copyOf(unreachablePaths);
            this.command = command;
        }

        /**
         * Returns reached branches.
         *
         * @return list of branches. First element is a first path
         */
        public List<BranchNode> getBranches() {
            return branches;
        }

        /**
         * Returns unreachable paths.
         *
         * @return list of paths. Last element is a last path
         */
        public List<String> getUnreachablePaths() {
            return unreachablePaths;
        }

        /**
         * Returns reached nodes.
         *
         * @return list of nodes. May be last element is {@code CommandNode}
         */
        public List<Node> getReachedNodes() {
            final ImmutableList.Builder<Node> result = ImmutableList.builder();
            result.addAll(branches);
            if (command != null) {
                result.add(command);
            }
            return result.build();
        }

        /**
         * Returns {@code CommandNode} if reached to it. Otherwise empty.
         *
         * @return {@code CommandNode} or empty
         */
        public Optional<CommandNode> getCommand() {
            return Optional.ofNullable(command);
        }

        @Override
        public int hashCode() {
            return Objects.hash(command, unreachablePaths, branches);
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || !(other instanceof WalkResult)) {
                return false;
            }
            final WalkResult co = (WalkResult) other;
            return Objects.equals(command, co.command) &&
                Objects.equals(unreachablePaths, co.unreachablePaths) &&
                Objects.equals(branches, co.branches);
        }

        @Override
        public String toString() {
            return "WalkResult{branches:'" + branches +
                "',unreachablePaths:'" + unreachablePaths +
                "',command:'" + command + "'}";
        }
    }
}
