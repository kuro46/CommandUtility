package com.github.kuro46.commandutility.handle;

import com.github.kuro46.commandutility.StringConverters;
import com.github.kuro46.commandutility.syntax.ParseErrorReason;
import java.util.function.BiConsumer;
import org.bukkit.command.CommandSender;

public final class CommandManagerBuilder {

    private BiConsumer<CommandSender, ParseErrorReason> parseErrorHandler;
    private BiConsumer<CommandSender, CastError> castErrorHandler;
    private FallbackCommandHandler fallbackHandler;
    private StringConverters converters = new StringConverters();

    public CommandManagerBuilder() {
        converters.registerDefaults();
    }

    public CommandManagerBuilder parseErrorHandler(
            final BiConsumer<CommandSender, ParseErrorReason> parseErrorHandler) {
        this.parseErrorHandler = parseErrorHandler;
        return this;
    }

    public CommandManagerBuilder castErrorHandler(
            final BiConsumer<CommandSender, CastError> castErrorHandler) {
        this.castErrorHandler = castErrorHandler;
        return this;
    }

    public CommandManagerBuilder fallbackHandler(final FallbackCommandHandler fallbackHandler) {
        this.fallbackHandler = fallbackHandler;
        return this;
    }

    public CommandManagerBuilder converters(final StringConverters converters) {
        this.converters = converters;
        return this;
    }

    public CommandManager build() {
        return new CommandManager(fallbackHandler, converters) {

            @Override
            public void handleParseError(
                    final CommandSender sender,
                    final ParseErrorReason error) {
                parseErrorHandler.accept(sender, error);
            }

            @Override
            public void handleCastError(
                    final CommandSender sender,
                    final CastError error) {
                castErrorHandler.accept(sender, error);
            }
        };
    }
}
