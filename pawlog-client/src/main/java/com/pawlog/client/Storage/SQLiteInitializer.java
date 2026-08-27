package com.pawlog.client.Storage;

import java.sql.Connection;
import java.sql.ResultSet;
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
                id TEXT NOT NULL,
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
            migrateEmailColumnToId(connection);
            statement.execute(createItemTable);
        }
    }

    private static void migrateEmailColumnToId(Connection connection) throws SQLException {
        boolean hasId = false;
        boolean hasLegacyEmail = false;

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA table_info(usage_batch)")) {

            while(result.next()) {
                String columnName = result.getString("name");

                if("id".equalsIgnoreCase(columnName)) {
                    hasId = true;
                }

                if("email".equalsIgnoreCase(columnName)) {
                    hasLegacyEmail = true;
                }
            }
        }

        if(!hasId && hasLegacyEmail) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE usage_batch RENAME COLUMN email TO id");
            }
        }
    }
}
