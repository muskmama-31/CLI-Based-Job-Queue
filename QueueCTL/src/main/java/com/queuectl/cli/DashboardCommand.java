package com.queuectl.cli;

import com.queuectl.service.DashboardService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "dashboard", description = "Start a minimal web dashboard")
public class DashboardCommand implements Runnable {
    @Option(names = { "--port" }, defaultValue = "8080", description = "Port to bind")
    int port;

    @Override
    public void run() {
        DashboardService s = new DashboardService();
        s.start(port);
        System.out.println("Dashboard running on http://localhost:" + port + " (Ctrl+C to stop)");
        try {
            Thread.currentThread().join();
        } catch (InterruptedException ignored) {
        }
    }
}
