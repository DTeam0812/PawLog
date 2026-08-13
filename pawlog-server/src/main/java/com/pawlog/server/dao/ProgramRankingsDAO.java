package com.pawlog.server.dao;

import com.pawlog.server.db.DBConnectionMgr;
import com.pawlog.server.entity.ProgramRankingEntity;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProgramRankingsDAO {
    private DBConnectionMgr pool;

    public ProgramRankingsDAO() {
        pool = DBConnectionMgr.getInstance();
    }

    // 1. 순위 데이터 일괄 등록 (배치 작업용)
    public boolean insertBatchRankings(List<ProgramRankingEntity> list) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO program_rankings "
                + "(program_id, period_type, ranking_date, rank_position, total_duration_sec, gender, age_group) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);

            for (ProgramRankingEntity entity : list) {
                pstmt.setLong(1, entity.getProgramId());
                pstmt.setString(2, entity.getPeriodType());
                pstmt.setDate(3, entity.getRankingDate());
                pstmt.setInt(4, entity.getRankPosition());
                pstmt.setLong(5, entity.getTotalDurationSec());
                pstmt.setString(6, entity.getGender()); // NULL 허용
                pstmt.setString(7, entity.getAgeGroup()); // NULL 허용
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

    // 2. 특정 조건의 기존 순위 데이터 삭제 (배치 재계산 전 초기화용)
    public boolean deleteRankings(String periodType, Date rankingDate, String gender, String ageGroup) {
        Connection con = null;
        PreparedStatement pstmt = null;

        StringBuilder sql = new StringBuilder("DELETE FROM program_rankings WHERE period_type = ? AND ranking_date = ? ");

        if (gender == null) {
            sql.append("AND gender IS NULL ");
        } else {
            sql.append("AND gender = ? ");
        }

        if (ageGroup == null) {
            sql.append("AND age_group IS NULL");
        } else {
            sql.append("AND age_group = ?");
        }

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql.toString());
            pstmt.setString(1, periodType);
            pstmt.setDate(2, rankingDate);

            int paramIndex = 3;
            if (gender != null) {
                pstmt.setString(paramIndex++, gender);
            }
            if (ageGroup != null) {
                pstmt.setString(paramIndex, ageGroup);
            }

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    // 3. 인기 프로그램 순위 조회 (전체/세그먼트 필터링, RK-01 & RK-02)
    public List<ProgramRankingEntity> getRankings(String periodType, Date rankingDate, String gender, String ageGroup, int limit) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<ProgramRankingEntity> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT ranking_id, program_id, period_type, ranking_date, rank_position, total_duration_sec, gender, age_group ")
                .append("FROM program_rankings ")
                .append("WHERE period_type = ? AND ranking_date = ? ");

        if (gender == null) {
            sql.append("AND gender IS NULL ");
        } else {
            sql.append("AND gender = ? ");
        }

        if (ageGroup == null) {
            sql.append("AND age_group IS NULL ");
        } else {
            sql.append("AND age_group = ? ");
        }

        sql.append("ORDER BY rank_position ASC LIMIT ?");

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql.toString());
            pstmt.setString(1, periodType);
            pstmt.setDate(2, rankingDate);

            int paramIndex = 3;
            if (gender != null) {
                pstmt.setString(paramIndex++, gender);
            }
            if (ageGroup != null) {
                pstmt.setString(paramIndex++, ageGroup);
            }
            pstmt.setInt(paramIndex, limit);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                ProgramRankingEntity entity = new ProgramRankingEntity();
                entity.setRankingId(rs.getLong("ranking_id"));
                entity.setProgramId(rs.getLong("program_id"));
                entity.setPeriodType(rs.getString("period_type"));
                entity.setRankingDate(rs.getDate("ranking_date"));
                entity.setRankPosition(rs.getInt("rank_position"));
                entity.setTotalDurationSec(rs.getLong("total_duration_sec"));
                entity.setGender(rs.getString("gender"));
                entity.setAgeGroup(rs.getString("age_group"));
                list.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return list;
    }

    // 4. 특정 프로그램의 해당 기준일 전체 순위 조회 (프로그램 상세 분석용, AV-01)
    public Integer getRankPositionByProgram(long programId, String periodType, Date rankingDate) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT rank_position FROM program_rankings "
                + "WHERE program_id = ? AND period_type = ? AND ranking_date = ? "
                + "AND gender IS NULL AND age_group IS NULL";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, programId);
            pstmt.setString(2, periodType);
            pstmt.setDate(3, rankingDate);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("rank_position");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return null;
    }
}
