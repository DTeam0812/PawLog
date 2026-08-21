package com.pawlog.client.Storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteConnectionManager {
    private SQLiteConnectionManager() {

    }

    private static Path getDatabasePath() {
        String localAppData = System.getenv("LOCALAPPDATA");

        if (localAppData == null || localAppData.isBlank()) {
            throw new IllegalStateException("LOCALAPPDATA 경로를 찾을 수 없습니다.");
        }
        return Path.of(localAppData, "PawLog", "pawlog.db");
    }
    public static Connection getConnection() throws SQLException {
        Path databasePath = getDatabasePath();
        try {
            Files.createDirectories(databasePath.getParent());
        } catch (IOException exception) {
            throw new SQLException("SQLite 폴더를 만들지 못했습니다.", exception);
        }

        String databaseUrl = "jdbc:sqlite:" + databasePath;
        Connection connection = DriverManager.getConnection(databaseUrl);

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }
}