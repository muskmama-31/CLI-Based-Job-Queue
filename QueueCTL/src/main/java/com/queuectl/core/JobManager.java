package com.queuectl.core;

import com.queuectl.model.Job;
import com.queuectl.repository.JobRepository;
import com.queuectl.repository.ConfigRepository;
import com.queuectl.repository.json.JsonJobRepository;
import com.queuectl.repository.json.JsonConfigRepository;
import com.queuectl.persistence.JsonJobStore;
import com.queuectl.persistence.JsonConfigStore;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

public class JobManager {
    private final JobRepository repo;
    private final ConfigRepository config;

    public JobManager() {
        this(new JsonJobRepository(new JsonJobStore()), new JsonConfigRepository(new JsonConfigStore()));
    }

    public JobManager(JobRepository repo) {
        this(repo, null);
    }

    public JobManager(JobRepository repo, ConfigRepository config) {
        this.repo = repo;
        this.config = config;
    }

    public Optional<Job> acquireNextReady() {
        recoverStaleProcessing();
        return repo.acquireNextReady();
    }

    public void update(Job job) {
        repo.save(job);
    }

    public void save(Job job) {
        repo.save(job);
    }

    public List<Job> loadAll() {
        return repo.loadAll();
    }

    public List<Job> byState(Job.State s) {
        return repo.findByState(s);
    }

    public Optional<Job> find(String id) {
        return repo.findById(id);
    }

    private void recoverStaleProcessing() {
        if (config == null)
            return;
        int staleSec = config.getInt("processing_stale_timeout_sec", 300);
        Instant cutoff = Instant.now().minusSeconds(staleSec);
        repo.loadAll().stream()
                .filter(j -> j.getState() == Job.State.PROCESSING)
                .filter(j -> j.getUpdatedAt() != null && j.getUpdatedAt().isBefore(cutoff))
                .forEach(j -> {
                    j.setState(Job.State.PENDING);
                    j.setNextRetryAt(null);
                    repo.save(j);
                });
    }
}
