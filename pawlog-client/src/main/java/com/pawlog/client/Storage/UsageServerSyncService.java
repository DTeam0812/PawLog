package com.pawlog.client.Storage;

import java.util.List;

import com.pawlog.Sync.Usagebatch;
import com.pawlog.client.AuthStore.AuthStore;
import com.pawlog.client.network.PawLogClient;

public class UsageServerSyncService {
    private static final Object SYNC_LOCK = new Object();

    private final UsageOutboxRepository repository;
    private final PawLogClient pawLogClient;

    public UsageServerSyncService() {
        repository = new UsageOutboxRepository();
        pawLogClient = PawLogClient.getInstance();
    }

    public int syncPending() throws Exception {
        synchronized(SYNC_LOCK) {
            return syncPendingLocked();
        }
    }

    private int syncPendingLocked() throws Exception {
        String currentId =
            AuthStore.getInstance().getId();

        if (currentId == null || currentId.isBlank()) {
            return 0;
        }

        List<Usagebatch> batches =
            repository.findPending();

        int syncedCount = 0;

        for(Usagebatch batch : batches) {
            if (!currentId.equals(batch.id())) {
                continue;
            }

            PawLogClient.Response response =
                pawLogClient.saveUsage(batch);

            if (!response.success()) {
                throw new IllegalStateException(
                    "사용 기록 전송 실패: " + response.message()
                );
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
