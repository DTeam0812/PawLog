package com.pawlog.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.pawlog.server.db.DBConnectionMgr;
import com.pawlog.server.entity.ProgramRecommendationEntity;

public class ProgramRecommendationDAO {
    private DBConnectionMgr pool;

    public ProgramRecommendationDAO() {
        pool = DBConnectionMgr.getInstance();
    }

    // 1. 프로그램 추천 기록 저장
    public boolean insertRecommendation(ProgramRecommendationEntity entity) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO program_recommendtations (user_id, program_id, reason) VALUES (?, ?, ?)";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, entity.getUserId());
            pstmt.setLong(2, entity.getProgramId());
            pstmt.setString(3, entity.getReason()); // NULL 가능

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    // 2. 특정 사용자의 추천 프로그램 목록 조회 (최신순)
    public List<ProgramRecommendationEntity> getRecommendationsByUserId(long userId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<ProgramRecommendationEntity> list = new ArrayList<>();

        String sql = "SELECT rec_id, user_id, program_id, reason, created_at "
                + "FROM program_recommendtations "
                + "WHERE user_id = ? "
                + "ORDER BY created_at DESC";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, userId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ProgramRecommendationEntity entity = new ProgramRecommendationEntity();
                entity.setRecId(rs.getLong("rec_id"));
                entity.setUserId(rs.getLong("user_id"));
                entity.setProgramId(rs.getLong("program_id"));
                entity.setReason(rs.getString("reason"));
                entity.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return list;
    }


    // 3. 추천 기록 삭제
    public boolean deleteRecommendation(long recId) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "DELETE FROM program_recommendtations WHERE rec_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, recId);

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