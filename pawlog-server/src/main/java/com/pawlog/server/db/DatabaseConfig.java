package com.pawlog.server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {

    private static final String DB_URL = "jdbc:mysql://cdn.ditanet.duckdns.org:8306/PawLog?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
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
     * README.md 스키마 기반 13개 테이블 자동 생성
     */
    public static void initDatabase() {
        System.out.println("[DB] 데이터베이스 및 테이블 자동 생성 시작...");

        String[] ddlStatements = {
            // 1. users (회원)
            """
            CREATE TABLE IF NOT EXISTS users (
                user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                email VARCHAR(255) NOT NULL UNIQUE,
                password_hash VARCHAR(255) NOT NULL,
                nickname VARCHAR(100),
                gender VARCHAR(10),
                birth_date DATE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            """,

            // 2. categories (카테고리)
            """
            CREATE TABLE IF NOT EXISTS categories (
                category_id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL UNIQUE
            );
            """,

            // 3. programs (프로그램)
            """
            CREATE TABLE IF NOT EXISTS programs (
                program_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                process_name VARCHAR(255) NOT NULL UNIQUE,
                display_name VARCHAR(255),
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

            // 3-2. program_like_stats(프로그램 좋아요 집계)
            """
            CREATE TABLE IF NOT EXISTS program_like_stats (
            program_id BIGINT PRIMARY KEY,
            like_count BIGINT DEFAULT 0,
            FOREIGN KEY (program_id) REFERENCES programs(program_id) ON DELETE CASCADE
            );
            """,

            // 4. usage_logs (개별 사용 로그)
            """
            CREATE TABLE IF NOT EXISTS usage_logs (
                log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT,
                program_id BIGINT,
                is_foreground BOOLEAN DEFAULT TRUE,
                start_time TIMESTAMP NOT NULL,
                end_time TIMESTAMP NOT NULL,
                duration_sec INT NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                FOREIGN KEY (program_id) REFERENCES programs(program_id) ON DELETE CASCADE
            );
            """,

            // 5. daily_usage_summary (일별 요약)
            """
            CREATE TABLE IF NOT EXISTS daily_usage_summary (
                summary_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT,
                program_id BIGINT,
                summary_date DATE NOT NULL,
                foreground_duration_sec BIGINT DEFAULT 0,
                background_duration_sec BIGINT DEFAULT 0,
                total_duration_sec BIGINT DEFAULT 0,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                FOREIGN KEY (program_id) REFERENCES programs(program_id) ON DELETE CASCADE,
                INDEX idx_user_date (user_id, summary_date)
            );
            """,

            // 6. program_rankings (프로그램 순위)
            """
            CREATE TABLE IF NOT EXISTS program_rankings (
                ranking_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                program_id BIGINT,
                period_type VARCHAR(20) NOT NULL,
                ranking_date DATE NOT NULL,
                rank_position INT NOT NULL,
                total_duration_sec BIGINT DEFAULT 0,
                gender VARCHAR(10),
                age_group VARCHAR(20),
                FOREIGN KEY (program_id) REFERENCES programs(program_id) ON DELETE CASCADE
            );
            """,

            // 7. program_usage_stats (프로그램 통계)
            """
            CREATE TABLE IF NOT EXISTS program_usage_stats (
                stat_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                program_id BIGINT,
                period_type VARCHAR(20) NOT NULL,
                stat_date DATE NOT NULL,
                avg_foreground_sec DOUBLE DEFAULT 0,
                avg_background_sec DOUBLE DEFAULT 0,
                avg_duration_sec DOUBLE DEFAULT 0,
                total_users INT DEFAULT 0,
                gender VARCHAR(10),
                age_group VARCHAR(20),
                FOREIGN KEY (program_id) REFERENCES programs(program_id) ON DELETE CASCADE
            );
            """,

            // 8. posts (게시글)
            """
            CREATE TABLE IF NOT EXISTS posts (
                post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT,
                program_id BIGINT,
                title VARCHAR(255) NOT NULL,
                content TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                FOREIGN KEY (program_id) REFERENCES programs(program_id) ON DELETE SET NULL
            );
            """,

            // 9. comments (댓글)
            """
            CREATE TABLE IF NOT EXISTS comments (
                comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                post_id BIGINT,
                user_id BIGINT,
                content TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            );
            """,

            // 10. post_likes (게시글 좋아요)
            """
            CREATE TABLE IF NOT EXISTS post_likes (
                user_id BIGINT,
                post_id BIGINT,
                PRIMARY KEY (user_id, post_id),
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE
            );
            """,

            // 11. program_recommendations (프로그램 추천 요청) 삭제예정
            """
            CREATE TABLE IF NOT EXISTS program_recommendations (
                rec_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT,
                program_id BIGINT,
                reason TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                FOREIGN KEY (program_id) REFERENCES programs(program_id) ON DELETE CASCADE
            );
            """,

            // 12. prediction_events (예측 이벤트)
            """
            CREATE TABLE IF NOT EXISTS prediction_events (
                event_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                voting_deadline TIMESTAMP NOT NULL,
                status VARCHAR(20) DEFAULT 'OPEN',
                target_date DATE NOT NULL,
                event_type VARCHAR(20) NOT NULL
            );
            """,

            // 13. prediction_votes (예측 투표)
            """
            CREATE TABLE IF NOT EXISTS prediction_votes (
                vote_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                event_id BIGINT,
                user_id BIGINT,
                program_id BIGINT,
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
            System.out.println("[DB] 스키마의 13개 테이블 생성 성공!");

        } catch (SQLException e) {
            System.err.println("[DB 오류] 테이블 생성 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        initDatabase();
    }
}
