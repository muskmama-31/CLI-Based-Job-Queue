package com.queuectl;

import com.queuectl.cli.QueueCtlCommand;
import picocli.CommandLine;

public class QueueCtlApp {
    public static void main(String[] args) {
        int code = new CommandLine(new QueueCtlCommand()).execute(args);
        System.exit(code);
    }
}
