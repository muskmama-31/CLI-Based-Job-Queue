package com.queuectl.cli;

import picocli.CommandLine.Command;

@Command(name = "queuectl", mixinStandardHelpOptions = true, version = "queuectl 1.0", description = "CLI-based background job queue system", subcommands = {
        EnqueueCommand.class,
        WorkerCommand.class,
        StatusCommand.class,
        ListCommand.class,
        DLQCommand.class,
        ConfigCommand.class,
        MetricsCommand.class,
        DashboardCommand.class
})
public class QueueCtlCommand implements Runnable {
    @Override
    public void run() {

    }
}
