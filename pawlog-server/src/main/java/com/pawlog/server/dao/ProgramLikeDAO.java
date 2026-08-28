package com.pawlog.server.dao;

import com.pawlog.common.dto.recommend.RecommendProgramDto;
import com.pawlog.server.db.DBConnectionMgr;
import com.pawlog.server.entity.ProgramLikeEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProgramLikeDAO {
    private DBConnectionMgr pool;

    public ProgramLikeDAO() {
        pool = DBConnectionMgr.getInstance();
    }


    // 1. 프로그램 좋아요 추가
    public boolean insertProgramLike(ProgramLikeEntity entity) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO program_likes (program_id, user_id) VALUES (?, ?)";

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

        String sql = "DELETE FROM program_likes WHERE program_id = ? AND user_id = ?";

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

        String sql = "SELECT COUNT(*) FROM program_likes WHERE program_id = ? AND user_id = ?";

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

        String sql = "SELECT COUNT(*) FROM program_likes WHERE program_id = ?";

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

    // 5. 추천수가 많은 상위 프로그램 목록 조회
    public List<RecommendProgramDto.Item> getTopLikedPrograms(int limit) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<RecommendProgramDto.Item> list = new ArrayList<>();

        String sql = "SELECT p.program_id, "
                   + "COALESCE(NULLIF(p.display_name, ''), p.process_name) AS program_name, "
                   + "COUNT(pl.user_id) AS like_count "
                   + "FROM programs p "
                   + "LEFT JOIN program_likes pl ON p.program_id = pl.program_id "
                   + "GROUP BY p.program_id, p.display_name, p.process_name "
                   + "ORDER BY like_count DESC, p.program_id ASC "
                   + "LIMIT ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, limit > 0 ? limit : 3);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                String programId = String.valueOf(rs.getLong("program_id"));
                String programName = rs.getString("program_name");
                int likeCount = rs.getInt("like_count");

                list.add(new RecommendProgramDto.Item(programId, programName, likeCount));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return list;
    }

}
