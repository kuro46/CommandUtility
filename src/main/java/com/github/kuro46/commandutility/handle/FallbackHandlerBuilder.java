package com.github.kuro46.commandutility.handle;

import org.bukkit.command.CommandSender;

public final class FallbackHandlerBuilder {

    private CommandSenderType senderType;
    private Handler handler;

    public FallbackHandlerBuilder senderType(final CommandSenderType senderType) {
        this.senderType = senderType;
        return this;
    }

    public FallbackHandlerBuilder handler(final Handler handler) {
        this.handler = handler;
        return this;
    }

    public FallbackCommandHandler build() {
        return new FallbackCommandHandler() {

            @Override
            public CommandSenderType getSenderType() {
                return senderType;
            }

            @Override
            public void handleFallback(
                    final CommandManager manager,
                    final CommandSender sender,
                    final CommandSections commandSections,
                    final List<String> args) {
                handler.handle(manager, sender, commandSections, args);
            }
        };
    }

    @FunctionalInterface
    public interface Handler {

        void handle(
                CommandManager caller,
                CommandSender sender,
                CommandSections commandSections,
                List<String> args);
    }
}
