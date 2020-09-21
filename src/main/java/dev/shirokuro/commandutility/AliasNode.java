package dev.shirokuro.commandutility;

import lombok.NonNull;

import java.util.Optional;

public final class AliasNode implements Node {

    private final Node parent;
    private final String name;
    private final String aliasOf;

    public AliasNode(final @NonNull Node parent, final @NonNull String name, final @NonNull String aliasOf) {
        this.parent = parent;
        this.name = name;
        this.aliasOf = aliasOf;
    }

    public String getAliasOf() {
        return aliasOf;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<Node> getParent() {
        return Optional.of(parent);
    }
}
