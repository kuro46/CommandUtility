package xyz.shirokuro.commandutility;

import com.google.common.collect.ImmutableMap;

import java.util.Deque;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Comparator;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
}
