package xyz.shirokuro.commandutility;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.ChatColor;
import xyz.shirokuro.commandutility.annotation.Completer;
import xyz.shirokuro.commandutility.annotation.Executor;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public final class CommandGroup implements TabExecutor {

    private final Map<String, CommandCompleter> completerMap = new HashMap<>();
    private final BranchNode root = new BranchNode("root");
    private final String errorPrefix;

    public CommandGroup(final String errorPrefix) {
        this.errorPrefix = Objects.requireNonNull(errorPrefix);
        addDefaultCompleters();
    }

    public CommandGroup() {
        this("");
    }

    public BranchNode getRoot() {
        return root;
    }

    private void addDefaultCompleters() {
        addCompleter("worlds", data -> {
            return Bukkit.getWorlds().stream()
                .map(World::getName)
                .filter(s -> s.startsWith(data.getCurrentValue()))
                .collect(Collectors.toList());
        });
        addCompleter("players", data -> {
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(s -> s.startsWith(data.getCurrentValue()))
                .collect(Collectors.toList());
        });
    }

    public CommandGroup addCompleter(final String argumentName, final CommandCompleter completer) {
        Objects.requireNonNull(argumentName);
        Objects.requireNonNull(completer);
        completerMap.put(argumentName, completer);
        return this;
    }

    public CommandGroup add(final CommandHandler handler, final String command, final String description) {
        Objects.requireNonNull(command);
        Objects.requireNonNull(description);
        Objects.requireNonNull(handler);
        if (command.trim().isEmpty()) {
            throw new IllegalArgumentException("command is empty!");
        }
        final List<ArgumentInfo> args = new ArrayList<>();
        final List<String> sections = new ArrayList<>();
        for (final String part : Splitter.on(' ').split(command)) {
            final Optional<ArgumentInfo> optionalInfo = ArgumentInfo.fromString(part);
            if (optionalInfo.isPresent()) {
                args.add(optionalInfo.get());
            } else {
                if (!args.isEmpty()) {
                    throw new RuntimeException("Found command part after argument part");
                }
                sections.add(part);
            }
        }
        if (sections.isEmpty()) {
            throw new IllegalArgumentException("No sections exists!");
        }
        final boolean firstTime = !root.getChildren().containsKey(sections.get(0));
        BranchNode current = root;
        for (int i = 0; i < sections.size(); i++) {
            final String section = sections.get(i);
            if (i < sections.size() - 1) {
                current = current.branch(section);
            } else {
                current.addChild(new CommandNode(current, section, args, description, handler));
            }
        }
        if (firstTime) {
            // null-check for unit testing
            // Register to Bukkit API
            if (Bukkit.getServer() != null) {
                final PluginCommand pluginCommand = Bukkit.getPluginCommand(sections.get(0));
                if (pluginCommand == null) {
                    throw new IllegalArgumentException("Command: " + sections.get(0) +
                            " is not registered by any plugins");
                }
                pluginCommand.setExecutor(this);
            }
        }
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
                if (!equalsMethodParams(method, ExecutionData.class)) {
                    throw new IllegalArgumentException("Method: " +
                            ReflectionUtils.methodInfo(method) +
                            " is annotated @Executor, but method parameters are incorrect!");
                }
                final ReflectedHandlerInfo info =
                    handlerInfoMap.computeIfAbsent(executorAnnotation.command(), s -> new ReflectedHandlerInfo());
                info.executor = method;
                info.description = executorAnnotation.description();
            } else if (completerAnnotation != null) {
                ReflectionUtils.assertPublic(method);
                if (!equalsMethodParams(method, CompletionData.class)) {
                    throw new IllegalArgumentException("Method: " +
                            ReflectionUtils.methodInfo(method) +
                            " is annotated @Completer, but method parameters are incorrect!");
                }
                final ReflectedHandlerInfo info =
                    handlerInfoMap.computeIfAbsent(completerAnnotation.command(), s -> new ReflectedHandlerInfo());
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

    private boolean equalsMethodParams(Method method, Class<?>... classes) {
        return Arrays.equals(method.getParameterTypes(), classes);
    }

    private static final class ReflectedHandlerInfo {
        private Method executor;
        private Method completer;
        private String description;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final List<String> normalized = new ArrayList<>();
        normalized.add(command.getName());
        normalized.addAll(Arrays.asList(args));
        final BranchNode.WalkResult findResult = root.walk(normalized);
        if (!findResult.getCommand().isPresent()) {
            sender.sendMessage(errorPrefix + "Candidates: " +
                    String.join(", ", Iterables.getLast(findResult.getBranches()).getChildren().keySet()));
            return true;
        }
        final CommandNode commandNode = findResult.getCommand().get();
        final Map<String, String> parsedArgs;
        try {
            parsedArgs = commandNode.parseArgs(findResult.getUnreachablePaths(), false);
        } catch (CommandNode.ArgumentNotEnoughException e) {
            final String requiredStr = commandNode.getArgs().stream()
                .map(info -> info.toString(false))
                .collect(Collectors.joining(" "));
            final StringJoiner joiner = new StringJoiner(" ");
            joiner.add(commandNode.sections());
            joiner.add(requiredStr);
            sender.sendMessage(errorPrefix + "Usage: /" + joiner.toString());
            return true;
        }
        try {
            commandNode.getHandler().execute(new ExecutionData(this, sender, commandNode, parsedArgs));
        } catch (final CommandExecutionException e) {
            final String message = e.getMessage();
            if (message != null) {
                sender.sendMessage(errorPrefix + message);
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> normalized = new ArrayList<>();
        normalized.add(command.getName());
        for (String arg : args) {
            if (!arg.isEmpty()) {
                normalized.add(arg);
            }
        }
        if (args.length >= 1 && args[args.length - 1].isEmpty()) {
            normalized.add("");
        }
        final String completing = normalized.get(normalized.size() - 1);
        final BranchNode.WalkResult findResult = root.walk(normalized.subList(0, normalized.size() - 1) /* Remove completing argument*/);
        if (!findResult.getCommand().isPresent()) {
            return Iterables.getLast(findResult.getBranches()).getChildren().keySet().stream()
                .filter(s -> s.startsWith(completing))
                .collect(Collectors.toList());
        } else {
            final CommandNode commandNode = findResult.getCommand().get();
            final ArgumentInfo argumentInfo = commandNode.getArgs().get(
                    Math.min(commandNode.getArgs().size() - 1,
                        findResult.getUnreachablePaths().size()));
            final String argumentName = argumentInfo.getName();
            try {
                final List<String> argsForParse = new ArrayList<>(findResult.getUnreachablePaths());
                argsForParse.add(completing);
                final String argumentValue = commandNode.parseArgs(argsForParse, true).get(argumentName);
                final CommandCompleter completer = argumentInfo.getCompleterName()
                    .map(completerMap::get)
                    .orElse(commandNode.getHandler());
                return completer.complete(new CompletionData(sender, commandNode, argumentName, argumentValue));
            } catch (CommandNode.ArgumentNotEnoughException e) {
                throw new RuntimeException("unreachable", e);
            }
        }
    }
}

