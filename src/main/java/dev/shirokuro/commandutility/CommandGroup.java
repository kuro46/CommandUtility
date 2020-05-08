package dev.shirokuro.commandutility;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.ChatColor;
import dev.shirokuro.commandutility.annotation.*;
import dev.shirokuro.commandutility.platform.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public final class CommandGroup implements PlatformCommandHandler {

    private final Map<String, CommandCompleter> completerMap = new HashMap<>();
    private final BranchNode root = new BranchNode("root");
    private final Platform platform;
    private final String errorPrefix;

    public CommandGroup(final Platform platform, final String errorPrefix) {
        this.errorPrefix = Objects.requireNonNull(errorPrefix, "errorPrefix");
        this.platform = Objects.requireNonNull(platform, "platform");
        addDefaultCompleters();
    }

    public CommandGroup(final Platform platform) {
        this(platform, "");
    }

    public static CommandGroup initBukkit(final String errorPrefix) {
        return new CommandGroup(new BukkitPlatform(), errorPrefix);
    }

    public BranchNode getRoot() {
        return root;
    }

    private void addDefaultCompleters() {
        completerMap.putAll(platform.defaultCompleters());
    }

    public CommandGroup addCompleter(final String argumentName, final CommandCompleter completer) {
        Objects.requireNonNull(argumentName);
        Objects.requireNonNull(completer);
        completerMap.put(argumentName, completer);
        return this;
    }

    public CommandGroup add(final Command command) {
        Objects.requireNonNull(command, "command");
        final String firstSection = command.getSections().get(0);
        final boolean firstTime = !root.getChildren().containsKey(firstSection);
        // Insert Command to tree
        BranchNode current = root;
        final Iterator<String> sectionIter = command.getSections().iterator();
        while (sectionIter.hasNext()) {
            final String section = sectionIter.next();
            if (sectionIter.hasNext()) {
                current = current.branch(section);
            } else {
                current.addChild(new CommandNode(current, section, command));
            }
        }
        // First time process
        if (firstTime) {
            // Register handler to platform
            try {
                platform.registerHandler(firstSection, this);
            } catch (final CommandNotExistsException e) {
                throw new IllegalArgumentException("Command: " + firstSection +
                    " is not registered by any plugins", e);
            }
        }
        return this;
    }

    public CommandGroup add(final CommandHandler handler, final String command, final String description) {
        final Command result = Command.fromString(handler, command, description);
        if (result.getSections().isEmpty()) {
            throw new IllegalArgumentException("Section is empty!");
        }
        add(result);
        return this;
    }

    public CommandGroup addAll(final Object o) {
        final Map<String, ReflectedHandlerInfo> handlerInfoMap = new HashMap<>();
        // find all annotated methods
        for (Method method : o.getClass().getDeclaredMethods()) {
            final Executor executorAnnotation = method.getAnnotation(Executor.class);
            final Completer completerAnnotation = method.getAnnotation(Completer.class);
            if (executorAnnotation != null) {
                ReflectionUtils.assertPublic(method);
                if (!ReflectionUtils.equalsMethodParams(method, ExecutionData.class)) {
                    throw new IllegalArgumentException("Method: " +
                            ReflectionUtils.methodInfo(method) +
                            " is annotated @Executor, but method parameters are incorrect!");
                }
                final ReflectedHandlerInfo info =
                    handlerInfoMap.computeIfAbsent(executorAnnotation.value(), s -> new ReflectedHandlerInfo());
                info.executor = method;
                final Description description = method.getAnnotation(Description.class);
                if (description != null) {
                    info.description = description.value();
                }
            } else if (completerAnnotation != null) {
                ReflectionUtils.assertPublic(method);
                if (!ReflectionUtils.equalsMethodParams(method, CompletionData.class)) {
                    throw new IllegalArgumentException("Method: " +
                            ReflectionUtils.methodInfo(method) +
                            " is annotated @Completer, but method parameters are incorrect!");
                }
                final ReflectedHandlerInfo info =
                    handlerInfoMap.computeIfAbsent(completerAnnotation.value(), s -> new ReflectedHandlerInfo());
                info.completer = method;
            }
        }
        // add found executor/completers
        handlerInfoMap.forEach((command, info) -> {
            final Method completer = info.completer;
            final Method executor = info.executor;
            if (completer != null && executor == null) {
                throw new IllegalArgumentException("Cannot find executor for '" + command + "'");
            }
            final ReflectedCommandHandler handler = new ReflectedCommandHandler(o, executor, completer);
            add(handler, command, info.description);
        });
        return this;
    }

    private static final class ReflectedHandlerInfo {
        private Method executor;
        private Method completer;
        private String description;
    }

    @Override
    public void execute(final CommandSender sender, final List<String> commandLine) {
        final BranchNode.WalkResult findResult = root.walk(commandLine);
        if (!findResult.getCommand().isPresent()) {
            sender.sendMessage(errorPrefix + "Candidates: " +
                    String.join(", ", Iterables.getLast(findResult.getBranches()).getChildren().keySet()));
            return;
        }
        final CommandNode commandNode = findResult.getCommand().get();
        final Command command = commandNode.getCommand();
        final Map<String, String> parsedArgs;
        try {
            parsedArgs = command.parseArgs(findResult.getUnreachablePaths(), false);
        } catch (Command.ArgumentNotEnoughException e) {
            final String requiredStr = command.getArgs().stream()
                .map(info -> info.toString(false))
                .collect(Collectors.joining(" "));
            final StringJoiner joiner = new StringJoiner(" ");
            command.getSections().forEach(joiner::add);
            joiner.add(requiredStr);
            sender.sendMessage(errorPrefix + "Usage: /" + joiner.toString());
            return;
        }
        try {
            command.getHandler().execute(new ExecutionData(this, sender, commandNode, parsedArgs));
        } catch (final CommandExecutionException e) {
            final String message = e.getMessage();
            if (message != null) {
                sender.sendMessage(errorPrefix + message);
            }
        }
    }

    @Override
    public List<String> complete(final CommandSender sender, final CompletingPosition pos, final List<String> commandLine) {
        final String completing = pos == CompletingPosition.LAST
            ? Iterables.getLast(commandLine)
            : "";
        final BranchNode.WalkResult findResult = root.walk(commandLine);
        if (!findResult.getCommand().isPresent()) {
            return Iterables.getLast(findResult.getBranches()).getChildren().keySet().stream()
                .filter(s -> s.startsWith(completing))
                .collect(Collectors.toList());
        } else {
            final CommandNode commandNode = findResult.getCommand().get();
            final Command command = commandNode.getCommand();
            final ArgumentInfo argumentInfo = command.getArgs().get(
                    Math.min(command.getArgs().size() - 1,
                        findResult.getUnreachablePaths().size()));
            final String argumentName = argumentInfo.getName();
            try {
                final List<String> argsForParse = new ArrayList<>(findResult.getUnreachablePaths());
                argsForParse.add(completing);
                final String argumentValue = command.parseArgs(argsForParse, true).get(argumentName);
                final CommandCompleter completer = argumentInfo.getCompleterName()
                    .map(completerMap::get)
                    .orElse(command.getHandler());
                return completer.complete(new CompletionData(sender, commandNode, argumentName, argumentValue));
            } catch (Command.ArgumentNotEnoughException e) {
                throw new RuntimeException("unreachable", e);
            }
        }
    }
}

