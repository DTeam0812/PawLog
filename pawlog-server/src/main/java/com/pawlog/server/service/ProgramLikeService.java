package com.pawlog.server.service;

import com.pawlog.common.dto.recommend.ProgramLikeDto;
import com.pawlog.common.dto.recommend.RecommendProgramDto;
import com.pawlog.server.dao.ProgramDAO;
import com.pawlog.server.dao.ProgramLikeDAO;
import com.pawlog.server.dao.ProgramLikeStatDAO;
import com.pawlog.server.entity.ProgramEntity;
import com.pawlog.server.entity.ProgramLikeEntity;

import java.util.List;

public class ProgramLikeService {
    private final ProgramDAO programDAO;
    private final ProgramLikeDAO programLikeDAO;
    private final ProgramLikeStatDAO programLikeStatDAO;

    public ProgramLikeService() {
        this.programDAO = new ProgramDAO();
        this.programLikeDAO = new ProgramLikeDAO();
        this.programLikeStatDAO = new ProgramLikeStatDAO();
    }

    public ProgramLikeService(ProgramDAO programDAO, ProgramLikeDAO programLikeDAO, ProgramLikeStatDAO programLikeStatDAO) {
        this.programDAO = programDAO;
        this.programLikeDAO = programLikeDAO;
        this.programLikeStatDAO = programLikeStatDAO;
    }

    /**
     * 프로그램 좋아요 토글 (등록 <-> 취소)
     *
     * @param programId     대상 프로그램 ID
     * @param currentUserId 현재 로그인한 사용자 ID
     * @return 좋아요 토글 결과 응답 DTO
     */
    public ProgramLikeDto.Response toggleProgramLike(long programId, Long currentUserId) {
        if (currentUserId == null || currentUserId <= 0) {
            return new ProgramLikeDto.Response(false, 401, "로그인이 필요한 서비스입니다.");
        }

        if (programId <= 0) {
            return new ProgramLikeDto.Response(false, 400, "유효하지 않은 프로그램ID입니다.");
        }

        try {
            // 프로그램 존재 여부 검증
            ProgramEntity program = programDAO.getProgramById(programId);
            if (program == null) {
                return new ProgramLikeDto.Response(false, 400, "유효하지 않은 프로그램ID입니다.");
            }

            boolean isCurrentlyLiked = programLikeDAO.isProgramLiked(currentUserId, programId);
            boolean success;

            if (isCurrentlyLiked) {
                // 이미 좋아요를 누른 상태 -> 취소
                success = programLikeDAO.deleteProgramLike(currentUserId, programId);
            } else {
                // 좋아요를 누르지 않은 상태 -> 등록
                success = programLikeDAO.insertProgramLike(new ProgramLikeEntity(currentUserId, programId));
            }

            if (!success) {
                return new ProgramLikeDto.Response(false, 500, "프로그램 추천 처리에 실패했습니다.");
            }

            // 통계 테이블 동기화 (오류 발생 시에도 토글 로직 자체는 성공 유지)
            try {
                programLikeStatDAO.syncSingleProgramLikeStats(programId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            boolean finalLikedState = !isCurrentlyLiked;
            int totalLikeCount = programLikeDAO.getLikeCountByProgramId(programId);

            ProgramLikeDto.LikeData likeData = new ProgramLikeDto.LikeData(finalLikedState, totalLikeCount);

            return new ProgramLikeDto.Response(
                    true,
                    200,
                    finalLikedState ? "프로그램을 추천했습니다." : "프로그램 추천을 취소했습니다.",
                    likeData
            );
        } catch (Exception e) {
            e.printStackTrace();
            return new ProgramLikeDto.Response(false, 500, "프로그램 추천 처리 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 추천수(좋아요) 기준 상위 핫 프로그램 조회
     *
     * @param request 요청 DTO (count 포함)
     * @return 핫 프로그램 목록 응답 DTO
     */
    public RecommendProgramDto.Response getTopRecommendPrograms(RecommendProgramDto.Request request) {
        int count = (request != null && request.getCount() > 0) ? request.getCount() : 3;

        try {
            List<RecommendProgramDto.Item> items = programLikeDAO.getTopLikedPrograms(count);
            return new RecommendProgramDto.Response(
                    true,
                    200,
                    "핫 프로그램 조회에 성공했습니다.",
                    items
            );
        } catch (Exception e) {
            e.printStackTrace();
            return new RecommendProgramDto.Response(
                    false,
                    500,
                    "핫 프로그램 조회 중 오류가 발생했습니다: " + e.getMessage()
            );
        }
    }
}
