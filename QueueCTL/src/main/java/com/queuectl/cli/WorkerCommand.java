package com.queuectl.cli;

import com.queuectl.service.WorkerService;
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
        @Option(names = { "--no-block" }, description = "Do not block; start workers and exit")
        boolean noBlock;

        @Override
        public void run() {
            WorkerService svc = new WorkerService();
            svc.clearStopSignal();
            svc.startWorkers(count);
            System.out.println("Workers running. Stop with: queuectl worker stop");
            if (!noBlock) {
                try {
                    Thread.currentThread().join();
                } catch (InterruptedException ignored) {
                }
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
