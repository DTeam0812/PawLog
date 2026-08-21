package com.pawlog.server.dao;

import com.pawlog.server.db.DBConnectionMgr;
import com.pawlog.server.entity.ProgramLikeEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ProgramLikeDAO {
    private DBConnectionMgr pool;

    public ProgramLikeDAO() {
        pool = DBConnectionMgr.getInstance();
    }


    // 1. 프로그램 좋아요 추가
    public boolean insertProgramLike(ProgramLikeEntity entity) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO programs_likes (program_id, user_id) VALUES (?, ?)";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, entity.getProgramId());
            pstmt.setLong(2, entity.getUserId());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    // 2. 프로그램 좋아요 취소
    public boolean deleteProgramLike(long userId, long programId) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "DELETE FROM programs_likes WHERE program_id = ? AND user_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, programId);
            pstmt.setLong(2, userId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    // 3. 특정 사용자가 특정 프로그램 좋아요를 눌렀는지 확인
    public boolean isProgramLiked(long userId, long programId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean liked = false;

        String sql = "SELECT COUNT(*) FROM programs_likes WHERE program_id = ? AND user_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, programId);
            pstmt.setLong(2, userId);
            rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                liked = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return liked;
    }

    // 4. 게시글별 총 좋아요 수 조회
    public int getLikeCountByProgramId(long programId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;

        String sql = "SELECT COUNT(*) FROM programs_likes WHERE program_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, programId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return count;
    }

}
