package dev.shirokuro.commandutility;

import be.seeseemelk.mockbukkit.MockBukkit;
import java.io.File;
import java.io.StringReader;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

public final class TestPlugin extends JavaPlugin {
    public TestPlugin() {
        super();
    }

    TestPlugin(JavaPluginLoader loader, PluginDescriptionFile descriptionFile, File dataFolder, File file) {
        super(loader, descriptionFile, dataFolder, file);
    }

    public static void load() {
        MockBukkit.loadWith(TestPlugin.class, initDescription());
    }

    private static PluginDescriptionFile initDescription() {
        final StringReader reader = new StringReader(
                "name: testplugin\n" +
                "version: 0.1.0\n" +
                "main: dev.shirokuro.commandutility.TestPlugin\n" +
                "commands:\n" +
                "  foo: {}");
        try {
            return new PluginDescriptionFile(reader);
        } catch (InvalidDescriptionException e) {
            throw new RuntimeException(e);
        }
    }
}
