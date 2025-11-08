package com.sabarish;

import com.sabarish.core.RetryManager;
import com.sabarish.model.Job;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RetryManagerTest {
    @Test
    void testBackoffGrowth() {
        RetryManager rm = new RetryManager(2, false);
        assertEquals(2, rm.backoffSeconds(1));
        assertEquals(4, rm.backoffSeconds(2));
        assertEquals(8, rm.backoffSeconds(3));
    }

    @Test
    void testFailureMovesToDeadAfterRetries() {
        Job job = new Job("echo hi", 1);
        RetryManager rm = new RetryManager(2, false);
        rm.handleFailure(job, "fail1");
        assertEquals(Job.State.DEAD, job.getState());
    }

    @Test
    void testRetryPending() {
        Job job = new Job("echo hi", 2);
        RetryManager rm = new RetryManager(2, false);
        rm.handleFailure(job, "fail1");
        assertEquals(Job.State.PENDING, job.getState());
        assertTrue(job.getNextRetryAt() != null);
    }
}
