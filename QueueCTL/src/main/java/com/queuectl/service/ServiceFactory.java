package com.queuectl.service;

import com.queuectl.core.JobManager;
import com.queuectl.core.retry.ExponentialBackoffRetryStrategy;
import com.queuectl.core.retry.RetryStrategy;
import com.queuectl.persistence.JsonConfigStore;
import com.queuectl.persistence.JsonJobStore;
import com.queuectl.repository.ConfigRepository;
import com.queuectl.repository.JobRepository;
import com.queuectl.repository.StopSignalPort;
import com.queuectl.repository.file.StopFlagRepository;
import com.queuectl.repository.json.JsonConfigRepository;
import com.queuectl.repository.json.JsonJobRepository;





public class ServiceFactory {
    public static JobRepository jobRepository() {
        return new JsonJobRepository(new JsonJobStore());
    }

    public static ConfigRepository configRepository() {
        return new JsonConfigRepository(new JsonConfigStore());
    }

    public static StopSignalPort stopSignalPort() {
        return new StopFlagRepository();
    }

    public static RetryStrategy retryStrategy(ConfigRepository cfg) {
        int backoffBase = cfg.getInt("backoff_base", 2);
        return new ExponentialBackoffRetryStrategy(backoffBase, true);
    }

    public static JobManager jobManager() {
        return new JobManager(jobRepository());
    }
}
