package com.queuectl.core;

import com.queuectl.core.retry.RetryStrategy;
import com.queuectl.worker.WorkerThread;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WorkerManager {
    private final JobManager jobManager;
    private final RetryStrategy retryStrategy;
    private final int pollIntervalMs;
    private final int gracefulShutdownSeconds;

    private ExecutorService pool;
    private final List<WorkerThread> workers = new ArrayList<>();

    public WorkerManager(JobManager jm, RetryStrategy strategy, int pollIntervalMs) {
        this(jm, strategy, pollIntervalMs, 30);
    }

    public WorkerManager(JobManager jm, RetryStrategy strategy, int pollIntervalMs, int gracefulShutdownSeconds) {
        this.jobManager = jm;
        this.retryStrategy = strategy;
        this.pollIntervalMs = pollIntervalMs;
        this.gracefulShutdownSeconds = gracefulShutdownSeconds;
    }

    public void start(int count) {
        pool = Executors.newFixedThreadPool(count);
        for (int i = 0; i < count; i++) {
            WorkerThread w = new WorkerThread("worker-" + UUID.randomUUID().toString().substring(0, 8),
                    jobManager, retryStrategy, pollIntervalMs);
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
            pool.awaitTermination(gracefulShutdownSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }
}
