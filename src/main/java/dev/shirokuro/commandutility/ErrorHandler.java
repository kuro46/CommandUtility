package dev.shirokuro.commandutility;

import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;

/**
 * {@code ErrorHandler} handles errors dispatched by {@link CommandGroup}.
 */
public interface ErrorHandler {

    public static ErrorHandler defaultHandler() {
        return DefaultErrorHandler.getInstance();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Called when {@link CommandHandler#execute CommandHandler.execute} threw {@link CommandExecutionException}.
     * <p>It is recommended to send exception message to {@code dispatcher} like below.</p>
     * <pre>
     * dispatcher.sendMessage(&quot;Error: &quot; + exception.getMessage());
     * </pre>
     *
     * @param caller caller. Not null
     * @param dispatcher dispatcher of the command. Not null
     * @param exception exception thrown by {@link CommandHandler#execute CommandHandler.execute}. Not null
     */
    void onExecutionFailed(final CommandGroup caller, final CommandSender dispatcher, final CommandExecutionException exception);

    /**
     * Called when {@link CommandGroup} couldn't find preferred command for {@link CommandSender}'s input.
     * <p>It is recommended to send usage message to {@code dispatcher} like below.</p>
     * <pre>
     * String requiredStr = command.getArgs().stream()
     *         .map(info -&gt; info.toString(false))
     *         .collect(Collectors.joining(&quot; &quot;));
     * StringJoiner joiner = new StringJoiner(&quot; &quot;);
     * command.getSections().forEach(joiner::add);
     * joiner.add(requiredStr);
     * dispatcher.sendMessage(&quot;Usage: /&quot; + joiner.toString());
     * </pre>
     *
     * @param caller caller. Not null
     * @param dispatcher dispatcher of the command. Not null
     * @param branch branch. Not null
     */
    void onPreferredCommandNotFound(final CommandGroup caller, final CommandSender dispatcher, final BranchNode branch);

    /**
     * Called when args input by {@link CommandSender} is invalid.
     * <p>It is recommended to send candidates to {@code dispatcher} like below.</p>
     * <pre>
     * String requiredStr = command.getArgs().stream()
     *         .map(info -&gt; info.toString(false))
     *         .collect(Collectors.joining(&quot; &quot;));
     * StringJoiner joiner = new StringJoiner(&quot; &quot;);
     * command.getSections().forEach(joiner::add);
     * joiner.add(requiredStr);
     * dispatcher.sendMessage(&quot;Usage: /&quot; + joiner.toString());
     * </pre>
     *
     * @param caller caller. Not null
     * @param dispatcher dispatcher of the command. Not null
     * @param command command. Not null
     */
    void onInvalidArgs(final CommandGroup caller, final CommandSender dispatcher, final Command command);

    public static final class Builder {

        private ErrorHandler fallback;
        private BiConsumer<CommandSender, CommandExecutionException> onExecutionFailed;
        private BiConsumer<CommandSender, BranchNode> onPreferredCommandNotFound;
        private BiConsumer<CommandSender, Command> onInvalidArgs;

        public Builder fallback(final ErrorHandler fallback) {
            this.fallback = fallback;
            return this;
        }

        public Builder onExecutionFailed(final BiConsumer<CommandSender, CommandExecutionException> onExecutionFailed) {
            this.onExecutionFailed = onExecutionFailed;
            return this;
        }

        public Builder onInvalidArgs(final BiConsumer<CommandSender, Command> onInvalidArgs) {
            this.onInvalidArgs = onInvalidArgs;
            return this;
        }

        public Builder onPreferredCommandNotFound(final BiConsumer<CommandSender, BranchNode> onPreferredCommandNotFound) {
            this.onPreferredCommandNotFound = onPreferredCommandNotFound;
            return this;
        }

        public ErrorHandler build() {
            return new ErrorHandler() {

                @Override
                public void onPreferredCommandNotFound(final CommandGroup caller, final CommandSender dispatcher, final BranchNode branch) {
                    if (onPreferredCommandNotFound != null) {
                        onPreferredCommandNotFound.accept(dispatcher, branch);
                    } else if (fallback != null) {
                        fallback.onPreferredCommandNotFound(caller, dispatcher, branch);
                    }
                }

                @Override
                public void onExecutionFailed(final CommandGroup caller, final CommandSender dispatcher, final CommandExecutionException exception) {
                    if (onExecutionFailed != null) {
                        onExecutionFailed.accept(dispatcher, exception);
                    } else if (fallback != null) {
                        fallback.onExecutionFailed(caller, dispatcher, exception);
                    }
                }

                @Override
                public void onInvalidArgs(final CommandGroup caller, final CommandSender dispatcher, final Command command) {
                    if (onInvalidArgs != null) {
                        onInvalidArgs.accept(dispatcher, command);
                    } else if (fallback != null) {
                        fallback.onInvalidArgs(caller, dispatcher, command);
                    }
                }
            };
        }
    }

    public static final class DefaultErrorHandler implements ErrorHandler {

        private static final DefaultErrorHandler INSTANCE = new DefaultErrorHandler();

        private DefaultErrorHandler() {
        }

        public static DefaultErrorHandler getInstance() {
            return INSTANCE;
        }

        @Override
        public void onPreferredCommandNotFound(final CommandGroup caller, final CommandSender dispatcher, final BranchNode branch) {
            dispatcher.sendMessage("Candidates: " + String.join(", ", branch.getChildren().keySet()));
        }

        @Override
        public void onExecutionFailed(final CommandGroup caller, final CommandSender dispatcher, final CommandExecutionException exception) {
            dispatcher.sendMessage(exception.getMessage());
        }

        @Override
        public void onInvalidArgs(final CommandGroup caller, final CommandSender dispatcher, final Command command) {
            final String requiredStr = command.getArgs().stream()
                    .map(info -> info.toString(false))
                    .collect(Collectors.joining(" "));
            final StringJoiner joiner = new StringJoiner(" ");
            command.getSections().forEach(joiner::add);
            joiner.add(requiredStr);
            dispatcher.sendMessage("Usage: " + joiner.toString());
        }
    }
}
