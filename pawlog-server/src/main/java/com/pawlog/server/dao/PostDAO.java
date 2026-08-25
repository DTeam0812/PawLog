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

    // 7. 게시글 목록 검색/필터링 조건별 총 개수 조회
    public long getPostCount(long programId, String keyword) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        long count = 0;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM posts p WHERE ");
        if (programId <= 0) {
            sql.append("p.program_id IS NULL ");
        } else {
            sql.append("p.program_id = ? ");
        }

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        if (hasKeyword) {
            sql.append("AND (p.title LIKE ? OR p.content LIKE ?) ");
        }

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql.toString());

            int idx = 1;
            if (programId > 0) {
                pstmt.setLong(idx++, programId);
            }
            if (hasKeyword) {
                String searchPattern = "%" + keyword.trim() + "%";
                pstmt.setString(idx++, searchPattern);
                pstmt.setString(idx++, searchPattern);
            }

            rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getLong(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }

        return count;
    }

    // 8. 게시글 목록 조회 (검색, 정렬, 페이징, 작성자 닉네임, 좋아요수, 댓글수, 사용자 좋아요 여부 포함)
    public List<com.pawlog.common.dto.board.PostListDto.PostItem> getPostList(
            long programId, String keyword, String sort, int offset, int limit, Long currentUserId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<com.pawlog.common.dto.board.PostListDto.PostItem> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("  p.post_id, ");
        sql.append("  p.user_id, ");
        sql.append("  p.program_id, ");
        sql.append("  u.nickname AS author_nickname, ");
        sql.append("  p.title, ");
        sql.append("  p.created_at, ");
        sql.append("  (SELECT COUNT(*) FROM post_likes pl WHERE pl.post_id = p.post_id) AS like_count, ");
        sql.append("  (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.post_id) AS comment_count, ");
        if (currentUserId != null) {
            sql.append("  (SELECT COUNT(*) FROM post_likes pl2 WHERE pl2.post_id = p.post_id AND pl2.user_id = ?) AS user_liked ");
        } else {
            sql.append("  0 AS user_liked ");
        }
        sql.append("FROM posts p ");
        sql.append("LEFT JOIN users u ON p.user_id = u.user_id ");
        sql.append("WHERE ");
        if (programId <= 0) {
            sql.append("p.program_id IS NULL ");
        } else {
            sql.append("p.program_id = ? ");
        }

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        if (hasKeyword) {
            sql.append("AND (p.title LIKE ? OR p.content LIKE ?) ");
        }

        if (sort != null && ("OLDEST".equalsIgnoreCase(sort) || "ASC".equalsIgnoreCase(sort))) {
            sql.append("ORDER BY p.created_at ASC ");
        } else {
            sql.append("ORDER BY p.created_at DESC ");
        }

        sql.append("LIMIT ? OFFSET ?");

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql.toString());

            int idx = 1;
            if (currentUserId != null) {
                pstmt.setLong(idx++, currentUserId);
            }
            if (programId > 0) {
                pstmt.setLong(idx++, programId);
            }
            if (hasKeyword) {
                String searchPattern = "%" + keyword.trim() + "%";
                pstmt.setString(idx++, searchPattern);
                pstmt.setString(idx++, searchPattern);
            }
            pstmt.setInt(idx++, limit);
            pstmt.setInt(idx++, offset);

            rs = pstmt.executeQuery();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

            while (rs.next()) {
                com.pawlog.common.dto.board.PostListDto.PostItem item = new com.pawlog.common.dto.board.PostListDto.PostItem();
                item.setPostId(rs.getLong("post_id"));
                item.setUserId(rs.getLong("user_id"));
                item.setProgramId(rs.getLong("program_id"));
                item.setAuthorNickname(rs.getString("author_nickname") != null ? rs.getString("author_nickname") : "");
                item.setTitle(rs.getString("title"));

                java.sql.Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) {
                    item.setCreatedAt(ts.toLocalDateTime().format(formatter));
                } else {
                    item.setCreatedAt("");
                }

                item.setLikeCount(rs.getInt("like_count"));
                item.setCommentCount(rs.getInt("comment_count"));
                item.setLiked(rs.getInt("user_liked") > 0);

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