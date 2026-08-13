package com.pawlog.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.pawlog.server.db.DBConnectionMgr;
import com.pawlog.server.entity.PredictionVoteEntity;

public class PredictionVoteDAO {
    private DBConnectionMgr pool;

    public PredictionVoteDAO() {
        pool = DBConnectionMgr.getInstance();
    }

    // 1. 투표 참여 (uk_prediction_event_user 제약 조건으로 인해 중복 투표 시 Exception 발생)
    public boolean insertVote(PredictionVoteEntity entity) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO prediction_votes (event_id, user_id, program_id) VALUES (?, ?, ?)";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, entity.getEventId());
            pstmt.setLong(2, entity.getUserId());
            pstmt.setLong(3, entity.getProgramId());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    // 2. 특정 사용자가 특정 이벤트에 이미 투표했는지 확인
    public boolean hasVoted(long eventId, long userId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean voted = false;

        String sql = "SELECT COUNT(*) FROM prediction_votes WHERE event_id = ? AND user_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, eventId);
            pstmt.setLong(2, userId);
            rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                voted = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return voted;
    }

    // 3. 특정 사용자가 투표한 프로그램 ID 조회
    public PredictionVoteEntity getVoteByUserAndEvent(long eventId, long userId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT vote_id, event_id, user_id, program_id FROM prediction_votes WHERE event_id = ? AND user_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, eventId);
            pstmt.setLong(2, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                PredictionVoteEntity entity = new PredictionVoteEntity();
                entity.setVoteId(rs.getLong("vote_id"));
                entity.setEventId(rs.getLong("event_id"));
                entity.setUserId(rs.getLong("user_id"));
                entity.setProgramId(rs.getLong("program_id"));
                return entity;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return null;
    }
}