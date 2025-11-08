package com.queuectl.service;

import com.queuectl.core.WorkerManager;
import com.queuectl.core.JobManager;
import com.queuectl.core.StopSignal;
import com.queuectl.core.retry.RetryStrategy;
import com.queuectl.repository.ConfigRepository;

public class WorkerService {
    private final JobManager jobManager;
    private final ConfigRepository configRepository;

    public WorkerService(JobManager jm, ConfigRepository cfgRepo) {
        this.jobManager = jm;
        this.configRepository = cfgRepo;
    }

    public WorkerService() {
        this(ServiceFactory.jobManager(), ServiceFactory.configRepository());
    }

    public void startWorkers(int count) {
        int pollMs = configRepository.getInt("worker_poll_interval_ms", 1000);
        int shutdownSec = configRepository.getInt("graceful_shutdown_timeout_sec", 30);
        RetryStrategy strategy = ServiceFactory.retryStrategy(configRepository);
        WorkerManager wm = new WorkerManager(jobManager, strategy, pollMs, shutdownSec);
        wm.start(count);
    }

    public void stopWorkers() {
        StopSignal.set();
    }

    public void clearStopSignal() {
        StopSignal.clear();
    }

    public static boolean isStopSignaled() {
        return StopSignal.isSet();
    }
}
