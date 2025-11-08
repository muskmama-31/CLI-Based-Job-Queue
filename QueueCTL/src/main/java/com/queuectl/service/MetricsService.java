package com.queuectl.service;

import com.queuectl.core.JobManager;
import com.queuectl.model.Job;

import java.util.List;

public class MetricsService {
    private final JobManager jm;

    public MetricsService(JobManager jm) {
        this.jm = jm;
    }

    public MetricsService() {
        this(ServiceFactory.jobManager());
    }

    public Summary summary() {
        List<Job> all = jm.loadAll();
        long pending = all.stream().filter(j -> j.getState() == Job.State.PENDING).count();
        long processing = all.stream().filter(j -> j.getState() == Job.State.PROCESSING).count();
        long completed = all.stream().filter(j -> j.getState() == Job.State.COMPLETED).count();
        long dead = all.stream().filter(j -> j.getState() == Job.State.DEAD).count();
        long failed = all.stream().filter(j -> j.getState() == Job.State.FAILED).count();
        long withDur = all.stream().filter(j -> j.getDurationMillis() != null && j.getState() == Job.State.COMPLETED)
                .count();
        double avgMs = withDur == 0 ? 0
                : all.stream().filter(j -> j.getDurationMillis() != null && j.getState() == Job.State.COMPLETED)
                        .mapToLong(Job::getDurationMillis).average().orElse(0);
        Summary s = new Summary();
        s.total = all.size();
        s.pending = pending;
        s.processing = processing;
        s.completed = completed;
        s.failed = failed;
        s.dead = dead;
        s.avgDurationMs = avgMs;
        return s;
    }

    public static class Summary {
        public long total;
        public long pending;
        public long processing;
        public long completed;
        public long failed;
        public long dead;
        public double avgDurationMs;
    }
}
