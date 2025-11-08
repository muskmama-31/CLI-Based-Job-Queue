package com.queuectl.cli;

import com.queuectl.persistence.JsonConfigStore;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "config", description = "Get/Set configuration", subcommands = {
        ConfigCommand.Get.class, ConfigCommand.Set.class })
public class ConfigCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("Use subcommands: get <key> | set <key> <value>");
    }

    @Command(name = "get", description = "Get config value")
    public static class Get implements Runnable {
        @Parameters(index = "0", description = "Key")
        String key;

        @Override
        public void run() {
            JsonConfigStore cfg = new JsonConfigStore();
            System.out.println(key + "=" + cfg.get(key));
        }
    }

    @Command(name = "set", description = "Set config value")
    public static class Set implements Runnable {
        @Parameters(index = "0", description = "Key")
        String key;
        @Parameters(index = "1", description = "Value")
        String value;

        @Override
        public void run() {
            JsonConfigStore cfg = new JsonConfigStore();
            cfg.set(key, value);
            System.out.println("Updated " + key + "=" + value);
        }
    }
}
