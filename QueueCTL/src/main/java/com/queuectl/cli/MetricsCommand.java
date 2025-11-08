package com.queuectl.cli;

import com.queuectl.service.MetricsService;
import picocli.CommandLine.Command;

@Command(name = "metrics", description = "Show queue metrics")
public class MetricsCommand implements Runnable {
    @Override
    public void run() {
        MetricsService.Summary s = new MetricsService().summary();
        System.out.println("total=" + s.total +
                " pending=" + s.pending +
                " processing=" + s.processing +
                " completed=" + s.completed +
                " failed=" + s.failed +
                " dead=" + s.dead +
                " avg_ms=" + String.format("%.0f", s.avgDurationMs));
    }
}
