package com.sabarish.cli;

import com.sabarish.core.JobManager;
import com.sabarish.model.Job;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "dlq", description = "Dead Letter Queue", subcommands = {
        DlqCommand.List.class, DlqCommand.Retry.class })
public class DlqCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("Use subcommands: list|retry <jobId>");
    }

    @Command(name = "list", description = "List DLQ jobs")
    public static class List implements Runnable {
        @Override
        public void run() {
            JobManager jm = new JobManager();
            jm.byState(Job.State.DEAD).forEach(j -> System.out
                    .println(j.getId() + "  DEAD  attempts=" + j.getAttempts() + "  err=" + j.getErrorMessage()));
        }
    }

    @Command(name = "retry", description = "Retry a DLQ job")
    public static class Retry implements Runnable {
        @Parameters(index = "0", description = "Job ID")
        String id;

        @Override
        public void run() {
            JobManager jm = new JobManager();
            Job j = jm.find(id).orElseThrow(() -> new RuntimeException("Job not found: " + id));
            if (j.getState() != Job.State.DEAD)
                throw new RuntimeException("Job is not in DLQ");
            j.resetForRetry();
            jm.update(j);
            System.out.println("DLQ job moved back to PENDING: " + j.getId());
        }
    }
}
