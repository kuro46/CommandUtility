package xyz.shirokuro.commandutility;

import java.util.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public final class CommandUtilsTests {

    @Test
    public void toEnumTestExists() {
        assertDoesNotThrow(() -> {
            final SampleEnum result = CommandUtils.toEnum(SampleEnum.class, "FOO", null);
            assertEquals(SampleEnum.FOO, result);
        });
    }

    @Test
    public void toEnumTestNotExists() {
        assertThrows(CommandExecutionException.class, () -> {
            CommandUtils.toEnum(SampleEnum.class, "BUZ", null);
        });
    }

    public enum SampleEnum {
        FOO,
        BAR
    }
}
