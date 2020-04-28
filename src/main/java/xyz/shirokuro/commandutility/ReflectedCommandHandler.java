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

    private Object invokeSilently(final Object caller, final Method method, final Object... args) {
        try {
            return method.invoke(caller, args);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Cannot access to method: " + ReflectionUtils.methodInfo(method), e);
        } catch (final InvocationTargetException e) {
            throw new RuntimeException("Exception occurred in wrapped method" + ReflectionUtils.methodInfo(method), e.getCause());
        }
    }

    @Override
    public void execute(final ExecutionData data) {
        invokeSilently(caller, executor, data);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> complete(final CompletionData data) {
        if (completer != null) {
            return (List<String>) invokeSilently(caller, completer, data);
        } else {
            return Collections.emptyList();
        }
    }
}
