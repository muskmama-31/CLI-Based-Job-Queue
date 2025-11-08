package com.queuectl.service;

import com.google.gson.Gson;
import com.queuectl.model.Job;
import com.queuectl.core.JobManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;

public class DashboardService {
    private final JobManager jm;
    private final Gson gson = new Gson();
    private com.sun.net.httpserver.HttpServer server;

    public DashboardService(JobManager jm) {
        this.jm = jm;
    }

    public DashboardService() {
        this(ServiceFactory.jobManager());
    }

    public void start(int port) {
        if (server != null)
            return;
        try {
            server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/jobs", exchange -> {
                List<Job> jobs = jm.loadAll();
                byte[] data = gson.toJson(jobs).getBytes();
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, data.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(data);
                }
            });
            server.createContext("/metrics", exchange -> {
                MetricsService.Summary s = new MetricsService(jm).summary();
                byte[] data = gson.toJson(s).getBytes();
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, data.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(data);
                }
            });
            server.createContext("/", exchange -> {
                String body = "<html><body><h1>queuectl dashboard</h1><ul><li><a href='/jobs'>/jobs</a></li><li><a href='/metrics'>/metrics</a></li></ul></body></html>";
                byte[] data = body.getBytes();
                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
                exchange.sendResponseHeaders(200, data.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(data);
                }
            });
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }
}
