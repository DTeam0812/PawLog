package com.pawlog.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.pawlog.server.db.DBConnectionMgr;
import com.pawlog.server.entity.UsageLogEntity;

public class UsageLogDAO {
    private DBConnectionMgr pool;

    public UsageLogDAO() {
        pool = DBConnectionMgr.getInstance();
    }

    // 프로그램 사용 기록 저장
    public boolean upsertUsageLog(UsageLogEntity entity) {
        Connection con = null;
        PreparedStatement pstmt = null;

        // 클라이언트가 60초 단위 델타값을 전송하므로, 중복 시 덮어쓰지 않고 누적 합산
        String sql = "INSERT INTO usage_logs (user_id, program_id, usage_date, usage_hour, duration_sec) "
                + "VALUES (?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE duration_sec = duration_sec + VALUES(duration_sec)";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, entity.getUserId());
            pstmt.setLong(2, entity.getProgramId());
            pstmt.setDate(3, entity.getUsageDate());
            pstmt.setInt(4, entity.getUsageHour());
            pstmt.setInt(5, entity.getDurationSec());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }


    // 특정 사용자의 가장 최근 사용 기록 단건 조회 (홈 화면용)
    public UsageLogEntity getLatestLogByUserId(long userId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT log_id, user_id, program_id, usage_date, usage_hour, duration_sec "
                + "FROM usage_logs WHERE user_id = ? ORDER BY usage_date DESC, usage_hour DESC LIMIT 1";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                UsageLogEntity entity = new UsageLogEntity();
                entity.setLogId(rs.getLong("log_id"));
                entity.setUserId(rs.getLong("user_id"));
                entity.setProgramId(rs.getLong("program_id"));
                entity.setUsageDate(rs.getDate("usage_date"));
                entity.setUsageHour(rs.getInt("usage_hour"));
                entity.setDurationSec(rs.getInt("duration_sec"));
                return entity;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return null;
    }


    // 모든 사용 기록 삭제 (전체 데이터 초기화)
    public boolean deleteAllUsageLogs() {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "TRUNCATE TABLE usage_logs";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);

            int result = pstmt.executeUpdate();
            return result >= 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    /**
     * 특정 사용자의 특정 날짜 프로그램별 사용량 목록 조회 (당일 통계용, 실시간 usage_logs 기준)
     * @return List<Object[]> -> [0]: program_name(String), [1]: total_duration_sec(Long)
     */
    public List<Object[]> getDailyProgramUsageByUserIdAndDate(long userId, java.sql.Date usageDate) {
        List<Object[]> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT COALESCE(p.process_name, p.display_name) AS program_name, "
                + "       SUM(u.duration_sec) AS total_duration_sec "
                + "FROM usage_logs u "
                + "JOIN programs p ON u.program_id = p.program_id "
                + "WHERE u.user_id = ? AND u.usage_date = ? "
                + "GROUP BY u.program_id, p.process_name, p.display_name "
                + "ORDER BY total_duration_sec DESC";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, userId);
            pstmt.setDate(2, usageDate);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("program_name"),
                    rs.getLong("total_duration_sec")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return list;
    }
}
