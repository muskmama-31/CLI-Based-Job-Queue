package com.queuectl.worker;

import com.queuectl.core.JobManager;
import com.queuectl.core.StopSignal;
import com.queuectl.core.retry.RetryStrategy;
import com.queuectl.model.Job;
import com.queuectl.util.CommandExecutor;
import com.queuectl.persistence.JsonJobStore;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class WorkerThread implements Runnable {
    private final String id;
    private final JobManager jobManager;
    private final RetryStrategy retryStrategy;
    private final int pollIntervalMs;
    private volatile boolean running = true;

    public WorkerThread(String id, JobManager jm, RetryStrategy strategy, int pollIntervalMs) {
        this.id = id;
        this.jobManager = jm;
        this.retryStrategy = strategy;
        this.pollIntervalMs = pollIntervalMs;
    }

    @Override
    public void run() {
        Thread.currentThread().setName(id);
        while (running && !StopSignal.isSet()) {
            try {
                java.util.Optional<Job> jobOpt = jobManager.acquireNextReady();
                if (!jobOpt.isPresent()) {
                    Thread.sleep(pollIntervalMs);
                    continue;
                }
                Job job = jobOpt.get();
                process(job);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {

            }
        }
    }

    private void process(Job job) {
        try {
            job.setStartedAt(java.time.Instant.now());
            int timeout = job.getTimeoutSec() == null ? 300 : job.getTimeoutSec();
            CommandExecutor.Result r = CommandExecutor.exec(job.getCommand(), timeout);
            if (r.exitCode == 0) {
                job.setOutput(r.output);
                job.setState(Job.State.COMPLETED);
            } else {
                retryStrategy.applyFailure(job, "exit=" + r.exitCode + " " + r.error);
            }
            if (job.getStartedAt() != null) {
                long dur = java.time.Duration.between(job.getStartedAt(), java.time.Instant.now()).toMillis();
                job.setDurationMillis(dur);
            }
            try {
                Path logsDir = JsonJobStore.getDefaultDir().resolve("logs");
                Files.createDirectories(logsDir);
                Path logFile = logsDir.resolve("job-" + job.getId() + ".log");
                String content = (r.output == null ? "" : r.output);
                if (r.error != null && !r.error.isEmpty()) {
                    content += System.lineSeparator() + "[error] " + r.error;
                }
                Files.writeString(logFile, content, StandardCharsets.UTF_8);
                job.setLogPath(logFile.toString());
            } catch (Exception ignored) {
            }
            jobManager.update(job);
        } catch (Exception e) {
            retryStrategy.applyFailure(job, e.getMessage());
            jobManager.update(job);
        }
    }

    public void shutdown() {
        running = false;
    }
}
