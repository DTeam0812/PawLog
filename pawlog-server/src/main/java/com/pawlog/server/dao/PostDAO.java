package com.pawlog.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.pawlog.server.db.DBConnectionMgr;
import com.pawlog.server.entity.PostEntity;

public class PostDAO {
    private DBConnectionMgr pool;

    public PostDAO() {
        pool = DBConnectionMgr.getInstance();
    }

    // 1. 게시글 작성 (program_id가 null이면 추천 프로그램 게시판)
    public boolean insertPost(PostEntity entity) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO posts (user_id, program_id, title, content) VALUES (?, ?, ?, ?)";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, entity.getUserId());

            if (entity.getProgramId() == null) {
                pstmt.setNull(2, Types.BIGINT);
            } else {
                pstmt.setLong(2, entity.getProgramId());
            }

            pstmt.setString(3, entity.getTitle());
            pstmt.setString(4, entity.getContent());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    // 2. 게시글 단건 상세 조회
    public PostEntity getPostById(long postId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT post_id, user_id, program_id, title, content, created_at FROM posts WHERE post_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, postId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                PostEntity entity = new PostEntity();
                entity.setPostId(rs.getLong("post_id"));
                entity.setUserId(rs.getLong("user_id"));

                long programId = rs.getLong("program_id");
                if (rs.wasNull()) {
                    entity.setProgramId(null);
                } else {
                    entity.setProgramId(programId);
                }

                entity.setTitle(rs.getString("title"));
                entity.setContent(rs.getString("content"));
                entity.setCreatedAt(rs.getTimestamp("created_at"));
                return entity;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return null;
    }

    // 3. 추천 프로그램 게시판 목록 조회 (program_id가 NULL인 게시글, 최신순, 페이징)
    public List<PostEntity> getFreeBoardPosts(int offset, int limit) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<PostEntity> list = new ArrayList<>();

        String sql = "SELECT post_id, user_id, program_id, title, content, created_at "
                + "FROM posts WHERE program_id IS NULL "
                + "ORDER BY created_at DESC LIMIT ? OFFSET ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                PostEntity entity = new PostEntity();
                entity.setPostId(rs.getLong("post_id"));
                entity.setUserId(rs.getLong("user_id"));
                entity.setProgramId(null);
                entity.setTitle(rs.getString("title"));
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

    // 4. 특정 프로그램 게시판 목록 조회 (program_id 기준, 최신순, 페이징)
    public List<PostEntity> getPostsByProgramId(long programId, int offset, int limit) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<PostEntity> list = new ArrayList<>();

        String sql = "SELECT post_id, user_id, program_id, title, content, created_at "
                + "FROM posts WHERE program_id = ? "
                + "ORDER BY created_at DESC LIMIT ? OFFSET ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, programId);
            pstmt.setInt(2, limit);
            pstmt.setInt(3, offset);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                PostEntity entity = new PostEntity();
                entity.setPostId(rs.getLong("post_id"));
                entity.setUserId(rs.getLong("user_id"));
                entity.setProgramId(rs.getLong("program_id"));
                entity.setTitle(rs.getString("title"));
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

    // 5. 게시글 수정 (작성자 본인 확인 포함)
    public boolean updatePost(PostEntity entity) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "UPDATE posts SET title = ?, content = ? WHERE post_id = ? AND user_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, entity.getTitle());
            pstmt.setString(2, entity.getContent());
            pstmt.setLong(3, entity.getPostId());
            pstmt.setLong(4, entity.getUserId());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    // 6. 게시글 삭제 (작성자 본인 확인 포함)
    public boolean deletePost(long postId, long userId) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "DELETE FROM posts WHERE post_id = ? AND user_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, postId);
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