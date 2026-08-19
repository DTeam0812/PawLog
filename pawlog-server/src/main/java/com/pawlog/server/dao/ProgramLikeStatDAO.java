package com.pawlog.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.pawlog.server.db.DBConnectionMgr;
import com.pawlog.server.entity.ProgramLikeStatEntity;

public class ProgramLikeStatDAO {
    private final DBConnectionMgr pool;

    public ProgramLikeStatDAO() {
        this.pool = DBConnectionMgr.getInstance();
    }

    // 1. 전체 동기화
    public boolean syncAllProgramLikeStats() {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO program_like_stats (program_id, like_count) "
                + "SELECT p.program_id, COALESCE(COUNT(pl.user_id), 0) AS like_count "
                + "FROM programs p "
                + "LEFT JOIN program_likes pl ON p.program_id = pl.program_id "
                + "GROUP BY p.program_id "
                + "ON DUPLICATE KEY UPDATE "
                + "like_count = VALUES(like_count)";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            int updatedRows = pstmt.executeUpdate();
            //System.out.println("[DB] 좋아요 집계 테이블 전체 동기화 완료 (반영 행 수: " + updatedRows + ")");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }


    // 2. 단일 프로그램 동기화
    public boolean syncSingleProgramLikeStats(long programId) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO program_like_stats (program_id, like_count) "
                + "VALUES (?, (SELECT COUNT(*) FROM program_likes WHERE program_id = ?)) "
                + "ON DUPLICATE KEY UPDATE "
                + "like_count = VALUES(like_count)";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, programId);
            pstmt.setLong(2, programId);
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }


    // 3. 단건 조회
    public long getLikeCountByProgramId(long programId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT like_count FROM program_like_stats WHERE program_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, programId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("like_count");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return 0L;
    }


    // 4. 프로그램 좋아요 내림차순 목록 조회
    public List<ProgramLikeStatEntity> getTopLikedPrograms(int limit) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<ProgramLikeStatEntity> list = new ArrayList<>();
        String sql = "SELECT program_id, like_count FROM program_like_stats ORDER BY like_count DESC LIMIT ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, limit);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ProgramLikeStatEntity entity = new ProgramLikeStatEntity();
                entity.setProgramId(rs.getLong("program_id"));
                entity.setLikeCount(rs.getLong("like_count"));
                list.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return list;
    }
}