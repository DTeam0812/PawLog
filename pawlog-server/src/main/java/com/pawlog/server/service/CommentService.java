package com.pawlog.server.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.pawlog.common.dto.board.CommentDeleteDto;
import com.pawlog.common.dto.board.CommentDto;
import com.pawlog.server.dao.CommentDAO;
import com.pawlog.server.dao.PostDAO;
import com.pawlog.server.dao.UserDAO;
import com.pawlog.server.entity.CommentEntity;
import com.pawlog.server.entity.PostEntity;

public class CommentService {
    private final CommentDAO commentDAO;
    private final PostDAO postDAO;
    private final UserDAO userDAO;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public CommentService() {
        this.commentDAO = new CommentDAO();
        this.postDAO = new PostDAO();
        this.userDAO = new UserDAO();
    }

    public CommentDto.Response writeComment(CommentDto.Request reqDto, Long currentUserId) {
        if (currentUserId == null || currentUserId <= 0) {
            return new CommentDto.Response(false, 401, "로그인이 필요한 서비스입니다.");
        }

        if (reqDto == null || reqDto.getPostId() <= 0) {
            return new CommentDto.Response(false, 400, "유효하지 않은 게시글 번호입니다.");
        }

        String content = reqDto.getContent() != null ? reqDto.getContent().trim() : "";
        if (content.isEmpty()) {
            return new CommentDto.Response(false, 400, "댓글 내용을 입력해 주세요.");
        }

        if (content.length() > 500) {
            return new CommentDto.Response(false, 400, "댓글은 최대 500자까지 작성할 수 있습니다.");
        }

        try {
            // 1. 게시글 존재 및 활성 상태 검증 (404 방어)
            PostEntity post = postDAO.getPostById(reqDto.getPostId());
            if (post == null || (post.getIsActive() != null && !post.getIsActive())) {
                return new CommentDto.Response(false, 404, "존재하지 않거나 삭제된 게시글입니다.");
            }

            // 2. 댓글 INSERT
            CommentEntity entity = new CommentEntity();
            entity.setPostId(reqDto.getPostId());
            entity.setUserId(currentUserId);
            entity.setContent(content);

            long commentId = commentDAO.insertCommentAndGetId(entity);
            if (commentId <= 0) {
                return new CommentDto.Response(false, 500, "댓글 작성에 실패했습니다.");
            }

            // 3. 닉네임 및 최신 총 댓글 수 조회
            String nickname = userDAO.getNicknameByUserId(currentUserId);
            if (nickname == null || nickname.isEmpty()) {
                nickname = "사용자";
            }
            int totalCommentCount = commentDAO.getCommentCountByPostId(reqDto.getPostId());

            // 게시글과 동일한 ISO 포맷의 생성 시간 적용
            String createdAt = LocalDateTime.now().format(FORMATTER);

            CommentDto.CommentData commentData = new CommentDto.CommentData(
                    commentId,
                    reqDto.getPostId(),
                    currentUserId,
                    nickname,
                    content,
                    createdAt,
                    totalCommentCount
            );

            return new CommentDto.Response(true, 200, "댓글이 성공적으로 등록되었습니다.", commentData);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommentDto.Response(false, 500, "댓글 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 댓글 삭제 비즈니스 로직 추가
    public CommentDeleteDto.Response deleteComment(CommentDeleteDto.Request reqDto, Long currentUserId) {
        if (currentUserId == null || currentUserId <= 0) {
            return new CommentDeleteDto.Response(false, 401, "로그인이 필요한 서비스입니다.");
        }
        if (reqDto == null || reqDto.getCommentId() <= 0 || reqDto.getPostId() <= 0) {
            return new CommentDeleteDto.Response(false, 400, "유효하지 않은 요청 데이터입니다.");
        }

        try {
            // DAO 쿼리 자체에 user_id 검증 조건 포함 (WHERE comment_id = ? AND user_id = ?)
            boolean deleted = commentDAO.deleteComment(reqDto.getCommentId(), currentUserId);
            if (!deleted) {
                return new CommentDeleteDto.Response(false, 403, "삭제 권한이 없거나 이미 삭제된 댓글입니다.");
            }

            int remainCommentCount = commentDAO.getCommentCountByPostId(reqDto.getPostId());
            return new CommentDeleteDto.Response(true, 200, "댓글이 삭제되었습니다.", remainCommentCount);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommentDeleteDto.Response(false, 500, "댓글 삭제 중 오류 발생: " + e.getMessage());
        }
    }
}