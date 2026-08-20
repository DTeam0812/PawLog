package com.pawlog.server.service;

import com.pawlog.common.dto.usage.UsageLogSaveDto;
import com.pawlog.server.dao.ProgramDAO;
import com.pawlog.server.dao.UsageLogDAO;
import com.pawlog.server.dao.UserDAO;
import com.pawlog.server.entity.ProgramEntity;
import com.pawlog.server.entity.UsageLogEntity;

import java.sql.Date;
import java.time.LocalDate;

public class UsageLogService {
    private final UsageLogDAO usageLogDAO;
    private final ProgramDAO programDAO;
    private final UserDAO userDAO;

    public UsageLogService() {
        this.usageLogDAO = new UsageLogDAO();
        this.programDAO = new ProgramDAO();
        this.userDAO = new UserDAO();
    }

    public UsageLogSaveDto.Response saveUsageLogs(Long sessionUserId, UsageLogSaveDto.Request req) {
        if (req == null || req.getLogs() == null || req.getLogs().isEmpty()) {
            return new UsageLogSaveDto.Response(false, 400, "저장할 로그 데이터가 없습니다.", 0);
        }

        // 유저 ID 확인
        Long userId = sessionUserId;
        if (userId == null) {
            // 세션 정보가 없거나 미인증 처리
            return new UsageLogSaveDto.Response(false, 401, "인증되지 않은 유저입니다. 로그인 후 이용해주세요.", 0);
        }

        // Request 레벨 fallback 날짜
        LocalDate fallbackDate;
        try {
            fallbackDate = LocalDate.parse(req.getUsageDate());
        } catch (Exception e) {
            fallbackDate = LocalDate.now();
        }

        int savedCount = 0;

        // 사용 기록 리스트 순회 저장
        for (UsageLogSaveDto.LogItem item : req.getLogs()) {
            if (item.getProgramName() == null || item.getProgramName().trim().isEmpty()) {
                continue;
            }

            String processName = item.getProgramName().trim();

            // 프로그램 조회 및 없으면 자동 생성
            ProgramEntity program = programDAO.getProgramByProcessName(processName);
            if (program == null) {
                String displayName = processName.replaceFirst("(?i)\\.exe$", "");
                programDAO.insertProgram(processName, displayName);
                program = programDAO.getProgramByProcessName(processName);
            }

            if (program == null) {
                // 프로그램 등록 실패 시 해당 건 건너뜀
                continue;
            }

            // 날짜 결정: LogItem에 usageDate가 있으면 사용, 없으면 Request 레벨 fallback
            LocalDate itemDate;
            if (item.getUsageDate() != null && !item.getUsageDate().isEmpty()) {
                try {
                    itemDate = LocalDate.parse(item.getUsageDate());
                } catch (Exception e) {
                    itemDate = fallbackDate;
                }
            } else {
                itemDate = fallbackDate;
            }

            int hour = Math.max(0, Math.min(23, item.getHour()));

            // UsageLogEntity 생성 및 DB UPSERT
            UsageLogEntity logEntity = new UsageLogEntity();
            logEntity.setUserId(userId);
            logEntity.setProgramId(program.getProgramId());
            logEntity.setUsageDate(Date.valueOf(itemDate));
            logEntity.setUsageHour(hour);
            logEntity.setDurationSec((int) item.getDurationSec());

            boolean success = usageLogDAO.upsertUsageLog(logEntity);
            if (success) {
                savedCount++;
            }
        }

        if (savedCount > 0) {
            return new UsageLogSaveDto.Response(true, 200, "사용 기록이 성공적으로 저장되었습니다.", savedCount);
        } else {
            return new UsageLogSaveDto.Response(false, 500, "사용 기록 저장에 실패하였습니다.", 0);
        }
    }
}
