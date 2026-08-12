package server.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

import server.db.DBConnectionMgr;
import server.db.dto.UsageLogDTO;

public class UsageLogDAO {
	 private DBConnectionMgr pool;

    public UsageLogDAO() {
        pool = DBConnectionMgr.getInstance();
    }

    // 프로그램 사용 기록 저장
    public boolean insertUsageLog(UsageLogDTO dto) {

        Connection con = null;
        PreparedStatement pstmt = null;

        String sql =
                "INSERT INTO usage_logs "
              + "(user_id, program_id, is_foreground, "
              + "start_time, end_time, duration_sec) "
              + "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            con = pool.getConnection();

            pstmt = con.prepareStatement(sql);

            pstmt.setLong(1, dto.getUserId());
            pstmt.setLong(2, dto.getProgramId());
            pstmt.setBoolean(3, dto.isForeground());
            pstmt.setTimestamp(4, dto.getStartTime());
            pstmt.setTimestamp(5, dto.getEndTime());
            pstmt.setInt(6, dto.getDurationSec());

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
