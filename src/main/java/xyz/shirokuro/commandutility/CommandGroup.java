package xyz.shirokuro.commandutility;

import com.google.common.base.Splitter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import xyz.shirokuro.commandutility.annotation.Completer;
import xyz.shirokuro.commandutility.annotation.Executor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public final class CommandGroup implements TabExecutor {

    private final BranchNode root = new BranchNode("root");
    private final String errorPrefix;

    public CommandGroup(final String errorPrefix) {
        this.errorPrefix = Objects.requireNonNull(errorPrefix);
    }

    public CommandGroup() {
        this("");
    }

    public CommandGroup add(final CommandHandler handler, final String command, final String description) {
        Objects.requireNonNull(command);
        Objects.requireNonNull(description);
        Objects.requireNonNull(handler);
        if (command.trim().isEmpty()) {
            throw new IllegalArgumentException("command is empty!");
        }
        final List<String> requiredNames = new ArrayList<>();
        final List<String> optionalNames = new ArrayList<>();
        final List<String> sections = new ArrayList<>();
        for (final String part : Splitter.on(' ').split(command)) {
            final char start = part.charAt(0);
            final char end = part.charAt(part.length() - 1);
            if (start == '<' && end == '>') {
                if (!optionalNames.isEmpty()) {
                    throw new RuntimeException("Found required argument after optional argument part");
                }
                final String name = part.substring(1, part.length() - 1);
                requiredNames.add(name);
            } else if (start == '[' && end == ']') {
                final String name = part.substring(1, part.length() - 1);
                optionalNames.add(name);
            } else if (!requiredNames.isEmpty() || !optionalNames.isEmpty()) {
                throw new RuntimeException("Found command part after argument part");
            } else {
                sections.add(part);
            }
        }
        if (sections.isEmpty()) {
            throw new IllegalArgumentException("No sections exists!");
        }
        if (!root.getChildren().containsKey(sections.get(0))) {
            // null-check for unit testing
            if (Bukkit.getServer() != null) {
                Bukkit.getPluginCommand(sections.get(0)).setExecutor(this);
            }
        }
        BranchNode current = root;
        for (int i = 0; i < sections.size(); i++) {
            final String section = sections.get(i);
            if (i < sections.size() - 1) {
                current = current.branch(section);
            } else {
                current.addChild(new CommandNode(current, section, requiredNames, optionalNames, description, handler));
            }
        }
        return this;
    }

    public CommandGroup addAll(final Object o) {
        final Map<String, Method> executors = new HashMap<>();
        final Map<String, Method> completers = new HashMap<>();
        final Map<String, String> descriptions = new HashMap<>();
        for (Method method : o.getClass().getMethods()) {
            final Executor executorAnnotation = method.getAnnotation(Executor.class);
            final Completer completerAnnotation = method.getAnnotation(Completer.class);
            if (executorAnnotation != null) {
                executors.put(executorAnnotation.command(), method);
                descriptions.put(executorAnnotation.command(), executorAnnotation.description());
            } else if (completerAnnotation != null) {
                completers.put(completerAnnotation.command(), method);
            }
        }
        executors.forEach((command, method) -> {
            final Method completer = completers.get(command);
            if (completer == null) {
                add((sender, command1, args) -> {
                    try {
                        method.invoke(o, sender, command1, args);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }, command, descriptions.get(command));
            } else {
                add(new CommandHandler() {
                    @Override
                    public void execute(CommandSender sender, CommandNode command, Map<String, String> args) {
                        try {
                            method.invoke(o, sender, command, args);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public List<String> complete(CommandSender sender, CommandNode command, String name, String value) {
                        try {
                            return (List<String>) completer.invoke(o, sender, command, name, value);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, command, descriptions.get(command));
            }
        });
        return this;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final List<String> normalized = new ArrayList<>();
        normalized.add(command.getName());
        normalized.addAll(Arrays.asList(args));
        final FindResult findResult = findCommand(normalized);
        if (findResult.getNode() instanceof BranchNode) {
            sender.sendMessage(errorPrefix + "Candidates: " + String.join(", ", ((BranchNode) findResult.getNode()).getChildren().keySet()));
            return true;
        }
        final CommandNode commandNode = (CommandNode) findResult.getNode();
        final Map<String, String> parsedArgs;
        try {
            parsedArgs = commandNode.parseArgs(findResult.getUnused(), false);
        } catch (CommandNode.ArgumentNotEnoughException e) {
            final String requiredStr = commandNode.getRequiredNames().stream()
                .map(s -> "<" + s + ">")
                .collect(Collectors.joining(" "));
            final String optionalStr = commandNode.getOptionalNames().stream()
                .map(s -> "[" + s + "]")
                .collect(Collectors.joining(" "));
            final StringJoiner joiner = new StringJoiner(" ");
            joiner.add(commandNode.sections());
            joiner.add(requiredStr);
            joiner.add(optionalStr);
            sender.sendMessage(errorPrefix + "Usage: /" + joiner.toString());
            return true;
        }
        commandNode.getHandler().execute(sender, commandNode, parsedArgs);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> normalized = new ArrayList<>();
        normalized.add(command.getName());
        for (String arg : args) {
            if (!arg.equals(" ")) {
                normalized.add(arg);
            }
        }
        final int completingIndex = normalized.size() - 1;
        final String completing = normalized.get(completingIndex);
        final FindResult findResult = findCommand(normalized.subList(0, normalized.size() - 1) /* Remove completing argument*/);
        if (findResult.getNode() instanceof BranchNode) {
            return ((BranchNode) findResult.getNode()).getChildren().keySet().stream()
                .filter(s -> s.startsWith(completing))
                .collect(Collectors.toList());
        } else {
            final CommandNode commandNode = (CommandNode) findResult.getNode();
            final String argumentName = commandNode.getArgumentAt(completingIndex, true);
            return commandNode.getHandler().complete(sender, commandNode, argumentName, completing);
        }
    }

    public FindResult findCommand(final List<String> list) {
        Node current = root;
        final List<String> unused = new ArrayList<>();
        for (String s : list) {
            if (current instanceof BranchNode) {
                current = ((BranchNode) current).getChildren().get(s);
            } else {
                unused.add(s);
            }
        }
        return new FindResult(current, unused);
    }

    public static final class FindResult {

        private final Node node;
        private final List<String> unused;

        public FindResult(final Node node, final List<String> unused) {
            this.node = node;
            this.unused = unused;
        }

        public Node getNode() {
            return node;
        }

        public List<String> getUnused() {
            return unused;
        }
    }
}
