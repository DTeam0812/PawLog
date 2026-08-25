package com.pawlog.server.service;

import java.util.Collections;
import java.util.List;

import com.pawlog.common.dto.board.PostDetailDto;
import com.pawlog.common.dto.board.PostEnrollDto;
import com.pawlog.common.dto.board.PostLikeDto;
import com.pawlog.common.dto.board.PostListDto;
import com.pawlog.server.dao.CommentDAO;
import com.pawlog.server.dao.PostDAO;
import com.pawlog.server.dao.PostLikeDAO;
import com.pawlog.server.entity.PostEntity;
import com.pawlog.server.entity.PostLikeEntity;

public class PostService {
    private final PostDAO postDAO;
    private final CommentDAO commentDAO;
    private final PostLikeDAO postLikeDAO;

    public PostService() {
        this.postDAO = new PostDAO();
        this.commentDAO = new CommentDAO();
        this.postLikeDAO = new PostLikeDAO();
    }

    public PostService(PostDAO postDAO, CommentDAO commentDAO, PostLikeDAO postLikeDAO) {
        this.postDAO = postDAO;
        this.commentDAO = commentDAO;
        this.postLikeDAO = postLikeDAO;
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

    /**
     * 특정 게시글의 상세 정보를 단건 조회합니다 (댓글 목록 포함).
     *
     * @param request       게시글 상세 조회 요청 DTO
     * @param currentUserId 현재 로그인한 사용자 ID (비로그인 시 null)
     * @return 게시글 상세 조회 응답 DTO
     */
    public PostDetailDto.Response getPostDetail(PostDetailDto.Request request, Long currentUserId) {
        if (request == null || request.getPostId() <= 0) {
            return new PostDetailDto.Response(false, 400, "유효한 게시글 ID가 필요합니다.", null);
        }

        try {
            PostDetailDto.PostDetailData postDetail = postDAO.getPostDetailById(request.getPostId(), currentUserId);
            if (postDetail == null) {
                return new PostDetailDto.Response(false, 404, "존재하지 않거나 삭제된 게시글입니다.", null);
            }

            // 댓글 목록 조회 및 세팅 (댓글이 없는 경우 빈 리스트 [] 반환)
            List<PostDetailDto.commentitem> comments = commentDAO.getCommentItemsByPostId(request.getPostId());
            if (comments == null) {
                comments = Collections.emptyList();
            }

            postDetail.setComments(comments);
            postDetail.setCommentCount(comments.size());

            return new PostDetailDto.Response(true, 200, "게시글 상세 조회 성공", postDetail);
        } catch (Exception e) {
            e.printStackTrace();
            return new PostDetailDto.Response(false, 500, "게시글 상세 조회 중 오류 발생: " + e.getMessage(), null);
        }
    }

    /**
     * 새로운 게시글을 등록합니다.
     *
     * @param request       게시글 등록 요청 DTO
     * @param currentUserId 현재 로그인한 사용자 ID (비로그인 시 null)
     * @return 게시글 등록 응답 DTO
     */
    public PostEnrollDto.Response enrollPost(PostEnrollDto.Request request, Long currentUserId) {
        if (currentUserId == null) {
            return new PostEnrollDto.Response(false, 401, "로그인이 필요합니다.", null);
        }

        if (request == null) {
            return new PostEnrollDto.Response(false, 400, "요청 데이터가 없습니다.", null);
        }

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return new PostEnrollDto.Response(false, 400, "게시글 제목을 입력해주세요.", null);
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return new PostEnrollDto.Response(false, 400, "게시글 내용을 입력해주세요.", null);
        }

        try {
            PostEntity entity = new PostEntity();
            entity.setUserId(currentUserId);
            entity.setProgramId(request.getProgramId() > 0 ? request.getProgramId() : null);
            entity.setTitle(request.getTitle().trim());
            entity.setContent(request.getContent().trim());
            entity.setIsActive(true);

            long postId = postDAO.insertPostAndGetId(entity);
            if (postId > 0) {
                return new PostEnrollDto.Response(true, 201, "게시글이 등록되었습니다.", new PostEnrollDto.PostEnrollData(postId));
            } else {
                return new PostEnrollDto.Response(false, 500, "게시글 등록에 실패했습니다.", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new PostEnrollDto.Response(false, 500, "게시글 등록 중 오류 발생: " + e.getMessage(), null);
        }
    }

    /**
     * 게시글 좋아요 토글 (등록 <-> 취소)
     *
     * @param postId        대상 게시글 ID
     * @param currentUserId 현재 로그인한 사용자 ID
     * @return 좋아요 토글 결과 응답 DTO
     */
    public PostLikeDto.Response togglePostLike(long postId, Long currentUserId) {
        if (currentUserId == null || currentUserId <= 0) {
            return new PostLikeDto.Response(false, 401, "로그인이 필요한 서비스입니다.");
        }

        if (postId <= 0) {
            return new PostLikeDto.Response(false, 400, "유효하지 않은 게시글 ID입니다.");
        }

        try {
            // 게시글 존재 및 활성(삭제 여부) 상태 사전 검증
            com.pawlog.server.entity.PostEntity post = postDAO.getPostById(postId);
            if (post == null || (post.getIsActive() != null && !post.getIsActive())) {
                return new PostLikeDto.Response(false, 404, "존재하지 않거나 삭제된 게시글입니다.", null);
            }
            boolean isCurrentlyLiked = postLikeDAO.isLiked(currentUserId, postId);
            boolean success;

            if (isCurrentlyLiked) {
                // 이미 좋아요를 누른 상태 -> 취소
                success = postLikeDAO.deleteLike(currentUserId, postId);
            } else {
                // 좋아요를 누르지 않은 상태 -> 등록
                success = postLikeDAO.insertLike(new PostLikeEntity(currentUserId, postId));
            }

            if (!success) {
                return new PostLikeDto.Response(false, 500, "좋아요 처리에 실패했습니다.");
            }

            boolean finalLikedState = !isCurrentlyLiked;
            int totalLikeCount = postLikeDAO.getLikeCountByPostId(postId);

            PostLikeDto.LikeData likeData = new PostLikeDto.LikeData(finalLikedState, totalLikeCount);

            return new PostLikeDto.Response(
                    true,
                    200,
                    finalLikedState ? "게시글을 추천했습니다." : "게시글 추천을 취소했습니다.",
                    likeData
            );
        } catch (Exception e) {
            e.printStackTrace();
            return new PostLikeDto.Response(false, 500, "게시글 좋아요 처리 중 오류 발생: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        PostService postService = new PostService();

        System.out.println("========== [테스트 1] 게시글 등록 (POST_ENROLL) ==========");
        PostEnrollDto.Request enrollReq = new PostEnrollDto.Request(3L, "VS Code 테마 추천", "오래 사용해도 눈이 편한 테마를 추천해주세요.");
        Long testUserId = 1L; // 로그인된 사용자 ID 가정
        PostEnrollDto.Response enrollRes = postService.enrollPost(enrollReq, testUserId);

        System.out.println("성공 여부: " + enrollRes.isSuccess());
        System.out.println("상태 코드: " + enrollRes.getCode());
        System.out.println("메시지: " + enrollRes.getMessage());
        if (enrollRes.getData() != null) {
            System.out.println("생성된 PostId: " + enrollRes.getData().getPostId());
        }

        System.out.println("\n========== [테스트 2] 기본 목록 조회 (POST_LIST_GET) ==========");
        PostListDto.Request listReq = new PostListDto.Request(3L, 0, 5, "LATEST", "");
        PostListDto.Response listRes = postService.getPostList(listReq, testUserId);
        printResponse(listRes);

        System.out.println("\n========== [테스트 3] 게시글 상세 조회 (POST_DETAIL_GET) ==========");
        long targetPostId = (enrollRes.getData() != null && enrollRes.getData().getPostId() > 0) ? enrollRes.getData().getPostId() : 1L;
        PostDetailDto.Request detailReq = new PostDetailDto.Request(targetPostId);
        PostDetailDto.Response detailRes = postService.getPostDetail(detailReq, testUserId);

        System.out.println("성공 여부: " + detailRes.isSuccess());
        System.out.println("상태 코드: " + detailRes.getCode());
        System.out.println("메시지: " + detailRes.getMessage());
        if (detailRes.getData() != null) {
            PostDetailDto.PostDetailData d = detailRes.getData();
            System.out.printf("[ID: %d] 제목: %s | 작성자: %s | 본문: %s | 좋아요: %d | 댓글: %d | 내가 좋아요: %s\n",
                    d.getPostId(), d.getTitle(), d.getAuthorNickname(), d.getContent(), d.getLikeCount(), d.getCommentCount(), d.isLiked() ? "Y" : "N");
        }
    }

    private static void printResponse(PostListDto.Response response) {
        System.out.println("성공 여부: " + response.isSuccess());
        System.out.println("상태 코드: " + response.getCode());
        System.out.println("메시지: " + response.getMessage());

        if (response.getData() != null) {
            PostListDto.PageData pageData = response.getData();
            System.out.printf("현재 페이지: %d | 총 페이지: %d | 전체 활성 게시글 수: %d\n",
                    pageData.getCurrentPage(), pageData.getTotalPages(), pageData.getTotalCount());
            System.out.println("----------------------------------------------------------------------");

            if (pageData.getPosts() == null || pageData.getPosts().isEmpty()) {
                System.out.println("조회된 활성 게시글이 없습니다.");
            } else {
                for (PostListDto.PostItem item : pageData.getPosts()) {
                    System.out.printf("[ID: %d] 제목: %s | 작성자: %s | 작성일: %s | 좋아요: %d | 댓글: %d | 내가 좋아요함: %s\n",
                            item.getPostId(),
                            item.getTitle(),
                            item.getAuthorNickname(),
                            item.getCreatedAt(),
                            item.getLikeCount(),
                            item.getCommentCount(),
                            item.isLiked() ? "Y" : "N");
                }
            }
            System.out.println("----------------------------------------------------------------------");
        }
    }
}
