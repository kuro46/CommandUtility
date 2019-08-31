package com.github.kuro46.commandutility.handle;

import java.util.Objects;

public final class CommandBuilder {

    private CommandSections sections;
    private CommandHandler handler;
    private String description;

    public CommandBuilder sections(final CommandSections sections) {
        Objects.requireNonNull(sections, "'sections' cannot be null");

        this.sections = sections;
        return this;
    }

    public CommandBuilder handler(final CommandHandler handler) {
        Objects.requireNonNull(handler, "'handler' cannot be null");

        this.handler = handler;
        return this;
    }

    public CommandBuilder description(final String description) {
        Objects.requireNonNull(description, "'description' cannot be null");

        this.description = description;
        return this;
    }

    public Command build() {
        Objects.requireNonNull(sections, "'sections' cannot be null");
        Objects.requireNonNull(handler, "'handler' cannot be null");

        return new Command(sections, handler, description);
    }
}
