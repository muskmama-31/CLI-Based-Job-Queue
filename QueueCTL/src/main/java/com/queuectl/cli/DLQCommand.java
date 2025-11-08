package com.queuectl.cli;

import com.queuectl.model.Job;
import com.queuectl.service.DLQService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "dlq", description = "Dead Letter Queue", subcommands = {
        DLQCommand.List.class, DLQCommand.Retry.class })
public class DLQCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("Use subcommands: list|retry <jobId>");
    }

    @Command(name = "list", description = "List DLQ jobs")
    public static class List implements Runnable {
        @Override
        public void run() {
            DLQService svc = new DLQService();
            svc.listDead().forEach(j -> System.out
                    .println(j.getId() + "  DEAD  attempts=" + j.getAttempts() + "  err=" + j.getErrorMessage()));
        }
    }

    @Command(name = "retry", description = "Retry a DLQ job")
    public static class Retry implements Runnable {
        @Parameters(index = "0", description = "Job ID")
        String id;

        @Override
        public void run() {
            DLQService svc = new DLQService();
            Job j = svc.retry(id);
            System.out.println("DLQ job moved back to PENDING: " + j.getId());
        }
    }
}
