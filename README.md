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

## Setup / Build
Requires Java 17+ and Maven.

```powershell
mvn -f .\QueueCTL\pom.xml clean package
```

Runnable fat jar: `QueueCTL/target/queuectl-1.0.0-shaded.jar`

## Usage Examples (with outputs)

```powershell
# Show status
java -jar .\QueueCTL\target\queuectl-1.0.0-shaded.jar status
```
Example:
```
Jobs:
	PENDING: 0
	PROCESSING: 0
	COMPLETED: 0
	FAILED: 0
	DEAD: 0
Stop signal: OFF
```

```powershell
# Start two workers (non-blocking)
java -jar .\QueueCTL\target\queuectl-1.0.0-shaded.jar worker start --count 2 --no-block
```
Output:
```
Workers running. Stop with: queuectl worker stop
```

```powershell
# Enqueue a job (PowerShell-friendly flags)
java -jar .\QueueCTL\target\queuectl-1.0.0-shaded.jar enqueue --command "echo hello" --priority 5
```
Output:
```
Job enqueued: <uuid>
```

```powershell
# Check status (after a short delay)
java -jar .\QueueCTL\target\queuectl-1.0.0-shaded.jar status
```
Example:
```
Jobs:
	PENDING: 0
	PROCESSING: 0
	COMPLETED: 1
	FAILED: 0
	DEAD: 0
Stop signal: OFF
```

```powershell
# List completed jobs
java -jar .\QueueCTL\target\queuectl-1.0.0-shaded.jar list --state COMPLETED
```
Example:
```
c9e2a3e0-...  [COMPLETED]  attempts=0  cmd="echo hello"  updated=2025-11-08T21:25:10Z
```

```powershell
# DLQ operations
java -jar .\QueueCTL\target\queuectl-1.0.0-shaded.jar dlq list
java -jar .\QueueCTL\target\queuectl-1.0.0-shaded.jar dlq retry <jobId>
```

```powershell
# Config
java -jar .\QueueCTL\target\queuectl-1.0.0-shaded.jar config get backoff_base
java -jar .\QueueCTL\target\queuectl-1.0.0-shaded.jar config set backoff_base 3
```

```powershell
# Metrics
java -jar .\QueueCTL\target\queuectl-1.0.0-shaded.jar metrics
```
Example:
```
total=2 pending=0 processing=0 completed=1 failed=0 dead=1 avg_ms=75
```

```powershell
# Dashboard (Ctrl+C to stop)
java -jar .\QueueCTL\target\queuectl-1.0.0-shaded.jar dashboard --port 8080
# open http://localhost:8080
```

```powershell
# Stop workers gracefully
java -jar .\QueueCTL\target\queuectl-1.0.0-shaded.jar worker stop
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

Smoke test script (Windows PowerShell):
```powershell
# Runs a small end-to-end demo using the shaded jar
.\n+QueueCTL\scripts\smoke.ps1
```
What it does:
- Builds the jar (unless skipped), resets `.queuectl`, starts workers non-blocking
- Enqueues a success and a fail-fast job, waits briefly, shows status/DLQ/metrics
- Stops workers gracefully

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


## Bonus Features
- Timeout: `enqueue` accepts `timeout_sec`; default from `default_timeout_sec`. Workers terminate long-running jobs.
- Priority: `enqueue` accepts `priority` (higher first); scheduler sorts by priority then FIFO.
- Scheduling: `enqueue` accepts `run_at` (ISO-8601). Jobs aren’t picked until this time.
- Output logging: Worker writes `.queuectl/logs/job-<id>.log` and stores `logPath` on the job.
- Metrics: `queuectl metrics` prints totals and avg duration; also exposed on dashboard `/metrics`.
- Dashboard: `queuectl dashboard --port 8080` serves `/` (HTML), `/jobs` (JSON), `/metrics` (JSON).

## License
TBD.
