package com.sabarish.cli;

import picocli.CommandLine.Command;

@Command(name = "queuectl", mixinStandardHelpOptions = true, version = "queuectl 0.1", description = "CLI-based background job queue system", subcommands = {
        EnqueueCommand.class,
        WorkerCommand.class,
        StatusCommand.class,
        ListCommand.class,
        DlqCommand.class,
        ConfigCommand.class
})
public class QueueCtlCommand implements Runnable {
    @Override
    public void run() {
        
    }
}
