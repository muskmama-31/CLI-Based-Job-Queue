package com.queuectl.core.retry;

import com.queuectl.model.Job;

public interface RetryStrategy {
    long computeDelaySeconds(int attempts);

    void applyFailure(Job job, String errorMessage);
}
