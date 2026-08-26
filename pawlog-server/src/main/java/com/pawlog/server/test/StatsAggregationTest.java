package com.pawlog.server.test;

import com.pawlog.server.dao.ProgramUsageStatDAO;
import com.pawlog.server.db.DBConnectionMgr;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class StatsAggregationTest {

    public static void main(String[] args) {
        // 테스트 대상 날짜 (2026년 8월 25일)
        LocalDate targetDate = LocalDate.of(2026, 8, 25);
        System.out.println("==================================================");
        System.out.printf("  [테스트] %s 일자 program_usage_stats 집계 시작%n", targetDate);
        System.out.println("==================================================");

        ProgramUsageStatDAO dao = new ProgramUsageStatDAO();

        try {
            // 1. 집계 및 저장 실행
            int resultCount = dao.aggregateAndSaveDailyStats(targetDate);
            System.out.printf(">> 집계 및 저장 완료! 총 %d건의 통계 행(Row)이 생성/갱신되었습니다.%n%n", resultCount);

            // 2. 실제로 DB에 저장된 데이터 조회 및 검증
            printSavedStats(targetDate);

        } catch (Exception e) {
            System.err.println("[테스트 실패] 오류 발생:");
            e.printStackTrace();
        }
    }

    private static void printSavedStats(LocalDate date) {
        DBConnectionMgr pool = DBConnectionMgr.getInstance();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT s.stat_id, s.program_id, "
                + "COALESCE(p.display_name, p.process_name) AS program_name, "
                + "s.stat_date, s.avg_duration_sec, s.total_users, "
                + "COALESCE(s.gender, 'ALL') AS gender, "
                + "COALESCE(s.age_group, 'ALL') AS age_group "
                + "FROM program_usage_stats s "
                + "LEFT JOIN programs p ON s.program_id = p.program_id "
                + "WHERE s.stat_date = ? "
                + "ORDER BY s.program_id, s.gender, s.age_group "
                + "LIMIT 30";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setDate(1, Date.valueOf(date));
            rs = pstmt.executeQuery();

            System.out.println("-------------------------------------------------------------------------------------------------------------------");
            System.out.printf("%-8s | %-12s | %-20s | %-12s | %-10s | %-8s | %-10s%n",
                    "Stat ID", "Program ID", "Program Name", "Avg Sec", "TotalUsers", "Gender", "Age Group");
            System.out.println("-------------------------------------------------------------------------------------------------------------------");

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf("%-8d | %-12d | %-20s | %-12.1f | %-10d | %-8s | %-10s%n",
                        rs.getLong("stat_id"),
                        rs.getLong("program_id"),
                        rs.getString("program_name"),
                        rs.getDouble("avg_duration_sec"),
                        rs.getInt("total_users"),
                        rs.getString("gender"),
                        rs.getString("age_group")
                );
            }

            if (count == 0) {
                System.out.println(">> 해당 일자에 daily_usage_summary 기록이 없거나 생성된 통계가 없습니다.");
            } else {
                System.out.println("-------------------------------------------------------------------------------------------------------------------");
                System.out.printf(">> 상위 %d건 출력 완료%n", count);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
    }
}
