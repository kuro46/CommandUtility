package xyz.shirokuro.commandutility;

import java.util.Optional;

public interface Node {
    String getName();

    Optional<Node> getParent();
}
