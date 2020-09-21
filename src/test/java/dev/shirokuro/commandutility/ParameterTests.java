package dev.shirokuro.commandutility;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public final class ParameterTests {

    @Test
    public void fromStringRequiredNoCompl() {
        final Parameter info = Parameter.fromString("<foo>").orElse(null);
        assertEquals(new Parameter("foo", null, true), info);
    }

    @Test
    public void fromStringRequiredCompl() {
        final Parameter info = Parameter.fromString("<foo:bar>").orElse(null);
        assertEquals(new Parameter("foo", "bar", true), info);
    }

    @Test
    public void fromStringOptionalNoCompl() {
        final Parameter info = Parameter.fromString("[foo]").orElse(null);
        assertEquals(new Parameter("foo", null, false), info);
    }

    @Test
    public void fromStringOptionalCompl() {
        final Parameter info = Parameter.fromString("[foo:bar]").orElse(null);
        assertEquals(new Parameter("foo", "bar", false), info);
    }
}
