package com.sabarish.core;

import com.sabarish.model.Job;
import com.sabarish.persistence.JsonJobStore;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class JobManager {
    private final JsonJobStore store = new JsonJobStore();

    public Optional<Job> acquireNextReady() {
        return store.withLock(jobs -> {
            Optional<Job> next = jobs.stream()
                    .filter(j -> j.getState() == Job.State.PENDING)
                    .filter(j -> j.getNextRetryAt() == null || !Instant.now().isBefore(j.getNextRetryAt()))
                    .sorted(Comparator.comparing(Job::getCreatedAt))
                    .findFirst();
            next.ifPresent(j -> j.setState(Job.State.PROCESSING));
            return next;
        });
    }

    public void update(Job job) {
        store.withLock(jobs -> {
            Map<String, Job> map = jobs.stream()
                    .collect(Collectors.toMap(Job::getId, j -> j, (a, b) -> a, LinkedHashMap::new));
            map.put(job.getId(), job);
            jobs.clear();
            jobs.addAll(map.values());
            return null;
        });
    }

    public void save(Job job) {
        update(job);
    }

    public List<Job> loadAll() {
        return store.loadAll();
    }

    public List<Job> byState(Job.State s) {
        return store.loadAll().stream().filter(j -> j.getState() == s).collect(Collectors.toList());
    }

    public Optional<Job> find(String id) {
        return store.loadAll().stream().filter(j -> j.getId().equals(id)).findFirst();
    }
}
