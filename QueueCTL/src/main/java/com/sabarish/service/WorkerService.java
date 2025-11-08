package com.sabarish.service;

import com.sabarish.core.JobManager;
import com.sabarish.core.RetryManager;
import com.sabarish.core.StopSignal;
import com.sabarish.core.WorkerManager;
import com.sabarish.persistence.JsonConfigStore;

public class WorkerService {
    public void startWorkers(int count) {
        JsonConfigStore cfg = new JsonConfigStore();
        int pollMs = cfg.getInt("worker_poll_interval_ms", 1000);
        int backoffBase = cfg.getInt("backoff_base", 2);
        WorkerManager wm = new WorkerManager(new JobManager(), new RetryManager(backoffBase, true), pollMs);
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
