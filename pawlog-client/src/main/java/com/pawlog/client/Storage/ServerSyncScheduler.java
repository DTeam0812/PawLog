package com.pawlog.client.Storage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerSyncScheduler {
    private final UsageServerSyncService syncService =
        new UsageServerSyncService();

    private ScheduledExecutorService scheduler;

    public synchronized void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleWithFixedDelay(
            this::syncToServer,
            60,
            60,
            TimeUnit.SECONDS
        );
    }

    private void syncToServer() {
        try {
            syncService.syncPending();
            System.out.println("SYNC SUCCESS");
        } catch (Exception exception) {
            System.out.println("SYNC SUCCESS FAIL");
            exception.printStackTrace();
        }
    }

    public synchronized void stop() {
        if (scheduler == null) {
            return;
        }

        scheduler.shutdownNow();
        scheduler = null;
    }
}