package com.queuectl.persistence;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class JsonConfigStore {
    private final Path file;
    private final Gson gson = new Gson();

    public JsonConfigStore() {
        this.file = JsonJobStore.getDefaultDir().resolve("config.json");
        try {
            Files.createDirectories(file.getParent());
            if (!Files.exists(file)) {
                Map<String, String> defaults = new HashMap<>();
                defaults.put("max_retries", "3");
                defaults.put("backoff_base", "2");
                defaults.put("worker_poll_interval_ms", "1000");
                defaults.put("graceful_shutdown_timeout_sec", "30");
                defaults.put("processing_stale_timeout_sec", "300");
                defaults.put("default_timeout_sec", "300");
                try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                    gson.toJson(defaults, w);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> readAll() {
        try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Type t = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> map = gson.fromJson(r, t);
            return map == null ? new HashMap<>() : map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writeAll(Map<String, String> map) {
        try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            gson.toJson(map, w);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String get(String key) {
        return readAll().get(key);
    }

    public int getInt(String key, int def) {
        String v = get(key);
        try {
            return v == null ? def : Integer.parseInt(v);
        } catch (Exception e) {
            return def;
        }
    }

    public void set(String key, String value) {
        Map<String, String> m = readAll();
        m.put(key, value);
        writeAll(m);
    }
}
