package com.sabarish.core;

import com.sabarish.model.Job;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

public class RetryManager {
    private final int backoffBase;
    private final boolean jitter;

    public RetryManager(int backoffBase, boolean jitter) {
        this.backoffBase = backoffBase;
        this.jitter = jitter;
    }

    public long backoffSeconds(int attempts) {
        long delay = (long) Math.pow(backoffBase, attempts);
        if (jitter) {
            long extra = (long) (delay * ThreadLocalRandom.current().nextDouble(0.0, 0.25));
            delay += extra;
        }
        return delay;
    }

    public void handleFailure(Job job, String errorMessage) {
        job.setErrorMessage(errorMessage);
        job.incrementAttempts();
        if (job.canRetry()) {
            long delay = backoffSeconds(job.getAttempts());
            job.setNextRetryAt(Instant.now().plusSeconds(delay));
            job.setState(Job.State.PENDING);
        } else {
            job.setState(Job.State.DEAD);
        }
    }
}
