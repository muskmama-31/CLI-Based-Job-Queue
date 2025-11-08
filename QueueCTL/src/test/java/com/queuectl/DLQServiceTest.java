package com.queuectl;

import com.queuectl.core.JobManager;
import com.queuectl.model.Job;
import com.queuectl.repository.JobRepository;
import com.queuectl.service.DLQService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DLQServiceTest {
    @Test
    void listDeadReturnsDeadJobs() {
        Job dead = new Job("echo dead", 1);
        dead.setState(Job.State.DEAD);
        JobRepository repo = mock(JobRepository.class);
        when(repo.findByState(Job.State.DEAD)).thenReturn(List.of(dead));
        when(repo.loadAll()).thenReturn(List.of(dead));
        when(repo.acquireNextReady()).thenReturn(Optional.empty());
        when(repo.findById(dead.getId())).thenReturn(Optional.of(dead));

        JobManager jm = new JobManager(repo);
        DLQService svc = new DLQService(jm);
        List<Job> result = svc.listDead();
        assertEquals(1, result.size());
        assertEquals(dead.getId(), result.get(0).getId());
    }

    @Test
    void retryMovesDeadJobToPending() {
        Job dead = new Job("echo dead", 1);
        dead.setState(Job.State.DEAD);
        dead.setNextRetryAt(Instant.now());
        JobRepository repo = mock(JobRepository.class);
        when(repo.findById(dead.getId())).thenReturn(Optional.of(dead));
        when(repo.findByState(Job.State.DEAD)).thenReturn(List.of(dead));
        when(repo.loadAll()).thenReturn(List.of(dead));
        when(repo.acquireNextReady()).thenReturn(Optional.empty());

        JobManager jm = new JobManager(repo);
        DLQService svc = new DLQService(jm);
        Job retried = svc.retry(dead.getId());
        assertEquals(Job.State.PENDING, retried.getState());
        assertNull(retried.getNextRetryAt());

        ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
        verify(repo, atLeastOnce()).save(captor.capture());
        boolean sawPending = captor.getAllValues().stream().anyMatch(j -> j.getState() == Job.State.PENDING);
        assertTrue(sawPending);
    }
}
