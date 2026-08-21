# PawLog

Windows의 활성 창을 기준으로 프로그램 사용 시간을 기록하고 통계를 보여주는 Java 애플리케이션입니다.

## 개발 환경

- Windows
- JDK 24
- Java Swing/AWT
- JNA (`lib` 폴더)
- H2 Database (`lib` 폴더)

## VS Code에서 실행

1. Extension Pack for Java 확장을 설치합니다.
2. `src/ui.Widget.java` 또는 `src/Main.java`를 엽니다.
3. `main` 메�## DB 스키마

```mermaid
erDiagram
    users ||--o{ programs_likes : "likes"
    programs ||--o{ programs_likes : "liked by"

    users ||--o{ usage_logs : "records"
    programs ||--o{ usage_logs : "logged in"

    users ||--o{ daily_usage_summary : "summarizes"
    programs ||--o{ daily_usage_summary : "summarized in"

    categories ||--o{ programs : "classifies"
    programs ||--o{ program_usage_stats : "has stats"

    users ||--o{ posts : "writes"
    programs ||--o{ posts : "tagged in"

    posts ||--o{ comments : "contains"
    users ||--o{ comments : "writes"

    users ||--o{ post_likes : "likes"
    posts ||--o{ post_likes : "liked by"

    prediction_events ||--o{ prediction_votes : "contains"
    users ||--o{ prediction_votes : "casts"
    programs ||--o{ prediction_votes : "voted for"

    users {
        bigint user_id PK "AUTO_INCREMENT"
        varchar(255) email UK "NOT NULL"
        varchar(255) password_hash "NOT NULL"
        varchar(255) nickname "NOT NULL"
        enum gender "NOT NULL ('M', 'F')"
        date birth_date "NOT NULL"
        datetime created_at "NOT NULL, DEFAULT CURRENT_TIMESTAMP"
    }

    categories {
        int category_id PK "AUTO_INCREMENT"
        varchar(50) name UK "NOT NULL"
    }

    programs {
        bigint program_id PK "AUTO_INCREMENT"
        varchar(255) process_name UK "NOT NULL"
        varchar(255) display_name "NOT NULL"
        int category_id FK "NULLABLE"
    }

    programs_likes {
        bigint program_id PK, FK
        bigint user_id PK, FK
    }

    usage_logs {
        bigint log_id PK "AUTO_INCREMENT"
        bigint user_id FK "NOT NULL"
        bigint program_id FK "NOT NULL"
        date usage_date "NOT NULL"
        tinyint usage_hour "NOT NULL (0~23)"
        int duration_sec "NOT NULL"
    }

    daily_usage_summary {
        bigint summary_id PK "AUTO_INCREMENT"
        bigint user_id FK "NOT NULL"
        bigint program_id FK "NOT NULL"
        date summary_date "NOT NULL"
        bigint total_duration_sec "NOT NULL, DEFAULT 0"
    }

    program_usage_stats {
        bigint stat_id PK "AUTO_INCREMENT"
        bigint program_id FK "NOT NULL"
        date stat_date "NOT NULL"
        float avg_foreground_sec "NOT NULL, DEFAULT 0"
        float avg_background_sec "NOT NULL, DEFAULT 0"
        float avg_duration_sec "NOT NULL"
        int total_users "NOT NULL"
        enum gender "NULLABLE ('M', 'F')"
        varchar(20) age_group "NULLABLE"
    }

    posts {
        bigint post_id PK "AUTO_INCREMENT"
        bigint user_id FK "NOT NULL"
        bigint program_id FK "NULLABLE"
        varchar(255) title "NOT NULL"
        text content "NOT NULL"
        datetime created_at "DEFAULT CURRENT_TIMESTAMP"
    }

    comments {
        bigint comment_id PK "AUTO_INCREMENT"
        bigint post_id FK "NOT NULL"
        bigint user_id FK "NOT NULL"
        text content "NOT NULL"
        datetime created_at "DEFAULT CURRENT_TIMESTAMP"
    }

    post_likes {
        bigint user_id PK, FK
        bigint post_id PK, FK
    }

    prediction_events {
        bigint event_id PK "AUTO_INCREMENT"
        varchar(50) title "NOT NULL"
        datetime created_at "DEFAULT CURRENT_TIMESTAMP"
        datetime voting_deadline "NOT NULL"
        enum status "NOT NULL, DEFAULT 'ANNOUNCED'"
        date target_date "NOT NULL"
        enum event_type "NOT NULL ('WEEKLY', 'MONTHLY')"
    }

    prediction_votes {
        bigint vote_id PK "AUTO_INCREMENT"
        bigint event_id FK "NOT NULL"
        bigint user_id FK "NOT NULL"
        bigint program_id FK "NOT NULL"
    }
```