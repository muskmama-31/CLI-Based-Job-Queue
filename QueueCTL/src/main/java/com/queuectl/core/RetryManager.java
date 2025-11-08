package com.queuectl.core;

import com.queuectl.core.retry.RetryStrategy;
import com.queuectl.model.Job;




@Deprecated
public class RetryManager {
    private final RetryStrategy strategy;

    public RetryManager(RetryStrategy strategy) {
        this.strategy = strategy;
    }

    public long backoffSeconds(int attempts) {
        return strategy.computeDelaySeconds(attempts);
    }

    public void handleFailure(Job job, String errorMessage) {
        strategy.applyFailure(job, errorMessage);
    }
}
