package com.queuectl.model;

import java.time.Instant;
import java.util.UUID;

public class Job {
    public enum State {
        PENDING, PROCESSING, COMPLETED, FAILED, DEAD
    }

    private String id;
    private String command;
    private State state;
    private int attempts;
    private int maxRetries;
    private int priority;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant nextRetryAt;
    private Instant runAt;
    private Instant startedAt;
    private Long durationMillis;
    private Integer timeoutSec;
    private String errorMessage;
    private String output;
    private String logPath;

    public Job() {
    }

    public Job(String command, int maxRetries) {
        this.id = UUID.randomUUID().toString();
        this.command = command;
        this.state = State.PENDING;
        this.attempts = 0;
        this.maxRetries = maxRetries;
        this.priority = 0;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getCommand() {
        return command;
    }

    public State getState() {
        return state;
    }

    public int getAttempts() {
        return attempts;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public int getPriority() {
        return priority;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getNextRetryAt() {
        return nextRetryAt;
    }

    public Instant getRunAt() {
        return runAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Long getDurationMillis() {
        return durationMillis;
    }

    public Integer getTimeoutSec() {
        return timeoutSec;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getOutput() {
        return output;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setId(String id) {
        this.id = id;
        touch();
    }

    public void setCommand(String c) {
        this.command = c;
        touch();
    }

    public void setState(State s) {
        this.state = s;
        touch();
    }

    public void setMaxRetries(int m) {
        this.maxRetries = m;
        touch();
    }

    public void setPriority(int p) {
        this.priority = p;
        touch();
    }

    public void incrementAttempts() {
        this.attempts++;
        touch();
    }

    public void setAttempts(int a) {
        this.attempts = a;
        touch();
    }

    public void setNextRetryAt(Instant t) {
        this.nextRetryAt = t;
        touch();
    }

    public void setRunAt(Instant t) {
        this.runAt = t;
        touch();
    }

    public void setStartedAt(Instant t) {
        this.startedAt = t;
        touch();
    }

    public void setDurationMillis(Long d) {
        this.durationMillis = d;
        touch();
    }

    public void setTimeoutSec(Integer seconds) {
        this.timeoutSec = seconds;
        touch();
    }

    public void setErrorMessage(String m) {
        this.errorMessage = m;
        touch();
    }

    public void setOutput(String o) {
        this.output = o;
        touch();
    }

    public void setLogPath(String path) {
        this.logPath = path;
        touch();
    }

    public boolean canRetry() {
        return attempts < maxRetries;
    }

    public boolean ready() {
        Instant now = Instant.now();
        boolean retryOk = (nextRetryAt == null || now.isAfter(nextRetryAt));
        boolean scheduleOk = (runAt == null || !now.isBefore(runAt));
        return state == State.PENDING && retryOk && scheduleOk;
    }

    public void resetForRetry() {
        this.state = State.PENDING;
        this.attempts = 0;
        this.nextRetryAt = null;
        this.runAt = null;
        this.errorMessage = null;
        this.startedAt = null;
        this.durationMillis = null;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }
}
