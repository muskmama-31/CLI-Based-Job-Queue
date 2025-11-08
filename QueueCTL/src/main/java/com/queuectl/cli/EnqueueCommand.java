package com.queuectl.cli;

import com.queuectl.model.Job;
import com.queuectl.repository.ConfigRepository;
import com.queuectl.service.JobService;
import com.queuectl.service.ServiceFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(name = "enqueue", description = "Add a new job to the queue")
public class EnqueueCommand implements Callable<Integer> {

    @Parameters(index = "0", arity = "0..1", description = "Optional job specification JSON, e.g. {\"command\":\"echo hi\"}")
    String jobJson;

    @Option(names = "--command", description = "Shell command to execute (mutually exclusive with JSON)")
    String command;

    @Option(names = "--max-retries", description = "Max retry attempts")
    Integer maxRetries;

    @Option(names = "--priority", description = "Job priority (higher runs first)")
    Integer priority;

    @Option(names = "--timeout-sec", description = "Per-job timeout seconds")
    Integer timeoutSec;

    @Option(names = "--run-at", description = "Schedule start time (ISO-8601), e.g. 2025-11-08T20:15:00Z")
    String runAtIso;

    @Override
    public Integer call() {
        try {
            JobService svc = new JobService();
            Job job;
            if (jobJson != null && !jobJson.isEmpty()) {
                // Delegate to existing JSON parsing logic
                job = svc.enqueueFromJson(jobJson);
            } else {
                if (command == null || command.isEmpty()) {
                    System.err.println("Error: either JSON spec or --command must be provided");
                    return 2;
                }
                ConfigRepository cfg = ServiceFactory.configRepository();
                int defaultMax = cfg.getInt("max_retries", 3);
                int mr = maxRetries != null ? maxRetries : defaultMax;
                job = new Job(command, mr);
                if (priority != null)
                    job.setPriority(priority);
                if (timeoutSec != null)
                    job.setTimeoutSec(timeoutSec);
                else
                    job.setTimeoutSec(cfg.getInt("default_timeout_sec", 300));
                if (runAtIso != null && !runAtIso.isEmpty()) {
                    try {
                        job.setRunAt(java.time.Instant.parse(runAtIso));
                    } catch (Exception pe) {
                        System.err.println("Invalid --run-at value; must be ISO-8601 instant: " + pe.getMessage());
                        return 3;
                    }
                }
                // Persist job via JobService's manager
                // Reuse JSON path for persistence simplicity
                // Direct save through manager
                ServiceFactory.jobManager().save(job);
            }
            System.out.println("Job enqueued: " + job.getId());
            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }
}
