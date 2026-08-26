package com.pawlog.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.pawlog.server.db.DBConnectionMgr;
import com.pawlog.server.entity.CommentEntity;

public class CommentDAO {
    private DBConnectionMgr pool;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public CommentDAO() {
        pool = DBConnectionMgr.getInstance();
    }

    // 1. 댓글 작성 및 생성된 comment_id 반환 (실패 시 -1)
    public long insertCommentAndGetId(CommentEntity entity) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "INSERT INTO comments (post_id, user_id, content, is_active) VALUES (?, ?, ?, TRUE)";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, entity.getPostId());
            pstmt.setLong(2, entity.getUserId());
            pstmt.setString(3, entity.getContent());

            int result = pstmt.executeUpdate();
            if (result > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return -1L;
    }

    // 2. 특정 게시글의 활성 댓글 총 개수 조회
    public int getCommentCountByPostId(long postId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;

        String sql = "SELECT COUNT(*) FROM comments WHERE post_id = ? AND (is_active = TRUE OR is_active IS NULL)";

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

    // 3. 댓글 삭제 (소프트 딜리트: is_active = FALSE)
    public boolean deleteComment(long commentId, long userId) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "UPDATE comments SET is_active = FALSE WHERE comment_id = ? AND user_id = ?";

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

    // 4. 특정 게시글의 댓글 목록 조회 (게시글과 동일한 시간 포맷 적용)
    public List<com.pawlog.common.dto.board.PostDetailDto.commentitem> getCommentItemsByPostId(long postId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<com.pawlog.common.dto.board.PostDetailDto.commentitem> list = new ArrayList<>();

        String sql = "SELECT u.nickname AS comment_author, c.content, c.created_at "
                + "FROM comments c "
                + "LEFT JOIN users u ON c.user_id = u.user_id "
                + "WHERE c.post_id = ? AND (c.is_active = TRUE OR c.is_active IS NULL) "
                + "ORDER BY c.created_at ASC";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, postId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                com.pawlog.common.dto.board.PostDetailDto.commentitem item = new com.pawlog.common.dto.board.PostDetailDto.commentitem();
                item.setCommentAuthor(rs.getString("comment_author") != null ? rs.getString("comment_author") : "알 수 없음");
                item.setCommentcontent(rs.getString("content") != null ? rs.getString("content") : "");

                // 게시글(PostDAO)과 동일한 포맷으로 변환
                java.sql.Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) {
                    item.setCommenttime(ts.toLocalDateTime().format(FORMATTER));
                } else {
                    item.setCommenttime("");
                }

                list.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }

        return list;
    }
}