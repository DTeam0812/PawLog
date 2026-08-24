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




    //    // 2. 특정 조건의 통계 데이터 삭제 (배치 재계산 전 초기화용)
//    public boolean deleteStats(String periodType, Date statDate) {
//        Connection con = null;
//        PreparedStatement pstmt = null;
//
//        String sql = "DELETE FROM program_usage_stats WHERE period_type = ? AND stat_date = ?";
//
//        try {
//            con = pool.getConnection();
//            pstmt = con.prepareStatement(sql);
//            pstmt.setString(1, periodType);
//            pstmt.setDate(2, statDate);
//
//            int result = pstmt.executeUpdate();
//            return result > 0;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        } finally {
//            pool.freeConnection(con, pstmt);
//        }
//    }
//    // 4. 연령대별 평균 사용량 비교 목록 조회 (성별 NULL, 연령대별 차트용)
//    public List<ProgramUsageStatEntity> getAgeGroupStatsByProgram(long programId, String periodType, Date statDate) {
//        Connection con = null;
//        PreparedStatement pstmt = null;
//        ResultSet rs = null;
//        List<ProgramUsageStatEntity> list = new ArrayList<>();
//
//        String sql = "SELECT stat_id, program_id, period_type, stat_date, "
//                + "avg_duration_sec, total_users, gender, age_group "
//                + "FROM program_usage_stats "
//                + "WHERE program_id = ? AND period_type = ? AND stat_date = ? "
//                + "AND gender IS NULL AND age_group IS NOT NULL "
//                + "ORDER BY age_group ASC";
//
//        try {
//            con = pool.getConnection();
//            pstmt = con.prepareStatement(sql);
//            pstmt.setLong(1, programId);
//            pstmt.setString(2, periodType);
//            pstmt.setDate(3, statDate);
//            rs = pstmt.executeQuery();
//
//            while (rs.next()) {
//                ProgramUsageStatEntity entity = new ProgramUsageStatEntity();
//                entity.setStatId(rs.getLong("stat_id"));
//                entity.setProgramId(rs.getLong("program_id"));
//                entity.setPeriodType(rs.getString("period_type"));
//                entity.setStatDate(rs.getDate("stat_date"));
//                entity.setAvgDurationSec(rs.getFloat("avg_duration_sec"));
//                entity.setTotalUsers(rs.getInt("total_users"));
//                entity.setGender(rs.getString("gender"));
//                entity.setAgeGroup(rs.getString("age_group"));
//                list.add(entity);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            pool.freeConnection(con, pstmt, rs);
//        }
//        return list;
//    }
//
//    // 5. 성별 평균 사용량 비교 목록 조회 (연령대 NULL, 성별 차트용)
//    public List<ProgramUsageStatEntity> getGenderStatsByProgram(long programId, String periodType, Date statDate) {
//        Connection con = null;
//        PreparedStatement pstmt = null;
//        ResultSet rs = null;
//        List<ProgramUsageStatEntity> list = new ArrayList<>();
//
//        String sql = "SELECT stat_id, program_id, period_type, stat_date, "
//                + "avg_duration_sec, total_users, gender, age_group "
//                + "FROM program_usage_stats "
//                + "WHERE program_id = ? AND period_type = ? AND stat_date = ? "
//                + "AND age_group IS NULL AND gender IS NOT NULL "
//                + "ORDER BY gender ASC";
//
//        try {
//            con = pool.getConnection();
//            pstmt = con.prepareStatement(sql);
//            pstmt.setLong(1, programId);
//            pstmt.setString(2, periodType);
//            pstmt.setDate(3, statDate);
//            rs = pstmt.executeQuery();
//
//            while (rs.next()) {
//                ProgramUsageStatEntity entity = new ProgramUsageStatEntity();
//                entity.setStatId(rs.getLong("stat_id"));
//                entity.setProgramId(rs.getLong("program_id"));
//                entity.setPeriodType(rs.getString("period_type"));
//                entity.setStatDate(rs.getDate("stat_date"));
//                entity.setAvgDurationSec(rs.getFloat("avg_duration_sec"));
//                entity.setTotalUsers(rs.getInt("total_users"));
//                entity.setGender(rs.getString("gender"));
//                entity.setAgeGroup(rs.getString("age_group"));
//                list.add(entity);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            pool.freeConnection(con, pstmt, rs);
//        }
//        return list;
//    }
}
