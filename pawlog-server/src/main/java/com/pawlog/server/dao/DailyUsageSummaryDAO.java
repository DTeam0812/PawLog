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

    // 2. 특정 사용자의 특정 날짜 프로그램별 사용 요약 목록 조회 (일간 통계 뷰) 사용시간 내림차순
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

    // 3. 특정 사용자의 특정 기간(시작일~종료일) 프로그램별 합계 집계 조회 (커스텀 기간 통계)
    /*public List<DailyUsageSummaryEntity> getSummaryByUserAndPeriod(long userId, Date startDate, Date endDate) {
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
    }*/

    // 3-1. 개선버전
    /**
     * 특정 사용자의 특정 기간(시작일~종료일) 프로그램별 사용량 합계 조회
     * @return List<Object[]> -> [0]: program_id(Long), [1]: program_name(String),
     *                           [2]: foreground_sec(Long), [3]: background_sec(Long), [4]: total_sec(Long)
     */
    public List<Object[]> getSummaryByUserAndPeriod(long userId, java.sql.Date startDate, java.sql.Date endDate) {
        List<Object[]> list = new ArrayList<>();

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        // programs 테이블과 JOIN하여 프로그램 이름을 한 번에 조회
        String sql = "SELECT d.program_id, " +
                "       COALESCE(p.display_name, p.process_name) AS program_name, " +
                "       SUM(d.foreground_duration_sec) AS foreground_duration_sec, " +
                "       SUM(d.background_duration_sec) AS background_duration_sec, " +
                "       SUM(d.total_duration_sec) AS total_duration_sec " +
                "FROM daily_usage_summary d " +
                "JOIN programs p ON d.program_id = p.program_id " +
                "WHERE d.user_id = ? AND d.summary_date BETWEEN ? AND ? " +
                "GROUP BY d.program_id, p.display_name, p.process_name " +
                "ORDER BY total_duration_sec DESC";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, userId);
            pstmt.setDate(2, startDate);
            pstmt.setDate(3, endDate);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[]{
                        rs.getLong("program_id"),
                        rs.getString("program_name"),
                        rs.getLong("foreground_duration_sec"),
                        rs.getLong("background_duration_sec"),
                        rs.getLong("total_duration_sec")
                };
                list.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }

        return list;
    }

    // 4. 특정 사용자의 특정 날짜 총 사용 시간(초) 합계 조회 (오늘의 총 사용 시간)
    public long getTotalUsageSecByUserAndPeriod(long userId, java.sql.Date startDate, java.sql.Date endDate) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        // COALESCE를 사용하여 해당 기간에 조회된 데이터가 없더라도 NULL이 아닌 0을 반환
        String sql = "SELECT COALESCE(SUM(total_duration_sec), 0) AS grand_total_sec " +
                "FROM daily_usage_summary " +
                "WHERE user_id = ? AND summary_date BETWEEN ? AND ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, userId);
            pstmt.setDate(2, startDate);
            pstmt.setDate(3, endDate);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getLong("grand_total_sec");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }

        return 0L;
    }
}
