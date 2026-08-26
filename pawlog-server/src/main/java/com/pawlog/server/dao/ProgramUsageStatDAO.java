package com.pawlog.server.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.pawlog.server.db.DBConnectionMgr;
import com.pawlog.server.entity.ProgramUsageStatEntity;

public class ProgramUsageStatDAO {
    private DBConnectionMgr pool;

    public ProgramUsageStatDAO() {
        pool = DBConnectionMgr.getInstance();
    }

    public static class RawRankData {
        public long programId;
        public String programName;
        public double avgDurationSec;
    }

    //인기 순위 상위 10개 반환
    public List<RawRankData> getTop10PopularRankingsRaw(
            String gender,
            String ageGroup,
            LocalDate startDate,
            LocalDate endDate) {

        List<RawRankData> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        boolean isGenderFilter = (gender != null && !gender.equalsIgnoreCase("ALL") && !gender.isBlank());
        boolean isAgeFilter = (ageGroup != null && !ageGroup.equalsIgnoreCase("ALL") && !ageGroup.isBlank());

        String sql = "SELECT s.program_id, "
                + "COALESCE(p.display_name, p.process_name) AS program_name, "
                + "SUM(s.avg_duration_sec * s.total_users) / NULLIF(SUM(s.total_users), 0) AS avg_duration_sec "
                + "FROM program_usage_stats s "
                + "INNER JOIN programs p ON s.program_id = p.program_id "
                + "WHERE s.stat_date BETWEEN ? AND ? ";

        if (isGenderFilter) sql += "AND s.gender = ? ";
        else sql += "AND s.gender IS NULL ";

        if (isAgeFilter) sql += "AND s.age_group = ? ";
        else sql += "AND s.age_group IS NULL ";

        sql += "GROUP BY s.program_id, p.display_name, p.process_name "
                + "HAVING SUM(s.total_users) >= 1 "
                + "ORDER BY avg_duration_sec DESC "
                + "LIMIT 10";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);

            int paramIndex = 1;
            pstmt.setDate(paramIndex++, Date.valueOf(startDate));
            pstmt.setDate(paramIndex++, Date.valueOf(endDate));

            if (isGenderFilter) pstmt.setString(paramIndex++, gender);
            if (isAgeFilter) pstmt.setString(paramIndex++, ageGroup);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                RawRankData data = new RawRankData();
                data.programId = rs.getLong("program_id");
                data.programName = rs.getString("program_name");
                data.avgDurationSec = rs.getDouble("avg_duration_sec");
                list.add(data);
            }
        } catch (Exception e) {
            throw new IllegalStateException("인기 순위 데이터 조회에 실패했습니다.", e);
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }

        return list;
    }

    // 특정 날짜에 이미 집계된 통계 데이터가 있는지 확인
    public boolean hasStatsForDate(LocalDate date) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT COUNT(*) FROM program_usage_stats WHERE stat_date = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setDate(1, Date.valueOf(date));
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }

        return false;
    }

    // daily_usage_summary + users 기반으로 특정 날짜의 통계를 자동 집계하여 program_usage_stats에 저장
    public int aggregateAndSaveDailyStats(LocalDate targetDate) {
        Connection con = null;
        PreparedStatement deletePstmt = null;
        PreparedStatement insertPstmt = null;
        Date sqlTargetDate = Date.valueOf(targetDate);

        String deleteSql = "DELETE FROM program_usage_stats WHERE stat_date = ?";

        String insertSql = "INSERT INTO program_usage_stats "
                + "(program_id, stat_date, avg_duration_sec, total_users, gender, age_group) "
                + "SELECT program_id, stat_date, avg_duration_sec, total_users, gender, age_group "
                + "FROM ( "
                // 1. 전체 통계 (gender = NULL, age_group = NULL)
                + "    SELECT d.program_id, ? AS stat_date, "
                + "           AVG(d.total_duration_sec) AS avg_duration_sec, "
                + "           COUNT(DISTINCT d.user_id) AS total_users, "
                + "           NULL AS gender, "
                + "           NULL AS age_group "
                + "    FROM daily_usage_summary d "
                + "    JOIN users u ON d.user_id = u.user_id "
                + "    WHERE d.summary_date = ? "
                + "    GROUP BY d.program_id "
                + "    UNION ALL "
                // 2. 성별별 통계 (gender = M/F, age_group = NULL)
                + "    SELECT d.program_id, ? AS stat_date, "
                + "           AVG(d.total_duration_sec) AS avg_duration_sec, "
                + "           COUNT(DISTINCT d.user_id) AS total_users, "
                + "           u.gender AS gender, "
                + "           NULL AS age_group "
                + "    FROM daily_usage_summary d "
                + "    JOIN users u ON d.user_id = u.user_id "
                + "    WHERE d.summary_date = ? "
                + "    GROUP BY d.program_id, u.gender "
                + "    UNION ALL "
                // 3. 연령대별 통계 (gender = NULL, age_group = 10s...)
                + "    SELECT d.program_id, ? AS stat_date, "
                + "           AVG(d.total_duration_sec) AS avg_duration_sec, "
                + "           COUNT(DISTINCT d.user_id) AS total_users, "
                + "           NULL AS gender, "
                + "           CASE "
                + "               WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) < 10 THEN 'under_10' "
                + "               WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 10 AND 19 THEN '10s' "
                + "               WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 20 AND 29 THEN '20s' "
                + "               WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 30 AND 39 THEN '30s' "
                + "               WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 40 AND 49 THEN '40s' "
                + "               WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 50 AND 59 THEN '50s' "
                + "               WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 60 AND 69 THEN '60s' "
                + "               ELSE '70s_over' "
                + "           END AS age_group "
                + "    FROM daily_usage_summary d "
                + "    JOIN users u ON d.user_id = u.user_id "
                + "    WHERE d.summary_date = ? "
                + "    GROUP BY d.program_id, "
                + "             CASE "
                + "                 WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) < 10 THEN 'under_10' "
                + "                 WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 10 AND 19 THEN '10s' "
                + "                 WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 20 AND 29 THEN '20s' "
                + "                 WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 30 AND 39 THEN '30s' "
                + "                 WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 40 AND 49 THEN '40s' "
                + "                 WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 50 AND 59 THEN '50s' "
                + "                 WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 60 AND 69 THEN '60s' "
                + "                 ELSE '70s_over' "
                + "             END "
                + "    UNION ALL "
                // 4. 성별 + 연령대별 통계 (gender = M/F, age_group = 10s...)
                + "    SELECT d.program_id, ? AS stat_date, "
                + "           AVG(d.total_duration_sec) AS avg_duration_sec, "
                + "           COUNT(DISTINCT d.user_id) AS total_users, "
                + "           u.gender AS gender, "
                + "           CASE "
                + "               WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) < 10 THEN 'under_10' "
                + "               WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 10 AND 19 THEN '10s' "
                + "               WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 20 AND 29 THEN '20s' "
                + "               WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 30 AND 39 THEN '30s' "
                + "               WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 40 AND 49 THEN '40s' "
                + "               WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 50 AND 59 THEN '50s' "
                + "               WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 60 AND 69 THEN '60s' "
                + "               ELSE '70s_over' "
                + "           END AS age_group "
                + "    FROM daily_usage_summary d "
                + "    JOIN users u ON d.user_id = u.user_id "
                + "    WHERE d.summary_date = ? "
                + "    GROUP BY d.program_id, u.gender, "
                + "             CASE "
                + "                 WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) < 10 THEN 'under_10' "
                + "                 WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 10 AND 19 THEN '10s' "
                + "                 WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 20 AND 29 THEN '20s' "
                + "                 WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 30 AND 39 THEN '30s' "
                + "                 WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 40 AND 49 THEN '40s' "
                + "                 WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 50 AND 59 THEN '50s' "
                + "                 WHEN TIMESTAMPDIFF(YEAR, u.birth_date, ?) BETWEEN 60 AND 69 THEN '60s' "
                + "                 ELSE '70s_over' "
                + "             END "
                + ") AS aggregated";

        try {
            con = pool.getConnection();
            con.setAutoCommit(false);

            // 기존 데이터 정리
            deletePstmt = con.prepareStatement(deleteSql);
            deletePstmt.setDate(1, sqlTargetDate);
            deletePstmt.executeUpdate();

            // 신규 통계 집계 적재
            insertPstmt = con.prepareStatement(insertSql);
            int idx = 1;
            // 1. 전체
            insertPstmt.setDate(idx++, sqlTargetDate);
            insertPstmt.setDate(idx++, sqlTargetDate);
            // 2. 성별별
            insertPstmt.setDate(idx++, sqlTargetDate);
            insertPstmt.setDate(idx++, sqlTargetDate);
            // 3. 연령대별
            insertPstmt.setDate(idx++, sqlTargetDate);
            for (int i = 0; i < 7; i++) { // CASE when conditions (under_10, 10s..60s)
                insertPstmt.setDate(idx++, sqlTargetDate);
            }
            insertPstmt.setDate(idx++, sqlTargetDate); // WHERE d.summary_date = ?
            for (int i = 0; i < 7; i++) { // GROUP BY CASE when conditions
                insertPstmt.setDate(idx++, sqlTargetDate);
            }
            // 4. 성별 + 연령대별
            insertPstmt.setDate(idx++, sqlTargetDate);
            for (int i = 0; i < 7; i++) { // CASE when conditions
                insertPstmt.setDate(idx++, sqlTargetDate);
            }
            insertPstmt.setDate(idx++, sqlTargetDate); // WHERE d.summary_date = ?
            for (int i = 0; i < 7; i++) { // GROUP BY CASE when conditions
                insertPstmt.setDate(idx++, sqlTargetDate);
            }

            int insertedRows = insertPstmt.executeUpdate();
            con.commit();
            return insertedRows;

        } catch (Exception e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (Exception rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            return -1;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                } catch (Exception autoCommitEx) {
                    autoCommitEx.printStackTrace();
                }
            }
            if (deletePstmt != null) {
                try {
                    deletePstmt.close();
                } catch (Exception ignored) {}
            }
            pool.freeConnection(con, insertPstmt);
        }
    }

    // 평균 사용률 통계 일괄 등록 (배치 작업용)
    public boolean insertBatchStats(List<ProgramUsageStatEntity> list) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO program_usage_stats "
                + "(program_id, stat_date, avg_duration_sec, total_users, gender, age_group) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);

            for (ProgramUsageStatEntity entity : list) {
                pstmt.setLong(1, entity.getProgramId());
                pstmt.setDate(2, entity.getStatDate());
                pstmt.setDouble(3, entity.getAvgDurationSec());
                pstmt.setInt(4, entity.getTotalUsers());
                pstmt.setString(5, entity.getGender()); // NULL 허용
                pstmt.setString(6, entity.getAgeGroup()); // NULL 허용
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            return results.length > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }
}
