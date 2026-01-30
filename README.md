# queuectl

A CLI-based background job queue system with workers, automatic retries, and a Dead Letter Queue (DLQ).

## Table of Contents

- [Features](#features)
- [Setup](#setup)
- [Quick Start](#quick-start)
- [Commands](#commands)
- [Configuration](#configuration)
- [How It Works](#how-it-works)
- [Testing](#testing)

## Features

- **Job Queue** – Enqueue shell commands to run in the background
- **Worker Pool** – Start multiple workers to process jobs in parallel
- **Auto Retries** – Failed jobs retry automatically with exponential backoff
- **Dead Letter Queue** – Jobs that exceed max retries go to DLQ for manual review
- **Persistence** – Jobs saved to JSON file, survives restarts
- **Extras** – Priority, scheduling, timeouts, metrics, and web dashboard

## Setup

**Requirements:** Java 17+, Maven

```powershell
mvn -f .\QueueCTL\pom.xml clean package
```

This creates: `QueueCTL/target/queuectl-1.0.0-shaded.jar`

## Quick Start

```powershell
# 1. Start workers (processes jobs in background)
java -jar .\QueueCTL\target\queuectl-1.0.0-shaded.jar worker start --count 2 --no-block

# 2. Add a job to the queue
java -jar .\QueueCTL\target\queuectl-1.0.0-shaded.jar enqueue --command "echo hello"

# 3. Check queue status
java -jar .\QueueCTL\target\queuectl-1.0.0-shaded.jar status

# 4. Stop workers when done
java -jar .\QueueCTL\target\queuectl-1.0.0-shaded.jar worker stop
```

## Commands

| Command | Description |
|---------|-------------|
| `status` | Show job counts by state |
| `enqueue --command "..."` | Add a new job |
| `list --state STATE` | List jobs (PENDING, COMPLETED, FAILED, DEAD) |
| `worker start --count N` | Start N workers |
| `worker stop` | Stop all workers |
| `dlq list` | Show failed jobs in dead letter queue |
| `dlq retry <id>` | Retry a failed job |
| `config get/set <key>` | View or change settings |
| `metrics` | Show job statistics |
| `dashboard --port 8080` | Launch web dashboard |

## Configuration

Use `config get <key>` or `config set <key> <value>` to manage settings.

| Key | What it does | Default |
|-----|--------------|---------|
| `max_retries` | How many times to retry a failed job | 3 |
| `backoff_base` | Multiplier for retry delay | 2 |
| `default_timeout_sec` | Max time a job can run | 300 |
| `worker_poll_interval_ms` | How often workers check for jobs | 1000 |

## How It Works

```
PENDING → PROCESSING → COMPLETED ✓
              ↓
           FAILED → retries → PENDING (try again)
              ↓
            DEAD (gave up, check DLQ)
```

1. Jobs start as **PENDING**
2. A worker picks it up → **PROCESSING**
3. If successful → **COMPLETED**
4. If failed → retries with increasing delay
5. If max retries exceeded → **DEAD** (moved to DLQ)

## Testing

```powershell
# Run unit tests
mvn -f .\QueueCTL\pom.xml test

# Run smoke test (end-to-end demo)
.\QueueCTL\scripts\smoke.ps1
```
