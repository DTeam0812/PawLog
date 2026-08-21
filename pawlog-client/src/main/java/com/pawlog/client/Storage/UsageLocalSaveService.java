package com.pawlog.client.Storage;

import java.sql.SQLException;
import java.util.Map;

import com.pawlog.Sync.Usagebatch;
import com.pawlog.Sync.UsagebatchFactory;
import com.pawlog.client.tracker.ProgramTracker;
import com.pawlog.client.tracker.UsageKey;

public class UsageLocalSaveService {
    private final UsageOutboxRepository repository;

    public UsageLocalSaveService() {
        repository = new UsageOutboxRepository();
    }

    public boolean savePending() throws SQLException {
        Map<UsageKey, Long> data =
            ProgramTracker.drainPendingUsage();

        if (data.isEmpty()) {
            return false;
        }

        try {
            Usagebatch batch =
                UsagebatchFactory.create(data);

            repository.save(batch);
            return true;
        } catch (SQLException | RuntimeException exception) {
            ProgramTracker.restorePendingUsage(data);
            throw exception;
        }
    }
}