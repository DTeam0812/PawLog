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



    // 프로그램 분석에서 사용할 기능
    // 1. D-7 ~ D-1 기간 동안의 프로그램별 일평균 사용 시간 및 순위 조회
// 반환: [0]: rank(Integer), [1]: avgDailyDurationSec(Double)
    public Object[] getAvgUsageAndRank(long programId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "WITH ProgramDailyAvg AS ("
                + "    SELECT d.program_id, "
                + "           SUM(d.total_duration_sec) / 7.0 AS avg_duration_sec, "
                + "           RANK() OVER (ORDER BY SUM(d.total_duration_sec) DESC) AS rank_pos "
                + "    FROM daily_usage_summary d "
                + "    WHERE d.summary_date BETWEEN ? AND ? "
                + "    GROUP BY d.program_id "
                + ") "
                + "SELECT rank_pos, avg_duration_sec FROM ProgramDailyAvg WHERE program_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setDate(1, java.sql.Date.valueOf(startDate));
            pstmt.setDate(2, java.sql.Date.valueOf(endDate));
            pstmt.setLong(3, programId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Object[] { rs.getInt("rank_pos"), rs.getDouble("avg_duration_sec") };
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return new Object[] { 0, 0.0 };
    }

    // 2. 특정 날짜(하루)의 특정 프로그램 총 사용 시간(초) 조회 (전날 대비 비교용)
    public long getDailyDurationByDate(long programId, java.time.LocalDate targetDate) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT COALESCE(SUM(total_duration_sec), 0) AS daily_total "
                + "FROM daily_usage_summary "
                + "WHERE program_id = ? AND summary_date = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, programId);
            pstmt.setDate(2, java.sql.Date.valueOf(targetDate));
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getLong("daily_total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return 0L;
    }

    // 3. D-7 ~ D-1 기간 동안의 연령대별 총 사용 시간(초) 조회 (생년월일 NULL 제외)
    public java.util.Map<String, Long> getAgeGroupDurations(long programId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        java.util.Map<String, Long> ageMap = new java.util.LinkedHashMap<>();
        ageMap.put("10대 미만", 0L);
        ageMap.put("10대", 0L);
        ageMap.put("20대", 0L);
        ageMap.put("30대", 0L);
        ageMap.put("40대", 0L);
        ageMap.put("50대", 0L);
        ageMap.put("60대", 0L);
        ageMap.put("70대 이상", 0L);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT "
                + "  CASE "
                + "    WHEN (YEAR(CURDATE()) - YEAR(u.birth_date)) < 10 THEN '10대 미만' "
                + "    WHEN (YEAR(CURDATE()) - YEAR(u.birth_date)) BETWEEN 10 AND 19 THEN '10대' "
                + "    WHEN (YEAR(CURDATE()) - YEAR(u.birth_date)) BETWEEN 20 AND 29 THEN '20대' "
                + "    WHEN (YEAR(CURDATE()) - YEAR(u.birth_date)) BETWEEN 30 AND 39 THEN '30대' "
                + "    WHEN (YEAR(CURDATE()) - YEAR(u.birth_date)) BETWEEN 40 AND 49 THEN '40대' "
                + "    WHEN (YEAR(CURDATE()) - YEAR(u.birth_date)) BETWEEN 50 AND 59 THEN '50대' "
                + "    WHEN (YEAR(CURDATE()) - YEAR(u.birth_date)) BETWEEN 60 AND 69 THEN '60대' "
                + "    ELSE '70대 이상' "
                + "  END AS age_group, "
                + "  SUM(d.total_duration_sec) AS group_total_sec "
                + "FROM daily_usage_summary d "
                + "JOIN users u ON d.user_id = u.user_id "
                + "WHERE d.program_id = ? AND d.summary_date BETWEEN ? AND ? AND u.birth_date IS NOT NULL "
                + "GROUP BY age_group";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, programId);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));
            pstmt.setDate(3, java.sql.Date.valueOf(endDate));
            rs = pstmt.executeQuery();

            while (rs.next()) {
                String group = rs.getString("age_group");
                long totalSec = rs.getLong("group_total_sec");
                if (ageMap.containsKey(group)) {
                    ageMap.put(group, totalSec);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return ageMap;
    }
}
