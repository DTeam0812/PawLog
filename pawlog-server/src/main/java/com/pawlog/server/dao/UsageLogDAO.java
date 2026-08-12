package com.pawlog.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

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

        String sql = "INSERT INTO usage_logs (user_id, program_id, is_foreground, start_time, end_time, duration_sec) VALUES (?, ?, ?, ?, ?, ?)";

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
}
