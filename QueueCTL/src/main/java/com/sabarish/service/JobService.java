package com.sabarish.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sabarish.core.JobManager;
import com.sabarish.model.Job;

public class JobService {
    private final JobManager jm = new JobManager();

    public Job enqueueFromJson(String json) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        String cmd = obj.get("command").getAsString();
        int maxRetries = obj.has("max_retries") ? obj.get("max_retries").getAsInt() : 3;
        Job job = new Job(cmd, maxRetries);
        jm.save(job);
        return job;
    }
}
