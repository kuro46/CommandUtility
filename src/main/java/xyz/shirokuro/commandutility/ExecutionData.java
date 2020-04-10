package xyz.shirokuro.commandutility;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

public final class ExecutionData {

    private final List<String> sections;
    private final Map<String, String> args;

    public ExecutionData(final List<String> sections, final Map<String, String> args) {
        this.sections = ImmutableList.copyOf(sections);
        this.args = ImmutableMap.copyOf(args);
    }

    public List<String> getSections() {
        return sections;
    }

    public Map<String, String> getArgs() {
        return args;
    }
}
