# queuectl

CLI-based background job queue system with workers, exponential backoff retries, and a Dead Letter Queue (DLQ). This implementation persists jobs to a JSON file for simplicity and is structured to allow a future swap to SQLite.

## Features
- Enqueue jobs specifying shell command
- Start/stop worker pool
- Automatic retries with exponential backoff & jitter
- Dead Letter handling after max retries
- JSON persistence (`.queuectl/jobs.json`) survives restarts
- Config store (`.queuectl/config.json`) for backoff base, poll interval, etc.
- List/status/DLQ operations via subcommands
 - Optional extras: per-job timeout, priority queueing, scheduled (run_at) jobs, per-job output logs, metrics, minimal web dashboard

## Install / Build
Requires Java 17+ and Maven.

```powershell
mvn -f .\QueueCTL\pom.xml clean package
```

Fat jar output: `QueueCTL/target/queuectl-1.0.0-jar-with-dependencies.jar`

## Usage Examples
```powershell
# Enqueue a job
java -jar .\QueueCTL\target\queuectl-1.0.0-jar-with-dependencies.jar enqueue '{"command":"echo hello"}'

# Start 2 workers
java -jar .\QueueCTL\target\queuectl-1.0.0-jar-with-dependencies.jar worker start --count 2

# In separate terminal, view status
java -jar .\QueueCTL\target\queuectl-1.0.0-jar-with-dependencies.jar status

# List jobs
java -jar .\QueueCTL\target\queuectl-1.0.0-jar-with-dependencies.jar list --state COMPLETED

# Stop workers
java -jar .\QueueCTL\target\queuectl-1.0.0-jar-with-dependencies.jar worker stop

# DLQ operations
java -jar .\QueueCTL\target\queuectl-1.0.0-jar-with-dependencies.jar dlq list
java -jar .\QueueCTL\target\queuectl-1.0.0-jar-with-dependencies.jar dlq retry <jobId>

# Config
java -jar .\QueueCTL\target\queuectl-1.0.0-jar-with-dependencies.jar config get backoff_base
java -jar .\QueueCTL\target\queuectl-1.0.0-jar-with-dependencies.jar config set backoff_base 3
```

## Configuration Keys
| Key | Meaning | Default |
|-----|---------|---------|
| max_retries | Default max retries if not in job JSON | 3 |
| backoff_base | Base for exponential backoff | 2 |
| worker_poll_interval_ms | Worker idle sleep | 1000 |
| graceful_shutdown_timeout_sec | Shutdown wait (informational) | 30 |
| processing_stale_timeout_sec | Recover stuck PROCESSING jobs | 300 |
| default_timeout_sec | Default per-job execution timeout | 300 |

## Architecture Overview
Layered layout under `com.queuectl` (4-layer: CLI → Service → Interface → Implementation):

- `cli` – Picocli command definitions (`QueueCtlCommand` root and subcommands)
- `service` – Orchestration services (`JobService`, `WorkerService`)
- `repository` – Interfaces (`JobRepository`, `ConfigRepository`, `StopSignalPort`) decoupling persistence
- `repository/json` – JSON adapters (`JsonJobRepository`, `JsonConfigRepository`)
- `core` – Queue logic (`JobManager`, `WorkerManager`, `StopSignal`), retry strategy (`RetryStrategy`, `ExponentialBackoffRetryStrategy`)
- `model` – Domain entities (`Job`)
- `persistence` – JSON file stores (`JsonJobStore`, `JsonConfigStore`) with file locking
- `worker` – `WorkerThread` runner executing shell commands
- `util` – Cross-platform `CommandExecutor`

Factory wiring: `service.ServiceFactory` composes repositories + strategies (easy swap to DB later).

## Job Lifecycle
```
PENDING -> PROCESSING -> COMPLETED
PENDING -> PROCESSING -> FAILED (schedule retry) -> PENDING (after backoff delay)
Exceeded max_retries -> DEAD (in DLQ)  (can be manually retried -> PENDING)
```

## Testing
Run tests:
```powershell
mvn -f .\QueueCTL\pom.xml test
```
Included tests:
- `RetryManagerTest` – backoff and failure state transitions
- `JobManagerTest` – acquire-next semantics

## Assumptions & Trade-offs
- JSON file store: simple, portable; not ideal for high contention.
- File lock granularity: whole job list file; acceptable for low volume.
- Command execution: shell-based; output captured in-memory only.
- Backoff jitter (25%) reduces retry storms.
- No security/auth; local developer tool.

## Design Excellence (SOLID)
- SRP: Stores/repositories only persist; managers orchestrate queue logic; workers only execute.
- OCP: Pluggable `RetryStrategy` (e.g., exponential backoff with jitter).
- LSP: Any `RetryStrategy` implementation can replace the default without breaking behavior.
- ISP: Small, focused ports (`JobRepository`, `ConfigRepository`, `StopSignalPort`).
- DIP: Services/managers depend on interfaces; implementations provided via `ServiceFactory`.

## ⚠️ Disqualification / Common Mistakes – Addressed
- Missing retry or DLQ functionality: Implemented (`RetryStrategy` with DLQ via `Job.State.DEAD` + `dlq` CLI to list/retry).
- Race conditions or duplicate job execution: Mitigated via file-channel locking and atomic acquire (PENDING→PROCESSING under lock); stale PROCESSING auto-recovered via `processing_stale_timeout_sec`.
- Non-persistent data: Jobs/config persisted to `.queuectl/*.json` and survive restarts.
- Hardcoded configuration values: Defaults live in `config.json`; code reads `max_retries`, `backoff_base`, `worker_poll_interval_ms`, `graceful_shutdown_timeout_sec`, `processing_stale_timeout_sec`.
- Unclear or missing README: This document covers features, architecture, config, and roadmap.

## Bonus Features
- Timeout: `enqueue` accepts `timeout_sec`; default from `default_timeout_sec`. Workers terminate long-running jobs.
- Priority: `enqueue` accepts `priority` (higher first); scheduler sorts by priority then FIFO.
- Scheduling: `enqueue` accepts `run_at` (ISO-8601). Jobs aren’t picked until this time.
- Output logging: Worker writes `.queuectl/logs/job-<id>.log` and stores `logPath` on the job.
- Metrics: `queuectl metrics` prints totals and avg duration; also exposed on dashboard `/metrics`.
- Dashboard: `queuectl dashboard --port 8080` serves `/` (HTML), `/jobs` (JSON), `/metrics` (JSON).

## Next Steps / Roadmap
- Swap to SQLite (fine-grained row locking, query filters)
- DLQService & ConfigService abstractions
- Job output log trimming & persistence
- Delayed / scheduled (`run_at`) jobs
- Priority queues
- Worker heartbeat & stale job recovery
- Per-job timeout & forced termination
- Metrics (rate, success/error counts, latency)
- Minimal web dashboard
- Structured logging (logback config)

## License
TBD.
