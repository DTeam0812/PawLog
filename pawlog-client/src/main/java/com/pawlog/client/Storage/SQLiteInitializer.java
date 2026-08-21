package com.pawlog.client.Storage;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteInitializer {
    private SQLiteInitializer() {

    }

    public static void initialize() throws SQLException {
        String createBatchTable = """
            CREATE TABLE IF NOT EXISTS usage_batch (
                batch_id TEXT PRIMARY KEY,
                device_id TEXT NOT NULL,
                email TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'PENDING',
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """;

        String createItemTable = """
            CREATE TABLE IF NOT EXISTS usage_item (
                item_id INTEGER PRIMARY KEY AUTOINCREMENT,
                batch_id TEXT NOT NULL,
                usage_date TEXT NOT NULL,
                hour INTEGER NOT NULL,
                program_name TEXT NOT NULL,
                duration_sec INTEGER NOT NULL,
                FOREIGN KEY (batch_id)
                    REFERENCES usage_batch(batch_id)
                    ON DELETE CASCADE
            )
            """;

        try (Connection connection = SQLiteConnectionManager.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute(createBatchTable);
            statement.execute(createItemTable);
        }
    }
}