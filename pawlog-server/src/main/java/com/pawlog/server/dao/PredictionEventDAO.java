package com.pawlog.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.pawlog.server.db.DBConnectionMgr;
import com.pawlog.server.entity.PredictionEventEntity;

public class PredictionEventDAO {
    private DBConnectionMgr pool;

    public PredictionEventDAO() {
        pool = DBConnectionMgr.getInstance();
    }

    // 1. 예측 이벤트 생성
    public boolean insertEvent(PredictionEventEntity entity) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO prediction_events (title, voting_deadline, status, target_date, event_type) VALUES (?, ?, ?, ?, ?)";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, entity.getTitle());
            pstmt.setTimestamp(2, entity.getVotingDeadline());
            pstmt.setString(3, entity.getStatus() != null ? entity.getStatus() : "OPEN");
            pstmt.setDate(4, entity.getTargetDate());
            pstmt.setString(5, entity.getEventType());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    // 2. 진행 중인(OPEN) 예측 이벤트 목록 조회
    public List<PredictionEventEntity> getOpenEvents() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<PredictionEventEntity> list = new ArrayList<>();

        String sql = "SELECT event_id, title, created_at, voting_deadline, status, target_date, event_type "
                + "FROM prediction_events WHERE status = 'OPEN' ORDER BY voting_deadline ASC";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                PredictionEventEntity entity = new PredictionEventEntity();
                entity.setEventId(rs.getLong("event_id"));
                entity.setTitle(rs.getString("title"));
                entity.setCreatedAt(rs.getTimestamp("created_at"));
                entity.setVotingDeadline(rs.getTimestamp("voting_deadline"));
                entity.setStatus(rs.getString("status"));
                entity.setTargetDate(rs.getDate("target_date"));
                entity.setEventType(rs.getString("event_type"));
                list.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return list;
    }

    // 3. 이벤트 상태 변경 ('CLOSED', 'ANNOUNCED' 등)
    public boolean updateEventStatus(long eventId, String status) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "UPDATE prediction_events SET status = ? WHERE event_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setLong(2, eventId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }
}