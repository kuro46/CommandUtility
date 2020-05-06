package dev.shirokuro.commandutility;

import org.junit.jupiter.api.Test;
import dev.shirokuro.commandutility.annotation.Completer;
import dev.shirokuro.commandutility.annotation.Executor;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public final class ArgumentInfoTests {

    @Test
    public void fromStringRequiredNoCompl() {
        final ArgumentInfo info = ArgumentInfo.fromString("<foo>").orElse(null);
        assertEquals(new ArgumentInfo("foo", null, true), info);
    }

    @Test
    public void fromStringRequiredCompl() {
        final ArgumentInfo info = ArgumentInfo.fromString("<foo:bar>").orElse(null);
        assertEquals(new ArgumentInfo("foo", "bar", true), info);
    }

    @Test
    public void fromStringOptionalNoCompl() {
        final ArgumentInfo info = ArgumentInfo.fromString("[foo]").orElse(null);
        assertEquals(new ArgumentInfo("foo", null, false), info);
    }

    @Test
    public void fromStringOptionalCompl() {
        final ArgumentInfo info = ArgumentInfo.fromString("[foo:bar]").orElse(null);
        assertEquals(new ArgumentInfo("foo", "bar", false), info);
    }
}
