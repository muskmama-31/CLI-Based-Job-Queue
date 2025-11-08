package com.queuectl.repository.json;

import com.queuectl.persistence.JsonConfigStore;
import com.queuectl.repository.ConfigRepository;

public class JsonConfigRepository implements ConfigRepository {
    private final JsonConfigStore store;

    public JsonConfigRepository(JsonConfigStore store) {
        this.store = store;
    }

    @Override
    public String get(String key) {
        return store.get(key);
    }

    @Override
    public int getInt(String key, int def) {
        return store.getInt(key, def);
    }

    @Override
    public void set(String key, String value) {
        store.set(key, value);
    }
}
