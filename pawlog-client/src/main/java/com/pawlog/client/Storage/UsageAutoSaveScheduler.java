package com.pawlog.client.Storage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class UsageAutoSaveScheduler {
    private final UsageLocalSaveService localSaveService =
        new UsageLocalSaveService();

    private ScheduledExecutorService scheduler;

    public synchronized void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleWithFixedDelay(
            this::saveToSQLite,
            5,
            5,
            TimeUnit.SECONDS
        );
    }

    private void saveToSQLite() {
        try {
            boolean saved = localSaveService.savePending();

            if (saved) {
                System.out.println("USE SQL SUCCESS");
            }
        } catch (Exception exception) {
            System.out.println("USE SQL FAIL");
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