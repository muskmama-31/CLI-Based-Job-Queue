package com.queuectl.core;

import com.queuectl.persistence.JsonJobStore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;

public class StopSignal {
    private static Path flagPath() {
        return JsonJobStore.getDefaultDir().resolve("stop.flag");
    }

    public static boolean isSet() {
        return Files.exists(flagPath());
    }

    public static void set() {
        try {
            Files.createDirectories(flagPath().getParent());
            Files.write(flagPath(), "stop".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception ignored) {
        }
    }

    public static void clear() {
        try {
            Files.deleteIfExists(flagPath());
        } catch (Exception ignored) {
        }
    }
}
