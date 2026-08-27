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

    public static class RealtimeRankRawData {
        public long programId;
        public String programName;
        public String categoryName;
        public long totalDurationSec;
        public int userCount;
    }

    /**
     * 특정 3개 시간대(예: now-2시간, now-1시간, 현재 시간)의 사용 로그를 합산하여 실시간 이용자 수(순위) 기준 Top N 프로그램을 조회
     */
    public List<RealtimeRankRawData> getTopRealtimePrograms(java.time.LocalDate date1, int hour1,
                                                            java.time.LocalDate date2, int hour2,
                                                            java.time.LocalDate date3, int hour3,
                                                            int limit) {
        List<RealtimeRankRawData> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT u.program_id, "
                + "       COALESCE(p.display_name, p.process_name) AS program_name, "
                + "       c.name AS category_name, "
                + "       SUM(u.duration_sec) AS total_duration_sec, "
                + "       COUNT(DISTINCT u.user_id) AS user_count "
                + "FROM usage_logs u "
                + "JOIN programs p ON u.program_id = p.program_id "
                + "LEFT JOIN categories c ON p.category_id = c.category_id "
                + "WHERE (u.usage_date = ? AND u.usage_hour = ?) "
                + "   OR (u.usage_date = ? AND u.usage_hour = ?) "
                + "   OR (u.usage_date = ? AND u.usage_hour = ?) "
                + "GROUP BY u.program_id, p.display_name, p.process_name, c.name "
                + "HAVING SUM(u.duration_sec) > 0 "
                + "ORDER BY user_count DESC, total_duration_sec DESC "
                + "LIMIT ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setDate(1, java.sql.Date.valueOf(date1));
            pstmt.setInt(2, hour1);
            pstmt.setDate(3, java.sql.Date.valueOf(date2));
            pstmt.setInt(4, hour2);
            pstmt.setDate(5, java.sql.Date.valueOf(date3));
            pstmt.setInt(6, hour3);
            pstmt.setInt(7, limit > 0 ? limit : 3);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                RealtimeRankRawData data = new RealtimeRankRawData();
                data.programId = rs.getLong("program_id");
                data.programName = rs.getString("program_name");
                data.categoryName = rs.getString("category_name");
                data.totalDurationSec = rs.getLong("total_duration_sec");
                data.userCount = rs.getInt("user_count");
                list.add(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }

        return list;
    }

    /**
     * 기존 호환용 2개 시간대 조회 오버로딩
     */
    public List<RealtimeRankRawData> getTopRealtimePrograms(java.time.LocalDate date1, int hour1, java.time.LocalDate date2, int hour2, int limit) {
        return getTopRealtimePrograms(date1, hour1, date2, hour2, date2, hour2, limit);
    }

    /**
     * 기존 호환용 Top 5 조회 메서드
     */
    public List<RealtimeRankRawData> getTop5RealtimePrograms(java.time.LocalDate date1, int hour1, java.time.LocalDate date2, int hour2) {
        return getTopRealtimePrograms(date1, hour1, date2, hour2, date2, hour2, 5);
    }

    // 프로그램 분석에 사용할 기능
    // D-7 ~ D-1 기간 동안의 4시간 단위(0~3, 4~7, 8~11, 12~15, 16~19, 20~23) 일평균 사용 시간(초) 조회
    public int[] getHourlyUsageSlots(long programId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        int[] slots = new int[6];
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT (usage_hour DIV 4) AS slot_idx, "
                + "       ROUND(SUM(duration_sec) / 7.0) AS avg_slot_duration "
                + "FROM usage_logs "
                + "WHERE program_id = ? AND usage_date BETWEEN ? AND ? "
                + "GROUP BY (usage_hour DIV 4)";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, programId);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));
            pstmt.setDate(3, java.sql.Date.valueOf(endDate));
            rs = pstmt.executeQuery();

            while (rs.next()) {
                int slotIdx = rs.getInt("slot_idx");
                int duration = rs.getInt("avg_slot_duration");
                if (slotIdx >= 0 && slotIdx < 6) {
                    slots[slotIdx] = duration;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return slots;
    }
}
