package com.queuectl;

import com.queuectl.core.retry.ExponentialBackoffRetryStrategy;
import com.queuectl.core.retry.RetryStrategy;
import com.queuectl.model.Job;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RetryManagerTest {
    @Test
    void testBackoffGrowth() {
        RetryStrategy rs = new ExponentialBackoffRetryStrategy(2, false);
        assertEquals(2, rs.computeDelaySeconds(1));
        assertEquals(4, rs.computeDelaySeconds(2));
        assertEquals(8, rs.computeDelaySeconds(3));
    }

    @Test
    void testFailureMovesToDeadAfterRetries() {
        Job job = new Job("echo hi", 1);
        RetryStrategy rs = new ExponentialBackoffRetryStrategy(2, false);
        rs.applyFailure(job, "fail1");
        assertEquals(Job.State.DEAD, job.getState());
    }

    @Test
    void testRetryPending() {
        Job job = new Job("echo hi", 2);
        RetryStrategy rs = new ExponentialBackoffRetryStrategy(2, false);
        rs.applyFailure(job, "fail1");
        assertEquals(Job.State.PENDING, job.getState());
        assertNotNull(job.getNextRetryAt());
    }
}
