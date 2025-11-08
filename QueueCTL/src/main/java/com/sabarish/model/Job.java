package com.sabarish.model;

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
    private Instant createdAt;
    private Instant updatedAt;
    private Instant nextRetryAt;
    private String errorMessage;
    private String output;

    public Job() {
    }

    public Job(String command, int maxRetries) {
        this.id = UUID.randomUUID().toString();
        this.command = command;
        this.state = State.PENDING;
        this.attempts = 0;
        this.maxRetries = maxRetries;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getNextRetryAt() {
        return nextRetryAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getOutput() {
        return output;
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

    public void setErrorMessage(String m) {
        this.errorMessage = m;
        touch();
    }

    public void setOutput(String o) {
        this.output = o;
        touch();
    }

    public boolean canRetry() {
        return attempts < maxRetries;
    }

    public boolean ready() {
        return state == State.PENDING && (nextRetryAt == null || Instant.now().isAfter(nextRetryAt));
    }

    public void resetForRetry() {
        this.state = State.PENDING;
        this.attempts = 0;
        this.nextRetryAt = null;
        this.errorMessage = null;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }
}
