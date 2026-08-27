package com.pawlog.server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {

    private static final String DB_URL = "jdbc:mysql://cdn.ditanet.duckdns.org:8306/PawLog?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "dita2414";

    static {
        try {
            // DB_URL 접두사에 따라 드라이버 자동 로드
            if (DB_URL.startsWith("jdbc:mysql:")) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } else if (DB_URL.startsWith("jdbc:h2:")) {
                Class.forName("org.h2.Driver");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DB 오류] JDBC 드라이버 로드 실패: " + e.getMessage());
        }
    }

    /**
     * DB Connection 획득
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * 최종 재구성된 테이블 스키마 자동 생성
     */
    public static void initDatabase() {
        System.out.println("[DB] 데이터베이스 및 테이블 자동 생성 시작...");

        String[] ddlStatements = {
            // 1. users (회원)
            """
            CREATE TABLE IF NOT EXISTS users (
                user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                id VARCHAR(255) NOT NULL UNIQUE,
                password_hash VARCHAR(255) NOT NULL,
                nickname VARCHAR(255) NOT NULL,
                gender ENUM('M', 'F') NOT NULL,
                birth_date DATE NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            """,

            // 2. categories (카테고리)
            """
            CREATE TABLE IF NOT EXISTS categories (
                category_id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(50) NOT NULL UNIQUE
            );
            """,

            // 3. programs (프로그램 목록)
            """
            CREATE TABLE IF NOT EXISTS programs (
                program_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                process_name VARCHAR(255) NOT NULL UNIQUE,
                display_name VARCHAR(255) NOT NULL,
                category_id INT,
                FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE SET NULL
            );
            """,

            // 3-1. program_likes (프로그램 좋아요)
            """
            CREATE TABLE IF NOT EXISTS program_likes (
                user_id BIGINT NOT NULL,
                program_id BIGINT NOT NULL,
                PRIMARY KEY (user_id, program_id),
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                FOREIGN KEY (program_id) REFERENCES programs(program_id) ON DELETE CASCADE
            );
            """,

            // 3-2. program_like_stats (프로그램 좋아요 집계)
            """
            CREATE TABLE IF NOT EXISTS program_like_stats (
                program_id BIGINT PRIMARY KEY,
                like_count BIGINT DEFAULT 0,
                FOREIGN KEY (program_id) REFERENCES programs(program_id) ON DELETE CASCADE
            );
            """,

            // 4. usage_logs (실시간 프로그램 사용 로그 — 시간대별 집계)
            """
            CREATE TABLE IF NOT EXISTS usage_logs (
                log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                program_id BIGINT NOT NULL,
                usage_date DATE NOT NULL,
                usage_hour TINYINT NOT NULL,
                duration_sec INT NOT NULL DEFAULT 0,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                FOREIGN KEY (program_id) REFERENCES programs(program_id) ON DELETE CASCADE,
                UNIQUE KEY uq_user_program_date_hour (user_id, program_id, usage_date, usage_hour)
            );
            """,

            // 5. daily_usage_summary (일간 통계 요약)
            """
            CREATE TABLE IF NOT EXISTS daily_usage_summary (
                summary_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                program_id BIGINT NOT NULL,
                summary_date DATE NOT NULL,
                total_duration_sec BIGINT NOT NULL DEFAULT 0,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                FOREIGN KEY (program_id) REFERENCES programs(program_id) ON DELETE CASCADE,
                UNIQUE KEY uq_user_program_date (user_id, program_id, summary_date),
                INDEX idx_user_date (user_id, summary_date)
            );
            """,

                /* 삭제
            // 6. program_rankings (프로그램 인기 순위)
            """
            CREATE TABLE IF NOT EXISTS program_rankings (
                ranking_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                program_id BIGINT NOT NULL,
                period_type ENUM('DAILY', 'WEEKLY', 'MONTHLY') NOT NULL DEFAULT 'DAILY',
                ranking_date DATE NOT NULL,
                rank_position INT NOT NULL,
                total_duration_sec BIGINT NOT NULL DEFAULT 0,
                gender ENUM('M', 'F'),
                age_group VARCHAR(20),
                FOREIGN KEY (program_id) REFERENCES programs(program_id) ON DELETE CASCADE,
                UNIQUE KEY uq_ranking (program_id, period_type, ranking_date, gender, age_group)
            );
            """,

                 */

            // 7. program_usage_stats (프로그램 평균 사용률 통계)
            """
            CREATE TABLE IF NOT EXISTS program_usage_stats (
                stat_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                program_id BIGINT NOT NULL,
                stat_date DATE NOT NULL,
                avg_duration_sec DOUBLE NOT NULL DEFAULT 0,
                total_users INT NOT NULL DEFAULT 0,
                gender ENUM('M', 'F'),
                age_group VARCHAR(20),
                FOREIGN KEY (program_id) REFERENCES programs(program_id) ON DELETE CASCADE,
                UNIQUE KEY uq_stat (program_id, stat_date, gender, age_group)
            );
            """,

            // 8. posts (게시글)
            """
            CREATE TABLE IF NOT EXISTS posts (
                post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                program_id BIGINT,
                title VARCHAR(255) NOT NULL,
                content TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                is_active BOOLEAN DEFAULT TRUE,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                FOREIGN KEY (program_id) REFERENCES programs(program_id) ON DELETE SET NULL
            );
            """,

            // 9. comments (댓글)
            """
            CREATE TABLE IF NOT EXISTS comments (
                comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                post_id BIGINT NOT NULL,
                user_id BIGINT NOT NULL,
                content TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                is_active BOOLEAN DEFAULT TRUE,
                FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            );
            """,

            // 10. post_likes (게시글 좋아요)
            """
            CREATE TABLE IF NOT EXISTS post_likes (
                user_id BIGINT NOT NULL,
                post_id BIGINT NOT NULL,
                PRIMARY KEY (user_id, post_id),
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE
            );
            """,

            // 11. prediction_events (예측 이벤트)
            """
            CREATE TABLE IF NOT EXISTS prediction_events (
                event_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                title VARCHAR(50) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                voting_deadline TIMESTAMP NOT NULL,
                status ENUM('OPEN', 'CLOSED', 'ANNOUNCED') NOT NULL DEFAULT 'ANNOUNCED',
                target_date DATE NOT NULL,
                event_type ENUM('WEEKLY', 'MONTHLY') NOT NULL
            );
            """,

            // 12. prediction_votes (예측 투표)
            """
            CREATE TABLE IF NOT EXISTS prediction_votes (
                vote_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                event_id BIGINT NOT NULL,
                user_id BIGINT NOT NULL,
                program_id BIGINT NOT NULL,
                FOREIGN KEY (event_id) REFERENCES prediction_events(event_id) ON DELETE CASCADE,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                FOREIGN KEY (program_id) REFERENCES programs(program_id) ON DELETE CASCADE
            );
            """
        };

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            for (String sql : ddlStatements) {
                stmt.execute(sql);
            }
            migrateEmailColumnToId(conn);
            System.out.println("[DB] 최종 스키마의 " + ddlStatements.length + "개 테이블 생성 성공!");

        } catch (SQLException e) {
            System.err.println("[DB 오류] 테이블 생성 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void migrateEmailColumnToId(Connection connection) throws SQLException {
        boolean hasId = false;
        boolean hasLegacyEmail = false;

        String columnSql = """
            SELECT COLUMN_NAME
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'users'
            """;

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(columnSql)) {

            while(result.next()) {
                String columnName = result.getString("COLUMN_NAME");

                if("id".equalsIgnoreCase(columnName)) {
                    hasId = true;
                }

                if("email".equalsIgnoreCase(columnName)) {
                    hasLegacyEmail = true;
                }
            }
        }

        if(!hasId && hasLegacyEmail) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(
                    "ALTER TABLE users CHANGE COLUMN email id VARCHAR(255) NOT NULL"
                );
            }
        }
    }

    public static void main(String[] args) {
        initDatabase();
    }
}
