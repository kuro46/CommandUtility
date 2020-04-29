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

    private Object invokeSilently(final Object caller, final Method method, final Object... args) throws CommandExecutionException {
        try {
            return method.invoke(caller, args);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Cannot access to method: " + ReflectionUtils.methodInfo(method), e);
        } catch (final InvocationTargetException e) {
            if (e.getCause() instanceof CommandExecutionException) {
                throw (CommandExecutionException) e.getCause();
            }
            throw new RuntimeException("Exception occurred in wrapped method" + ReflectionUtils.methodInfo(method), e.getCause());
        }
    }

    @Override
    public void execute(final ExecutionData data) throws CommandExecutionException{
        invokeSilently(caller, executor, data);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> complete(final CompletionData data) {
        if (completer != null) {
            try {
                return (List<String>) invokeSilently(caller, completer, data);
            } catch (final CommandExecutionException e) {
                throw new RuntimeException("You cannot throw CommandExecutionException from completer", e);
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(caller, executor, completer);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        final ReflectedCommandHandler rch = (ReflectedCommandHandler) other;
        return Objects.equals(caller, rch.caller) &&
            Objects.equals(executor, rch.executor) &&
            Objects.equals(completer, rch.completer);
    }

    @Override
    public String toString() {
        return "ReflectedCommandHandler{caller='" + caller +
            "',executor='" + executor +
            "',completer='" + completer +
            "'}";
    }
}
