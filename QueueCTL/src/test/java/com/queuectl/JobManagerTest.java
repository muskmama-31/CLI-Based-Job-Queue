package com.queuectl;

import com.queuectl.core.JobManager;
import com.queuectl.model.Job;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class JobManagerTest {
    @Test
    void enqueueAndAcquire() {
        JobManager jm = new JobManager();
        Job job = new Job("echo hi", 1);
        jm.save(job);
        Optional<Job> acquired = jm.acquireNextReady();
        assertTrue(acquired.isPresent());
        assertEquals(Job.State.PROCESSING, acquired.get().getState());
    }
}
