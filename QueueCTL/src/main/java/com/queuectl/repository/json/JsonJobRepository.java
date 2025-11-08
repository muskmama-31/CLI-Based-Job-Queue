package com.queuectl.repository.json;

import com.queuectl.model.Job;
import com.queuectl.persistence.JsonJobStore;
import com.queuectl.repository.JobRepository;

import java.util.*;
import java.util.stream.Collectors;

public class JsonJobRepository implements JobRepository {
    private final JsonJobStore store;

    public JsonJobRepository(JsonJobStore store) {
        this.store = store;
    }

    @Override
    public Optional<Job> acquireNextReady() {
        return store.withLock(jobs -> {
            Optional<Job> next = jobs.stream()
                    .filter(Job::ready)
                    .sorted(Comparator
                            .comparingInt(Job::getPriority).reversed()
                            .thenComparing(Job::getCreatedAt))
                    .findFirst();
            next.ifPresent(j -> j.setState(Job.State.PROCESSING));
            return next;
        });
    }

    @Override
    public void save(Job job) {
        store.withLock(jobs -> {
            Map<String, Job> map = jobs.stream()
                    .collect(Collectors.toMap(Job::getId, j -> j, (a, b) -> a, LinkedHashMap::new));
            map.put(job.getId(), job);
            jobs.clear();
            jobs.addAll(map.values());
            return null;
        });
    }

    @Override
    public List<Job> loadAll() {
        return store.loadAll();
    }

    @Override
    public Optional<Job> findById(String id) {
        return store.loadAll().stream().filter(j -> j.getId().equals(id)).findFirst();
    }

    @Override
    public List<Job> findByState(Job.State state) {
        return store.loadAll().stream().filter(j -> j.getState() == state).collect(Collectors.toList());
    }
}
