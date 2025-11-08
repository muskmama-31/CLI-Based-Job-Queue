package com.queuectl.cli;

import com.queuectl.core.JobManager;
import com.queuectl.model.Job;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Command(name = "list", description = "List jobs, optionally by state")
public class ListCommand implements Runnable {
    @Option(names = { "--state", "-s" }, description = "Filter by state: PENDING|PROCESSING|COMPLETED|FAILED|DEAD")
    String state;

    @Override
    public void run() {
        JobManager jm = new JobManager();
        List<Job> jobs = (state == null)
                ? jm.loadAll()
                : jm.byState(Job.State.valueOf(state.toUpperCase(Locale.ROOT)));
        DateTimeFormatter fmt = DateTimeFormatter.ISO_INSTANT;
        for (Job j : jobs) {
            System.out.println(j.getId() + "  [" + j.getState() + "]  attempts=" + j.getAttempts()
                    + "  cmd=\"" + j.getCommand() + "\"  updated="
                    + (j.getUpdatedAt() == null ? "" : fmt.format(j.getUpdatedAt())));
        }
    }
}
