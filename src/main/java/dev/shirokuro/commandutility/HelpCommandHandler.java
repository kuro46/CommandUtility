package dev.shirokuro.commandutility;

import java.util.StringJoiner;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * {@code HelpCommandHandler} is a {@code CommandHandler} to display help messages of all commands.
 */
public final class HelpCommandHandler implements CommandHandler {

    private final String header;
    private final String footer;

    /**
     * Constructs {@code HelpCommandHandler} with specified header and footer.
     *
     * @param header header of this command
     * @param footer footer of this command
     */
    public HelpCommandHandler(final String header, final String footer) {
        this.header = header;
        this.footer = footer;
    }

    public HelpCommandHandler() {
        this(null, null);
    }

    @Override
    public void execute(final ExecutionData data) {
        final BranchNode rootBranch = data.getGroup().getRoot();
        final CommandSender sender = data.getSender();
        if (header != null) {
            sender.sendMessage(header);
        }
        for (final CommandNode commandNode : rootBranch.walkNodeTree()) {
            final Command command = commandNode.getCommand();
            final StringJoiner sj = new StringJoiner(" ");
            sj.add(ChatColor.GRAY + "/" + command.getSections() + ChatColor.RESET);
            command.getArgs().stream()
                    .map(info -> {
                        final ChatColor prefix = info.isRequired()
                                ? ChatColor.GOLD
                                : ChatColor.YELLOW;
                        return prefix + info.toString(false) + ChatColor.RESET;
                    })
                    .forEach(sj::add);
            sender.sendMessage(sj.toString());
            command.getDescription().ifPresent(description -> {
                sender.sendMessage("  - " + description);
            });
        }
        if (footer != null) {
            sender.sendMessage(footer);
        }
    }
}


