package xyz.shirokuro.commandutility;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import java.util.StringJoiner;

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
            final StringJoiner sj = new StringJoiner(" ");
            sj.add(ChatColor.GRAY + commandNode.sections() + ChatColor.RESET);
            commandNode.getArgs().stream()
                .map(info -> {
                    final ChatColor prefix = info.isRequired()
                        ? ChatColor.GOLD
                        : ChatColor.YELLOW;
                    return prefix + info.toString(false) + ChatColor.RESET;
                })
                .forEach(sj::add);
            sj.add("-");
            sj.add(commandNode.getDescription());
            sender.sendMessage(sj.toString());
        }
        if (footer != null) {
            sender.sendMessage(footer);
        }
    }
}


