package com.pawlog.server.service;

import java.util.ArrayList;
import java.util.List;

import com.pawlog.common.dto.board.ProgramSearchDto;
import com.pawlog.server.dao.ProgramDAO;
import com.pawlog.server.entity.ProgramEntity;

public class UtilService {
    private final ProgramDAO programDAO;

    public UtilService() {
        this.programDAO = new ProgramDAO();
    }

    public UtilService(ProgramDAO programDAO) {
        this.programDAO = programDAO;
    }

    /**
     * 프로그램 검색 (process_name 또는 display_name LIKE 검색)
     */
    public ProgramSearchDto.Response searchPrograms(ProgramSearchDto.Request request) {
        if (request == null) {
            return new ProgramSearchDto.Response(false, 400, "요청 데이터가 올바르지 않습니다.", List.of());
        }

        String keyword = request.getKeyword();
        int limit = request.getLimit();

        if (keyword == null || keyword.trim().isEmpty()) {
            return new ProgramSearchDto.Response(false, 400, "검색 키워드를 입력해주세요.", List.of());
        }

        if (limit <= 0) {
            limit = 10; // 기본값
        }

        try {
            List<ProgramEntity> entities = programDAO.searchPrograms(keyword.trim(), limit);
            List<ProgramSearchDto.ProgramItem> items = new ArrayList<>();

            for (ProgramEntity entity : entities) {
                items.add(new ProgramSearchDto.ProgramItem(
                    entity.getProgramId(),
                    entity.getProcessName(),
                    entity.getDisplayName()
                ));
            }

            return new ProgramSearchDto.Response(true, 200, "프로그램 검색 성공", items);
        } catch (Exception e) {
            e.printStackTrace();
            return new ProgramSearchDto.Response(false, 500, "프로그램 검색 중 서버 오류가 발생했습니다.", List.of());
        }
    }
}