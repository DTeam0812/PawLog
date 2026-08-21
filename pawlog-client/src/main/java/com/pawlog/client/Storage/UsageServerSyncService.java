package com.pawlog.client.Storage;

import java.util.List;

import com.pawlog.Sync.Usagebatch;
import com.pawlog.client.AuthStore.AuthStore;
import com.pawlog.client.network.PawLogClient;

public class UsageServerSyncService {
    private final UsageOutboxRepository repository;
    private final PawLogClient pawLogClient;

    public UsageServerSyncService() {
        repository = new UsageOutboxRepository();
        pawLogClient = PawLogClient.getInstance();
    }

    public int syncPending() throws Exception {
        String currentEmail =
            AuthStore.getInstance().getEmail();

        if (currentEmail == null || currentEmail.isBlank()) {
            return 0;
        }

        List<Usagebatch> batches =
            repository.findPending();

        int syncedCount = 0;

        for(Usagebatch batch : batches) {
            if (!currentEmail.equals(batch.email())) {
                continue;
            }

            PawLogClient.Response response =
                pawLogClient.saveUsage(batch);

            if (!response.success()) {
                break;
            }

            boolean deleted =
                repository.delete(batch.batchId());

            if (deleted) {
                syncedCount++;
            }
        }

        return syncedCount;
    }
}