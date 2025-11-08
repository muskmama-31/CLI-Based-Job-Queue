package com.queuectl.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.queuectl.core.JobManager;
import com.queuectl.model.Job;
import com.queuectl.repository.ConfigRepository;

public class JobService {
    private final JobManager jm;
    private final ConfigRepository cfg;

    public JobService(JobManager manager, ConfigRepository cfg) {
        this.jm = manager;
        this.cfg = cfg;
    }

    public JobService() {
        this(ServiceFactory.jobManager(), ServiceFactory.configRepository());
    }

    public Job enqueueFromJson(String json) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        String cmd = obj.get("command").getAsString();
        int defaultMax = cfg.getInt("max_retries", 3);
        int maxRetries = obj.has("max_retries") ? obj.get("max_retries").getAsInt() : defaultMax;
        Job job = new Job(cmd, maxRetries);
        if (obj.has("priority"))
            job.setPriority(obj.get("priority").getAsInt());
        if (obj.has("timeout_sec"))
            job.setTimeoutSec(obj.get("timeout_sec").getAsInt());
        else
            job.setTimeoutSec(cfg.getInt("default_timeout_sec", 300));
        if (obj.has("run_at")) {
            job.setRunAt(java.time.Instant.parse(obj.get("run_at").getAsString()));
        }
        jm.save(job);
        return job;
    }
}
