package com.pawlog.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.pawlog.server.db.DBConnectionMgr;
import com.pawlog.server.entity.CommentEntity;

public class CommentDAO {
    private DBConnectionMgr pool;

    public CommentDAO() {
        pool = DBConnectionMgr.getInstance();
    }

    // 1. 댓글 작성
    public boolean insertComment(CommentEntity entity) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO comments (post_id, user_id, content) VALUES (?, ?, ?)";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, entity.getPostId());
            pstmt.setLong(2, entity.getUserId());
            pstmt.setString(3, entity.getContent());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    // 2. 특정 게시글의 댓글 목록 조회 (작성일 내림차순)
    public List<CommentEntity> getCommentsByPostId(long postId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<CommentEntity> list = new ArrayList<>();

        String sql = "SELECT comment_id, post_id, user_id, content, created_at "
                + "FROM comments WHERE post_id = ? ORDER BY created_at DESC";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, postId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                CommentEntity entity = new CommentEntity();
                entity.setCommentId(rs.getLong("comment_id"));
                entity.setPostId(rs.getLong("post_id"));
                entity.setUserId(rs.getLong("user_id"));
                entity.setContent(rs.getString("content"));
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

    // 3. 댓글 수정 (작성자 본인 확인 포함)
    public boolean updateComment(long commentId, long userId, String content) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "UPDATE comments SET content = ? WHERE comment_id = ? AND user_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, content);
            pstmt.setLong(2, commentId);
            pstmt.setLong(3, userId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    // 4. 댓글 삭제 (작성자 본인 확인 포함)
    public boolean deleteComment(long commentId, long userId) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "DELETE FROM comments WHERE comment_id = ? AND user_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, commentId);
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
}