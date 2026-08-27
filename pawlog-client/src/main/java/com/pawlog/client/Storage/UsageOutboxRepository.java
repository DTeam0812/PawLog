package com.pawlog.client.Storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.pawlog.Sync.Usagebatch;
import com.pawlog.Sync.Usageitem;

public class UsageOutboxRepository {
    
     public void save(Usagebatch batch) throws SQLException {
        if (batch == null) {
            throw new IllegalArgumentException("저장할 배치가 없습니다.");
        }

        if (batch.logs() == null || batch.logs().isEmpty()) {
            throw new IllegalArgumentException("저장할 사용 기록이 없습니다.");
        }

        try (Connection connection = SQLiteConnectionManager.getConnection()) {
            connection.setAutoCommit(false);

            try {
                InsertBatch(connection, batch);
                insertItems(connection, batch);
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }
    public void InsertBatch(Connection connection, Usagebatch batch)throws SQLException {
        String sql = """
                INSERT INTO usage_batch (batch_id , device_id , id , status)
                VALUES (? , ? , ? , ? );
                """;
                try(PreparedStatement statement = connection.prepareStatement(sql)){
                    statement.setString(1, batch.batchId());
                    statement.setString(2, batch.deviceId());
                    statement.setString(3, batch.id());
                    statement.setString(4, "PENDING");
                    statement.executeUpdate();
                }
               
    }

    private void insertItems(Connection connection, Usagebatch batch) throws SQLException {
        String sql = """
            INSERT INTO usage_item (
                batch_id,
                usage_date,
                hour,
                program_name,
                duration_sec
            ) VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for(Usageitem item : batch.logs()) {
                statement.setString(1, batch.batchId());
                statement.setString(2, item.usageDate().toString());
                statement.setInt(3, item.hour());
                statement.setString(4, item.processName());
                statement.setLong(5, item.durationSec());
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }
    public List<Usagebatch> findPending() throws SQLException {
    List<Usagebatch> batchHeaders = new ArrayList<>();
        
    String sql = """
        SELECT
            batch_id,
            device_id,
            id
        FROM usage_batch
        WHERE status = ?
        ORDER BY created_at
        """;

    try (Connection connection = SQLiteConnectionManager.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setString(1, "PENDING");

        try (ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Usagebatch batchHeader = new Usagebatch(
                    result.getString("device_id"),
                    result.getString("batch_id"),
                    result.getString("id"),
                    List.of()
                );

                batchHeaders.add(batchHeader);
            }
        }

        List<Usagebatch> batches = new ArrayList<>();

        for(Usagebatch header : batchHeaders) {
            List<Usageitem> items = findItems(connection, header.batchId());

            Usagebatch batch = new Usagebatch(
                header.deviceId(),
                header.batchId(),
                header.id(),
                items
            );

            batches.add(batch);
        }

        return batches;
    }
}
private List<Usageitem> findItems(Connection connection, String batchId) throws SQLException {
    List<Usageitem> items = new ArrayList<>();

    String sql = """
        SELECT
            usage_date,
            hour,
            program_name,
            duration_sec
        FROM usage_item
        WHERE batch_id = ?
        ORDER BY item_id
        """;

    try (PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.setString(1, batchId);

        try (ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Usageitem item = new Usageitem(
                    LocalDate.parse(result.getString("usage_date")),
                    result.getInt("hour"),
                    result.getString("program_name"),
                    result.getLong("duration_sec")
                );

                items.add(item);
            }
        }
    }

    return items;
}
public boolean delete(String batchId) throws SQLException {
    String sql = """
        DELETE FROM usage_batch
        WHERE batch_id = ?
        """;

    try (Connection connection = SQLiteConnectionManager.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setString(1, batchId);

        int deletedCount = statement.executeUpdate();
        return deletedCount > 0;
    }
}

}
