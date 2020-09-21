package dev.shirokuro.commandutility;

import java.util.StringJoiner;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

/**
 * {@code HelpCommandHandler} is a {@code CommandHandler} to display help messages of all commands.
 */
public final class HelpCommandHandler implements CommandHandler {

    private final String header;
    private final String footer;
    private final String requiredPermission;

    /**
     * Constructs {@code HelpCommandHandler} with specified header and footer.
     *
     * @param header header of this command
     * @param footer footer of this command
     */
    public HelpCommandHandler(final String header, final String footer, final String requiredPermission) {
        this.header = header;
        this.footer = footer;
        this.requiredPermission = requiredPermission;
    }

    public HelpCommandHandler(final String requiredPermission) {
        this(null, null, requiredPermission);
    }

    public HelpCommandHandler() {
        this(null, null, null);
    }

    @Override
    public void execute(final ExecutionData data) {
        final CommandSender sender = data.getSender();
        if (requiredPermission != null && !sender.hasPermission(requiredPermission)) {
            sender.sendMessage("You do not have permission");
            return;
        }
        if (header != null) {
            sender.sendMessage(header);
        }
        final BranchNode rootBranch = data.getGroup().getRoot();
        for (final CommandNode commandNode : rootBranch.walkNodeTree()) {
            final Command command = commandNode.getCommand();
            final StringJoiner sj = new StringJoiner(" ");
            sj.add(ChatColor.GRAY + "/" + String.join(" ", command.getSections()) + ChatColor.RESET);
            command.getParameters().stream()
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


