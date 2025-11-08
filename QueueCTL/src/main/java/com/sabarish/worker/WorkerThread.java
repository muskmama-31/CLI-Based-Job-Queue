package com.sabarish.worker;

import com.sabarish.core.JobManager;
import com.sabarish.core.RetryManager;
import com.sabarish.core.StopSignal;
import com.sabarish.model.Job;
import com.sabarish.util.CommandExecutor;

public class WorkerThread implements Runnable {
    private final String id;
    private final JobManager jobManager;
    private final RetryManager retryManager;
    private final int pollIntervalMs;
    private volatile boolean running = true;

    public WorkerThread(String id, JobManager jm, RetryManager rm, int pollIntervalMs) {
        this.id = id;
        this.jobManager = jm;
        this.retryManager = rm;
        this.pollIntervalMs = pollIntervalMs;
    }

    @Override
    public void run() {
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
            CommandExecutor.Result r = CommandExecutor.exec(job.getCommand(), 5);
            if (r.exitCode == 0) {
                job.setOutput(r.output);
                job.setState(Job.State.COMPLETED);
            } else {
                retryManager.handleFailure(job, "exit=" + r.exitCode + " " + r.error);
            }
            jobManager.update(job);
        } catch (Exception e) {
            retryManager.handleFailure(job, e.getMessage());
            jobManager.update(job);
        }
    }

    public void shutdown() {
        running = false;
    }
}
