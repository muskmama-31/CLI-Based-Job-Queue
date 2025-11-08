package com.queuectl.repository;

import com.queuectl.model.Job;

import java.util.List;
import java.util.Optional;

public interface JobRepository {
    Optional<Job> acquireNextReady();

    void save(Job job);

    List<Job> loadAll();

    Optional<Job> findById(String id);

    List<Job> findByState(Job.State state);
}
