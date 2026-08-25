package com.pawlog.server.service;

import java.util.List;

import com.pawlog.common.dto.board.PostListDto;
import com.pawlog.server.dao.PostDAO;

public class PostService {
    private final PostDAO postDAO;

    public PostService() {
        this.postDAO = new PostDAO();
    }

    public PostService(PostDAO postDAO) {
        this.postDAO = postDAO;
    }

    /**
     * 게시글 목록을 페이징, 검색, 정렬 조건에 따라 조회합니다.
     *
     * @param request       게시글 목록 조회 요청 DTO
     * @param currentUserId 현재 로그인한 사용자 ID (비로그인 시 null)
     * @return 게시글 목록 응답 DTO
     */
    public PostListDto.Response getPostList(PostListDto.Request request, Long currentUserId) {
        if (request == null) {
            return new PostListDto.Response(false, 400, "요청 데이터가 없습니다.", null);
        }

        try {
            int page = request.getPage();
            if (page < 0) {
                page = 0;
            }

            int pageSize = request.getPageSize();
            if (pageSize <= 0) {
                pageSize = 9;
            }

            long programId = request.getProgramId();
            String keyword = request.getKeyword();
            String sort = request.getSort();

            long totalCount = postDAO.getPostCount(programId, keyword);
            int totalPages = totalCount == 0 ? 0 : (int) Math.ceil((double) totalCount / pageSize);
            int offset = page * pageSize;

            List<PostListDto.PostItem> posts = postDAO.getPostList(programId, keyword, sort, offset, pageSize, currentUserId);

            PostListDto.PageData pageData = new PostListDto.PageData(posts, page, totalPages, totalCount);
            return new PostListDto.Response(true, 200, "게시글 목록 조회 성공", pageData);
        } catch (Exception e) {
            e.printStackTrace();
            return new PostListDto.Response(false, 500, "게시글 목록 조회 중 오류 발생: " + e.getMessage(), null);
        }
    }
}
