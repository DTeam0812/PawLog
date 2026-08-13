package com.pawlog.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.pawlog.server.db.DBConnectionMgr;
import com.pawlog.server.entity.PostLikeEntity;

public class PostLikeDAO {
    private DBConnectionMgr pool;

    public PostLikeDAO() {
        pool = DBConnectionMgr.getInstance();
    }

    // 1. 좋아요 추가
    public boolean insertLike(PostLikeEntity entity) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO post_likes (user_id, post_id) VALUES (?, ?)";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, entity.getUserId());
            pstmt.setLong(2, entity.getPostId());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    // 2. 좋아요 취소
    public boolean deleteLike(long userId, long postId) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "DELETE FROM post_likes WHERE user_id = ? AND post_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, userId);
            pstmt.setLong(2, postId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    // 3. 특정 사용자가 특정 게시글에 좋아요를 눌렀는지 확인
    public boolean isLiked(long userId, long postId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean liked = false;

        String sql = "SELECT COUNT(*) FROM post_likes WHERE user_id = ? AND post_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, userId);
            pstmt.setLong(2, postId);
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
    public int getLikeCountByPostId(long postId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;

        String sql = "SELECT COUNT(*) FROM post_likes WHERE post_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, postId);
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