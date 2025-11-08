package com.queuectl.core.retry;

import com.queuectl.model.Job;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

public class ExponentialBackoffRetryStrategy implements RetryStrategy {
    private final int base;
    private final boolean jitter;

    public ExponentialBackoffRetryStrategy(int base, boolean jitter) {
        this.base = base;
        this.jitter = jitter;
    }

    @Override
    public long computeDelaySeconds(int attempts) {
        long delay = (long) Math.pow(base, attempts);
        if (jitter) {
            long extra = (long) (delay * ThreadLocalRandom.current().nextDouble(0.0, 0.25));
            delay += extra;
        }
        return delay;
    }

    @Override
    public void applyFailure(Job job, String errorMessage) {
        job.setErrorMessage(errorMessage);
        job.incrementAttempts();
        if (job.canRetry()) {
            long delay = computeDelaySeconds(job.getAttempts());
            job.setNextRetryAt(Instant.now().plusSeconds(delay));
            job.setState(Job.State.PENDING);
        } else {
            job.setState(Job.State.DEAD);
        }
    }
}
