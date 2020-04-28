package xyz.shirokuro.commandutility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

final class ReflectedCommandHandler implements CommandHandler {

    private final Object caller;
    private final Method executor;
    private final Method completer;

    public ReflectedCommandHandler(final Object caller, final Method executor, final Method completer) {
        this.caller = Objects.requireNonNull(caller);
        this.executor = Objects.requireNonNull(executor);
        this.completer = completer;
    }

    @SuppressWarnings("unchecked")
    private <T> T invokeSilently(final Object caller, final Method method, final Object... args) {
        try {
            return (T) method.invoke(caller, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void execute(final ExecutionData data) {
        invokeSilently(caller, executor, data);
    }

    @Override
    public List<String> complete(final CompletionData data) {
        if (completer != null) {
            return invokeSilently(caller, completer, data);
        } else {
            return Collections.emptyList();
        }
    }
}


