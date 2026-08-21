package com.pawlog.client.Storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.pawlog.Device.DeviceIdManager;
import com.pawlog.Sync.Usagebatch;
import com.pawlog.Sync.Usageitem;

public class SQLiteConnectionTest {
    public static void main(String[] args) {
        try {
            SQLiteInitializer.initialize();

            Usagebatch batch = createTestBatch();

            UsageOutboxRepository repository = new UsageOutboxRepository();
            repository.save(batch);

            System.out.println("SQLite 저장 성공");
            showSavedBatch(batch.batchId());
        } catch (Exception exception) {
            System.out.println("SQLite 테스트 실패");
            exception.printStackTrace();
        }
    }

    private static Usagebatch createTestBatch() {
        String deviceId = DeviceIdManager.getDeviceId();
        String batchId = UUID.randomUUID().toString();

        List<Usageitem> items = List.of(
            new Usageitem(LocalDate.now(), 10, "chrome.exe", 120),
            new Usageitem(LocalDate.now(), 10, "Code.exe", 300),
            new Usageitem(LocalDate.now(), 11, "discord.exe", 60)
        );

        return new Usagebatch(
            deviceId,
            batchId,
            "sqlite-test@pawlog.local",
            items
        );
    }

    private static void showSavedBatch(String batchId) throws SQLException {
        String sql = """
            SELECT
                b.batch_id,
                b.device_id,
                b.email,
                b.status,
                i.usage_date,
                i.hour,
                i.program_name,
                i.duration_sec
            FROM usage_batch b
            JOIN usage_item i
                ON b.batch_id = i.batch_id
            WHERE b.batch_id = ?
            ORDER BY i.item_id
            """;

        try (Connection connection = SQLiteConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, batchId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    System.out.println("--------------------");
                    System.out.println("배치 ID: " + result.getString("batch_id"));
                    System.out.println("이메일: " + result.getString("email"));
                    System.out.println("상태: " + result.getString("status"));
                    System.out.println("날짜: " + result.getString("usage_date"));
                    System.out.println("시간대: " + result.getInt("hour"));
                    System.out.println("프로그램: " + result.getString("program_name"));
                    System.out.println("사용시간: " + result.getLong("duration_sec"));
                }
            }
        }
    }
}