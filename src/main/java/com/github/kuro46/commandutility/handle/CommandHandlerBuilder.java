package com.github.kuro46.commandutility.handle;

import com.github.kuro46.commandutility.syntax.CommandSyntax;
import com.github.kuro46.commandutility.syntax.CompletionData;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.bukkit.command.CommandSender;


public final class CommandHandlerBuilder {

    private CommandSyntax syntax;
    private CommandSenderType senderType;
    private Handler handler;
    private Completer completer = (caller, sender, sections, completionData) -> {
        return Collections.emptyList();
    };

    public CommandHandlerBuilder syntax(final CommandSyntax syntax) {
        this.syntax = syntax;
        return this;
    }

    public CommandHandlerBuilder senderType(final CommandSenderType senderType) {
        this.senderType = senderType;
        return this;
    }

    public CommandHandlerBuilder handler(final Handler handler) {
        this.handler = handler;
        return this;
    }

    public CommandHandlerBuilder completer(final Completer completer) {
        this.completer = completer;
        return this;
    }

    public CommandHandler build() {
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
