package com.sabarish.cli;

import com.sabarish.service.WorkerService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "worker", description = "Manage workers", subcommands = {
        WorkerCommand.Start.class, WorkerCommand.Stop.class })
public class WorkerCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("Use subcommands: start|stop");
    }

    @Command(name = "start", description = "Start worker pool")
    static class Start implements Runnable {
        @Option(names = { "--count", "-c" }, defaultValue = "3", description = "Number of workers")
        int count;

        @Override
        public void run() {
            WorkerService svc = new WorkerService();
            svc.clearStopSignal();
            svc.startWorkers(count);
            System.out.println("Workers running. Press Ctrl+C to stop, or run: queuectl worker stop");
            try {
                Thread.currentThread().join();
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Command(name = "stop", description = "Signal workers to stop gracefully")
    static class Stop implements Runnable {
        @Override
        public void run() {
            WorkerService svc = new WorkerService();
            svc.stopWorkers();
            System.out.println("Stop signal written. Active workers will finish current jobs and exit.");
        }
    }
}
