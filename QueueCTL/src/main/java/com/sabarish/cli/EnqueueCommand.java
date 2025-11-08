package com.sabarish.cli;

import com.sabarish.model.Job;
import com.sabarish.service.JobService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(name = "enqueue", description = "Add a new job to the queue")
public class EnqueueCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Job specification JSON, e.g. {\"command\":\"echo hi\"}")
    String jobJson;

    @Override
    public Integer call() {
        try {
            JobService svc = new JobService();
            Job job = svc.enqueueFromJson(jobJson);
            System.out.println("Job enqueued: " + job.getId());
            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }
}
