package com.pawlog.server.dao;

import com.pawlog.server.db.DBConnectionMgr;
import com.pawlog.server.entity.DailyUsageSummaryEntity;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DailyUsageSummaryDAO {
    private DBConnectionMgr pool;

    public DailyUsageSummaryDAO() {
        pool = DBConnectionMgr.getInstance();
    }

    // 1. 세션 종료 시 일간 사용량 누적 (UPSERT: 없으면 INSERT, 있으면 사용 시간 ADD)
    public boolean upsertDailySummary(DailyUsageSummaryEntity entity) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO daily_usage_summary "
                + "(user_id, program_id, summary_date, foreground_duration_sec, background_duration_sec, total_duration_sec) "
                + "VALUES (?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "foreground_duration_sec = foreground_duration_sec + VALUES(foreground_duration_sec), "
                + "background_duration_sec = background_duration_sec + VALUES(background_duration_sec), "
                + "total_duration_sec = total_duration_sec + VALUES(total_duration_sec)";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, entity.getUserId());
            pstmt.setLong(2, entity.getProgramId());
            pstmt.setDate(3, entity.getSummaryDate());
            pstmt.setLong(4, entity.getForegroundDurationSec());
            pstmt.setLong(5, entity.getBackgroundDurationSec());
            pstmt.setLong(6, entity.getTotalDurationSec());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    // 2. 특정 사용자의 특정 날짜 프로그램별 사용 요약 목록 조회 (일간 통계 뷰)
    public List<DailyUsageSummaryEntity> getDailySummaryByUserAndDate(long userId, Date summaryDate) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<DailyUsageSummaryEntity> list = new ArrayList<>();

        String sql = "SELECT summary_id, user_id, program_id, summary_date, "
                + "foreground_duration_sec, background_duration_sec, total_duration_sec "
                + "FROM daily_usage_summary "
                + "WHERE user_id = ? AND summary_date = ? "
                + "ORDER BY total_duration_sec DESC";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, userId);
            pstmt.setDate(2, summaryDate);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                DailyUsageSummaryEntity entity = new DailyUsageSummaryEntity();
                entity.setSummaryId(rs.getLong("summary_id"));
                entity.setUserId(rs.getLong("user_id"));
                entity.setProgramId(rs.getLong("program_id"));
                entity.setSummaryDate(rs.getDate("summary_date"));
                entity.setForegroundDurationSec(rs.getLong("foreground_duration_sec"));
                entity.setBackgroundDurationSec(rs.getLong("background_duration_sec"));
                entity.setTotalDurationSec(rs.getLong("total_duration_sec"));
                list.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return list;
    }

    // 3. 특정 사용자의 특정 기간(시작일~종료일) 프로그램별 합계 집계 조회 (주간/월간/커스텀 기간 통계)
    public List<DailyUsageSummaryEntity> getSummaryByUserAndPeriod(long userId, Date startDate, Date endDate) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<DailyUsageSummaryEntity> list = new ArrayList<>();

        String sql = "SELECT program_id, "
                + "SUM(foreground_duration_sec) AS foreground_duration_sec, "
                + "SUM(background_duration_sec) AS background_duration_sec, "
                + "SUM(total_duration_sec) AS total_duration_sec "
                + "FROM daily_usage_summary "
                + "WHERE user_id = ? AND summary_date BETWEEN ? AND ? "
                + "GROUP BY program_id "
                + "ORDER BY total_duration_sec DESC";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, userId);
            pstmt.setDate(2, startDate);
            pstmt.setDate(3, endDate);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                DailyUsageSummaryEntity entity = new DailyUsageSummaryEntity();
                entity.setUserId(userId);
                entity.setProgramId(rs.getLong("program_id"));
                entity.setForegroundDurationSec(rs.getLong("foreground_duration_sec"));
                entity.setBackgroundDurationSec(rs.getLong("background_duration_sec"));
                entity.setTotalDurationSec(rs.getLong("total_duration_sec"));
                list.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return list;
    }

    // 4. 특정 사용자의 특정 날짜 총 사용 시간(초) 합계 조회 (오늘의 총 사용 시간)
    public long getTotalDurationByUserAndDate(long userId, Date summaryDate) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        long totalSec = 0;

        String sql = "SELECT SUM(total_duration_sec) FROM daily_usage_summary WHERE user_id = ? AND summary_date = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, userId);
            pstmt.setDate(2, summaryDate);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                totalSec = rs.getLong(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return totalSec;
    }
}
