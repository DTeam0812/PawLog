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
    programs ||--o{ programs_likes : "is liked by"

    users ||--o{ usage_logs : "records"
    programs ||--o{ usage_logs : "logged in"

    users ||--o{ daily_usage_summary : "has"
    programs ||--o{ daily_usage_summary : "summarized in"

    programs ||--o{ program_usage_stats : "aggregated into"

    users ||--o{ posts : "writes"
    programs ||--o{ posts : "tagged in"

    posts ||--o{ comments : "contains"
    users ||--o{ comments : "writes"

    users ||--o{ post_likes : "likes"
    posts ||--o{ post_likes : "is liked by"

    prediction_events ||--o{ prediction_votes : "includes"
    users ||--o{ prediction_votes : "casts"
    programs ||--o{ prediction_votes : "voted for"

    users {
        BIGINT user_id PK
        VARCHAR(255) email UK
        VARCHAR(255) password_hash
        VARCHAR(255) nickname
        ENUM gender
        DATE birth_date
        DATETIME created_at
    }

    programs {
        BIGINT program_id PK
        VARCHAR(255) process_name UK
        VARCHAR(255) display_name
    }

    programs_likes {
        BIGINT program_id PK,FK
        BIGINT user_id PK,FK
    }

    usage_logs {
        BIGINT log_id PK
        BIGINT user_id FK
        BIGINT program_id FK
        DATE usage_date
        TINYINT usage_hour
        INT duration_sec
    }

    daily_usage_summary {
        BIGINT summary_id PK
        BIGINT user_id FK
        BIGINT program_id FK
        DATE summary_date
        BIGINT total_duration_sec
    }

    program_usage_stats {
        BIGINT stat_id PK
        BIGINT program_id FK
        DATE stat_date
        FLOAT avg_duration_sec
        INT total_users
        ENUM gender
        VARCHAR(20) age_group
    }

    posts {
        BIGINT post_id PK
        BIGINT user_id FK
        BIGINT program_id FK
        VARCHAR(50) post_type
        VARCHAR(255) title
        TEXT content
        DATETIME created_at
    }

    comments {
        BIGINT comment_id PK
        BIGINT post_id FK
        BIGINT user_id FK
        TEXT content
        DATETIME created_at
    }

    post_likes {
        BIGINT user_id PK,FK
        BIGINT post_id PK,FK
    }

    prediction_events {
        BIGINT event_id PK
        VARCHAR(50) title
        DATETIME created_at
        DATETIME voting_deadline
        ENUM status
        DATE target_date
        ENUM event_type
    }

    prediction_votes {
        BIGINT vote_id PK
        BIGINT event_id FK
        BIGINT user_id FK
        BIGINT program_id FK
    }
```