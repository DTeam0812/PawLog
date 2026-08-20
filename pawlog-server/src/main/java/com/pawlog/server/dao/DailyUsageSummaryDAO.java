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

    // 1. 일간 사용량 요약 저장 / 갱신 (UPSERT: 같은 user + program + date 조합이면 total_duration_sec 갱신)
    public boolean upsertDailySummary(DailyUsageSummaryEntity entity) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO daily_usage_summary "
                + "(user_id, program_id, summary_date, total_duration_sec) "
                + "VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "total_duration_sec = VALUES(total_duration_sec)";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, entity.getUserId());
            pstmt.setLong(2, entity.getProgramId());
            pstmt.setDate(3, entity.getSummaryDate());
            pstmt.setLong(4, entity.getTotalDurationSec());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    // 1-1. usage_logs의 시간대별 합계를 기반으로 daily_usage_summary 즉시 동기화 (파이프라인 1단계용)
    public boolean syncFromUsageLogs(long userId, Date usageDate, long programId) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO daily_usage_summary (user_id, program_id, summary_date, total_duration_sec) "
                + "SELECT user_id, program_id, usage_date, SUM(duration_sec) "
                + "FROM usage_logs "
                + "WHERE user_id = ? AND usage_date = ? AND program_id = ? "
                + "GROUP BY user_id, program_id, usage_date "
                + "ON DUPLICATE KEY UPDATE "
                + "total_duration_sec = VALUES(total_duration_sec)";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, userId);
            pstmt.setDate(2, usageDate);
            pstmt.setLong(3, programId);

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

        String sql = "SELECT summary_id, user_id, program_id, summary_date, total_duration_sec "
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

    // 3. 특정 사용자의 특정 기간(시작일~종료일) 프로그램별 사용량 합계 조회
    /**
     * @return List<Object[]> -> [0]: program_id(Long), [1]: program_name(String), [2]: total_sec(Long)
     */
    public List<Object[]> getSummaryByUserAndPeriod(long userId, java.sql.Date startDate, java.sql.Date endDate) {
        List<Object[]> list = new ArrayList<>();

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT d.program_id, " +
                "       COALESCE(p.display_name, p.process_name) AS program_name, " +
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

    // 4. 특정 사용자의 특정 기간 총 사용 시간(초) 합계 조회
    public long getTotalUsageSecByUserAndPeriod(long userId, java.sql.Date startDate, java.sql.Date endDate) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

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
