package xyz.shirokuro.commandutility;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;

final class ReflectionUtils {
    private ReflectionUtils() {
    }

    /**
     * Returns human-readable method information
     *
     * @return string representation of method
     */
    public static String methodInfo(final Method method) {
        Objects.requireNonNull(method, "method");
        return "Method: '" + method.getName() + "' in '" + method.getDeclaringClass().getName() + "'";
    }

    /**
     * Checks equality of method parameters.
     *
     * @return {@code true} if {@code method}'s parameter is equal to {@code classes}.
     * Otherwise {@code false}
     */
    public static boolean equalsMethodParams(final Method method, final Class<?>... classes) {
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(classes, "classes");
        return Arrays.equals(method.getParameterTypes(), classes);
    }

    /**
     * Asserts {@code method} is {@code public}.
     *
     * @throws IllegalArgumentException when {@code method} is not {@code public}
     */
    public static void assertPublic(final Method method) {
        Objects.requireNonNull(method, "method");
        if (!Modifier.isPublic(method.getModifiers())) {
            throw new IllegalArgumentException(methodInfo(method) + " is not public!");
        }
    }
}


