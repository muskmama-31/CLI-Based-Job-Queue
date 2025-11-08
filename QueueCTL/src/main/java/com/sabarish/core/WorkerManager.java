package com.sabarish.core;

import com.sabarish.worker.WorkerThread;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WorkerManager {
    private final JobManager jobManager;
    private final RetryManager retryManager;
    private final int pollIntervalMs;

    private ExecutorService pool;
    private final List<WorkerThread> workers = new ArrayList<>();

    public WorkerManager(JobManager jm, RetryManager rm, int pollIntervalMs) {
        this.jobManager = jm;
        this.retryManager = rm;
        this.pollIntervalMs = pollIntervalMs;
    }

    public void start(int count) {
        pool = Executors.newFixedThreadPool(count);
        for (int i = 0; i < count; i++) {
            WorkerThread w = new WorkerThread("worker-" + UUID.randomUUID().toString().substring(0, 8),
                    jobManager, retryManager, pollIntervalMs);
            workers.add(w);
            pool.submit(w);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public void shutdown() {
        for (WorkerThread w : workers)
            w.shutdown();
        StopSignal.set();
        pool.shutdown();
        try {
            pool.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }
}
