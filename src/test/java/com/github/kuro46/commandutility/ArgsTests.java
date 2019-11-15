package com.github.kuro46.commandutility;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ArgsTests {

    @Test
    void instanciateWithDuplicatedArgs() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Args.builder()
                .required("foo", "bar")
                .optional("foo")
                .build();
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Args.builder()
                .required("foo", "bar", "foo")
                .build();
        });
    }

    @Test
    void instanciateWithIllegalOrderArgs() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Args.builder()
                .optional("foo", "bar")
                .required("buzz")
                .build();
        });
    }

    @SuppressWarnings("ClassCanBeStatic")
    @Nested
    class Parse {

        @Test
        void parseNotEnoughRawArgs() {
            final Args args = Args.builder()
                .required("foo")
                .required("bar")
                .required("buzz")
                .build();
            final List<String> target = ImmutableList.of("target1");
            Assertions.assertFalse(args.parse(target).isPresent());
        }

        @Test
        void parseEnoughRawArgs() {
            final Args args = Args.builder()
                .required("foo")
                .required("bar")
                .required("buzz")
                .build();
            final List<String> target = ImmutableList.of("target1", "target2", "target3");
            final Optional<ParsedArgs> wrappedParsedArgs = args.parse(target);
            Assertions.assertTrue(wrappedParsedArgs.isPresent());
            final ParsedArgs parsedArgs = wrappedParsedArgs.get();
            Assertions.assertEquals("target1", parsedArgs.getOrFail("foo"));
            Assertions.assertEquals("target2", parsedArgs.getOrFail("bar"));
            Assertions.assertEquals("target3", parsedArgs.getOrFail("buzz"));
        }

        @Test
        void parseTooMuchRawArgs() {
            final Args args = Args.builder()
                .required("foo")
                .required("bar")
                .required("buzz")
                .build();
            final List<String> target = ImmutableList.of(
                "target1",
                "target2",
                "target3",
                "target4"
            );
            final Optional<ParsedArgs> wrappedParsedArgs = args.parse(target);
            Assertions.assertTrue(wrappedParsedArgs.isPresent());
            final ParsedArgs parsedArgs = wrappedParsedArgs.get();
            Assertions.assertEquals("target1", parsedArgs.getOrFail("foo"));
            Assertions.assertEquals("target2", parsedArgs.getOrFail("bar"));
            Assertions.assertEquals("target3 target4", parsedArgs.getOrFail("buzz"));
        }

        @Test
        void parseMinimumRawArgsWithOptArgs() {
            final Args args = Args.builder()
                .required("foo")
                .required("bar")
                .optional("buzz")
                .build();
            final List<String> target = ImmutableList.of("target1", "target2");
            final Optional<ParsedArgs> wrappedParsedArgs = args.parse(target);
            Assertions.assertTrue(wrappedParsedArgs.isPresent());
            final ParsedArgs parsedArgs = wrappedParsedArgs.get();
            Assertions.assertEquals("target1", parsedArgs.getOrFail("foo"));
            Assertions.assertEquals("target2", parsedArgs.getOrFail("bar"));
        }

        @Test
        void parseMaximumRawArgsWithOptArgs() {
            final Args args = Args.builder()
                .required("foo")
                .required("bar")
                .optional("buzz")
                .build();
            final List<String> target = ImmutableList.of("target1", "target2", "target3");
            final Optional<ParsedArgs> wrappedParsedArgs = args.parse(target);
            Assertions.assertTrue(wrappedParsedArgs.isPresent());
            final ParsedArgs parsedArgs = wrappedParsedArgs.get();
            Assertions.assertEquals("target1", parsedArgs.getOrFail("foo"));
            Assertions.assertEquals("target2", parsedArgs.getOrFail("bar"));
            Assertions.assertEquals("target3", parsedArgs.getOrFail("buzz"));
        }
    }
}
