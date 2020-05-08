package dev.shirokuro.commandutility;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public final class ReflectionUtilsTests {

    @Test
    public void equalsMethodParamsTestEqual() {
        Method m = getMethod("oneParameterMethod", Object.class);
        ReflectionUtils.equalsMethodParams(m, Object.class);
    }

    private Method getMethod(final String name, final Class<?>... params) {
        try {
            return TargetClass.class.getDeclaredMethod(name, params);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void equalsMethodParamsTestNotEqual() {
        Method m = getMethod("publicMethod");
        ReflectionUtils.equalsMethodParams(m, Object.class);
    }

    @Test
    public void assertPublicTestPublicMethod() {
        Method m = getMethod("publicMethod");
        assertDoesNotThrow(() -> ReflectionUtils.assertPublic(m));
    }

    @Test
    public void assertPublicTestPrivateMethod() {
        Method m = getMethod("privateMethod");
        assertThrows(IllegalArgumentException.class, () -> ReflectionUtils.assertPublic(m));
    }

    @SuppressWarnings("unused")
    private static class TargetClass {
        private static void privateMethod() {
        }

        public static void publicMethod() {
        }

        public static void oneParameterMethod(Object o) {
        }
    }
}
