package com.sabarish;

import com.sabarish.cli.QueueCtlCommand;
import picocli.CommandLine;

public class App {
    public static void main(String[] args) {
        int code = new CommandLine(new QueueCtlCommand()).execute(args);
        System.exit(code);
    }
}
