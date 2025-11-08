package com.queuectl.service;

import com.queuectl.core.JobManager;
import com.queuectl.model.Job;

import java.util.List;

public class DLQService {
    private final JobManager jobManager;

    public DLQService(JobManager jm) {
        this.jobManager = jm;
    }

    public DLQService() {
        this(ServiceFactory.jobManager());
    }

    public List<Job> listDead() {
        return jobManager.byState(Job.State.DEAD);
    }

    public Job retry(String id) {
        Job j = jobManager.find(id).orElseThrow(() -> new RuntimeException("Job not found: " + id));
        if (j.getState() != Job.State.DEAD) {
            throw new RuntimeException("Job is not in DLQ: " + id);
        }
        j.resetForRetry();
        jobManager.update(j);
        return j;
    }
}
