package com.sabarish.cli;

import com.sabarish.core.JobManager;
import com.sabarish.model.Job;
import com.sabarish.service.WorkerService;
import picocli.CommandLine.Command;

import java.util.EnumMap;
import java.util.Map;

@Command(name = "status", description = "Show job state counts and worker signal")
public class StatusCommand implements Runnable {
    @Override
    public void run() {
        JobManager jm = new JobManager();
        Map<Job.State, Long> counts = new EnumMap<>(Job.State.class);
        for (Job.State s : Job.State.values())
            counts.put(s, 0L);
        jm.loadAll().forEach(j -> counts.put(j.getState(), counts.get(j.getState()) + 1));
        System.out.println("Jobs:");
        counts.forEach((s, c) -> System.out.println("  " + s + ": " + c));
        System.out.println("Stop signal: " + (WorkerService.isStopSignaled() ? "ON" : "OFF"));
    }
}
