package com.queuectl.repository.file;

import com.queuectl.persistence.JsonJobStore;
import com.queuectl.repository.StopSignalPort;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class StopFlagRepository implements StopSignalPort {
    private Path flagPath() {
        return JsonJobStore.getDefaultDir().resolve("stop.flag");
    }

    @Override
    public boolean isSet() {
        return Files.exists(flagPath());
    }

    @Override
    public void set() {
        try {
            Files.createDirectories(flagPath().getParent());
            Files.write(flagPath(), "stop".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void clear() {
        try {
            Files.deleteIfExists(flagPath());
        } catch (Exception ignored) {
        }
    }
}
