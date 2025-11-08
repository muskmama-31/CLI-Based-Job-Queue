package com.queuectl.repository;

public interface ConfigRepository {
    String get(String key);

    int getInt(String key, int def);

    void set(String key, String value);
}
