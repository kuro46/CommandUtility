package xyz.shirokuro.commandutility;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ArgTests {

    @Test
    void equalityTest() {
        final Arg reqArg = new Arg("foo", true);
        final Arg optArg = new Arg("foo", false);
        Assertions.assertEquals(reqArg, optArg);
    }

    @Test
    void toStringTest() {
        final Arg reqArg = new Arg("foo", true);
        Assertions.assertEquals(reqArg.toString(), "<foo>");
        final Arg optArg = new Arg("bar", false);
        Assertions.assertEquals(optArg.toString(), "[bar]");
    }
}
