package dev.shirokuro.commandutility.platform;

import dev.shirokuro.commandutility.CommandCompleter;
import java.util.*;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public final class BukkitPlatform implements Platform {

    @Override
    public void registerHandler(final String firstCommand, final PlatformCommandHandler handler) throws CommandNotExistsException {
        Objects.requireNonNull(firstCommand, "firstCommand");
        Objects.requireNonNull(handler, "handler");
        final PluginCommand command = Bukkit.getPluginCommand(firstCommand);
        if (command == null) {
            throw new CommandNotExistsException(firstCommand);
        } else {
            command.setExecutor(new PlatformCommandHandlerDispatcher(handler));
        }
    }

    @Override
    public Map<String, CommandCompleter> defaultCompleters() {
        final Map<String, CommandCompleter> completers = new HashMap<>();
        completers.put("worlds", data -> {
            return Bukkit.getWorlds().stream()
                    .map(World::getName)
                    .filter(s -> s.startsWith(data.getCurrentValue()))
                    .collect(Collectors.toList());
        });
        completers.put("players", data -> {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.startsWith(data.getCurrentValue()))
                    .collect(Collectors.toList());
        });
        return completers;
    }

    public static final class PlatformCommandHandlerDispatcher implements TabExecutor {

        private final PlatformCommandHandler inner;

        public PlatformCommandHandlerDispatcher(final PlatformCommandHandler inner) {
            Objects.requireNonNull(inner, "inner");
            this.inner = inner;
        }

        @Override
        public boolean onCommand(final CommandSender sender, final Command command, final String alias, final String[] args) {
            final List<String> commandLine = new ArrayList<>();
            commandLine.add(command.getName());
            commandLine.addAll(Arrays.asList(args));
            inner.execute(sender, commandLine);
            return true;
        }

        @Override
        public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
            final List<String> commandLine = new ArrayList<>();
            commandLine.add(command.getName());
            Arrays.stream(args)
                    .filter(s -> !s.isEmpty())
                    .forEach(commandLine::add);
            final CompletingPosition pos = args.length >= 1 && args[args.length - 1].isEmpty()
                    ? CompletingPosition.NEXT
                    : CompletingPosition.CURRENT;
            return inner.complete(sender, pos, commandLine);
        }
    }
}
