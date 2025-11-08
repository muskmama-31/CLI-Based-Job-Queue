package com.queuectl.persistence;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.queuectl.model.Job;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

public class JsonJobStore {
    private final Path dir;
    private final Path file;
    private final Gson gson;

    private static class InstantAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
        public JsonElement serialize(Instant src, Type t, JsonSerializationContext c) {
            return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.toString());
        }

        public Instant deserialize(JsonElement json, Type t, JsonDeserializationContext c) {
            if (json == null || json.isJsonNull())
                return null;
            return Instant.parse(json.getAsString());
        }
    }

    public JsonJobStore() {
        this(getDefaultDir());
    }

    public JsonJobStore(Path dir) {
        this.dir = dir;
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.file = dir.resolve("jobs.json");
        this.gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantAdapter()).setPrettyPrinting()
                .create();
        if (!Files.exists(file))
            persist(new ArrayList<>());
    }

    public static Path getDefaultDir() {
        return Paths.get(System.getProperty("user.dir")).resolve(".queuectl");
    }

    public <T> T withLock(Function<List<Job>, T> fn) {
        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "rw");
                FileChannel channel = raf.getChannel();
                FileLock lock = channel.lock()) {
            List<Job> jobs = readAll(raf);
            T result = fn.apply(jobs);
            writeAll(raf, jobs);
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Job store lock failed", e);
        }
    }

    public List<Job> loadAll() {
        try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<Job>>() {
            }.getType();
            List<Job> jobs = gson.fromJson(r, listType);
            return jobs == null ? new ArrayList<>() : jobs;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Job> readAll(RandomAccessFile raf) throws IOException {
        raf.seek(0);
        byte[] data = new byte[(int) raf.length()];
        if (data.length > 0)
            raf.readFully(data);
        String json = data.length == 0 ? "[]" : new String(data, StandardCharsets.UTF_8);
        Type listType = new TypeToken<List<Job>>() {
        }.getType();
        List<Job> jobs = gson.fromJson(json, listType);
        return jobs == null ? new ArrayList<>() : jobs;
    }

    private void writeAll(RandomAccessFile raf, List<Job> jobs) throws IOException {
        String json = gson.toJson(jobs);
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        raf.setLength(0);
        raf.seek(0);
        raf.write(data);
        raf.getChannel().force(true);
    }

    private void persist(List<Job> jobs) {
        try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            gson.toJson(jobs, w);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
