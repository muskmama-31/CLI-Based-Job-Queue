package com.queuectl;

import com.queuectl.core.JobManager;
import com.queuectl.model.Job;
import com.queuectl.repository.JobRepository;
import com.queuectl.repository.ConfigRepository;
import com.queuectl.service.JobService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JobServiceTest {
    @Test
    void enqueuePersistsJobViaRepository() {
        JobRepository repo = mock(JobRepository.class);
        when(repo.acquireNextReady()).thenReturn(Optional.empty());
        when(repo.loadAll()).thenReturn(Collections.emptyList());
        when(repo.findByState(Job.State.PENDING)).thenReturn(Collections.emptyList());
        when(repo.findById(anyString())).thenReturn(Optional.empty());

        JobManager jm = new JobManager(repo);
        ConfigRepository cfg = mock(ConfigRepository.class);
        when(cfg.getInt("max_retries", 3)).thenReturn(3);
        JobService svc = new JobService(jm, cfg);
        Job job = svc.enqueueFromJson("{\"command\":\"echo test\",\"max_retries\":2}");

        ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
        verify(repo, atLeastOnce()).save(captor.capture());
        assertEquals(job.getId(), captor.getValue().getId());
        assertEquals("echo test", job.getCommand());
        assertEquals(2, job.getMaxRetries());
    }
}
