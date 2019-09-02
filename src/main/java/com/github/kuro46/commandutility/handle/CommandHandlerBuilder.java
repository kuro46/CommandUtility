package com.github.kuro46.commandutility.handle;

import com.github.kuro46.commandutility.syntax.CommandSyntax;
import com.github.kuro46.commandutility.syntax.CompletionData;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.command.CommandSender;


public final class CommandHandlerBuilder {

    private CommandSyntax syntax = CommandSyntax.builder().build();
    private CommandSenderType senderType = CommandSenderType.ANY;
    private Handler handler;
    private Completer completer = (caller, sender, sections, completionData) -> {
        return Collections.emptyList();
    };

    public CommandHandlerBuilder syntax(final CommandSyntax syntax) {
        Objects.requireNonNull(syntax, "'syntax' cannot be null");

        this.syntax = syntax;
        return this;
    }

    public CommandHandlerBuilder senderType(final CommandSenderType senderType) {
        Objects.requireNonNull(senderType, "'senderType' cannot be null");

        this.senderType = senderType;
        return this;
    }

    public CommandHandlerBuilder handler(final Handler handler) {
        Objects.requireNonNull(handler, "'handler' cannot be null");

        this.handler = handler;
        return this;
    }

    public CommandHandlerBuilder completer(final Completer completer) {
        Objects.requireNonNull(completer, "'completer' cannot be null");

        this.completer = completer;
        return this;
    }

    public CommandHandler build() {
        Objects.requireNonNull(syntax, "'syntax' cannot be null");
        Objects.requireNonNull(senderType, "'senderType' cannot be null");
        Objects.requireNonNull(handler, "'handler' cannot be null");
        Objects.requireNonNull(completer, "'completer' cannot be null");

        return new CommandHandler() {

            @Override
            public CommandSyntax getCommandSyntax() {
                return syntax;
            }

            @Override
            public CommandSenderType getSenderType() {
                return senderType;
            }

            @Override
            public void handleCommand(
                    final CommandManager caller,
                    final CommandSender sender,
                    final CommandSections sections,
                    final Map<String, String> args) {
                handler.handle(caller, sender, sections, args);
            }

            @Override
            public List<String> handleTabComplete(
                    final CommandManager caller,
                    final CommandSender sender,
                    final CommandSections sections,
                    final CompletionData completionData) {
                return completer.complete(caller, sender, sections, completionData);
            }
        };
    }

    public void register(final CommandManager manager, final String sections) {
        register(manager, CommandSections.fromString(sections));
    }

    public void register(final CommandManager manager, final CommandSections sections) {
        manager.registerCommand(new Command(sections, build()));
    }

    @FunctionalInterface
    public interface Handler {

        void handle(
                CommandManager caller,
                CommandSender sender,
                CommandSections sections,
                Map<String, String> args);
    }

    @FunctionalInterface
    public interface Completer {

        List<String> complete(
                CommandManager caller,
                CommandSender sender,
                CommandSections sections,
                CompletionData completionData);
    }
}
