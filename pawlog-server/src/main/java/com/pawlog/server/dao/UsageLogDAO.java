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
    public boolean insertUsageLog(UsageLogEntity entity) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO usage_logs (user_id, program_id, is_foreground, start_time, end_time, duration_sec) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, entity.getUserId());
            pstmt.setLong(2, entity.getProgramId());
            pstmt.setBoolean(3, entity.isForeground());
            pstmt.setTimestamp(4, entity.getStartTime());
            pstmt.setTimestamp(5, entity.getEndTime());
            pstmt.setInt(6, entity.getDurationSec());

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

        String sql = "SELECT log_id, user_id, program_id, is_foreground, start_time, end_time, duration_sec "
                + "FROM usage_logs WHERE user_id = ? ORDER BY end_time DESC LIMIT 1";

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
                entity.setForeground(rs.getBoolean("is_foreground"));
                entity.setStartTime(rs.getTimestamp("start_time"));
                entity.setEndTime(rs.getTimestamp("end_time"));
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


}
