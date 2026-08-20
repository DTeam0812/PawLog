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
3. `main` 메서드 위의 **Run**을 누릅니다.

Windows API를 사용하는 `Main` 실행 시 JVM 옵션으로 아래 값을 사용합니다.

```text
--enable-native-access=ALL-UNNAMED
```

## DB 스키마
```mermaid
erDiagram
    users ||--o{ program_likes : "likes"
    programs ||--o{ program_likes : "liked by"
    categories ||--o{ programs : "categorizes"
    users ||--o{ usage_logs : "records"
    programs ||--o{ usage_logs : "tracked in"
    users ||--o{ daily_usage_summary : "summarizes"
    programs ||--o{ daily_usage_summary : "summarized in"
    programs ||--o{ program_rankings : "ranked in"
    programs ||--o{ program_usage_stats : "analyzed in"
    users ||--o{ posts : "writes"
    programs ||--o{ posts : "tagged in"
    posts ||--o{ comments : "has"
    users ||--o{ comments : "writes"
    users ||--o{ post_likes : "likes"
    posts ||--o{ post_likes : "liked by"
    prediction_events ||--o{ prediction_votes : "contains"
    users ||--o{ prediction_votes : "votes"
    programs ||--o{ prediction_votes : "voted for"

    users {
        bigint user_id PK "사용자 고유 번호"
        varchar email UK "이메일 계정"
        varchar password_hash "암호화된 비밀번호"
        varchar nickname "닉네임"
        enum gender "성별 (M, F)"
        date birth_date "생년월일"
        datetime created_at "가입 일시"
    }

    categories {
        int category_id PK "카테고리 고유 번호"
        varchar name UK "카테고리명"
    }

    programs {
        bigint program_id PK "프로그램 고유 번호"
        varchar process_name UK "실행 파일명"
        varchar display_name "화면 표시용 이름"
        int category_id FK "카테고리 번호"
    }

    program_likes {
        bigint user_id PK, FK "사용자 번호"
        bigint program_id PK, FK "프로그램 번호"
    }

    usage_logs {
        bigint log_id PK "사용 기록 고유 번호"
        bigint user_id FK "사용자 번호"
        bigint program_id FK "프로그램 번호"
        date usage_date "사용 날짜"
        tinyint usage_hour "사용 시간대 (0~23)"
        int duration_sec "누적 사용 시간(초)"
    }

    daily_usage_summary {
        bigint summary_id PK "요약 데이터 고유 번호"
        bigint user_id FK "사용자 번호"
        bigint program_id FK "프로그램 번호"
        date summary_date "집계 대상 날짜"
        bigint foreground_duration_sec "포그라운드 시간"
        bigint background_duration_sec "백그라운드 시간"
        bigint total_duration_sec "총 사용 시간(초)"
    }

    program_rankings {
        bigint ranking_id PK "순위 기록 고유 번호"
        bigint program_id FK "프로그램 번호"
        enum period_type "집계 주기 (DAILY, WEEKLY, MONTHLY)"
        date ranking_date "순위 기준 날짜"
        int rank_position "순위"
        bigint total_duration_sec "총 사용 시간"
        enum gender "분류 성별 (M, F)"
        varchar age_group "분류 연령대"
    }

    program_usage_stats {
        bigint stat_id PK "통계 데이터 고유 번호"
        bigint program_id FK "프로그램 번호"
        enum period_type "집계 주기 (DAILY, WEEKLY, MONTHLY)"
        date stat_date "통계 기준 날짜"
        float avg_foreground_sec "평균 포그라운드 시간"
        float avg_background_sec "평균 백그라운드 시간"
        float avg_duration_sec "하루 평균 총 사용 시간"
        int total_users "이용 유저 수"
        enum gender "분류 성별 (M, F)"
        varchar age_group "분류 연령대"
    }

    posts {
        bigint post_id PK "게시글 고유 번호"
        bigint user_id FK "작성자 번호"
        bigint program_id FK "태그 프로그램 번호"
        varchar title "게시글 제목"
        text content "본문 내용"
        datetime created_at "작성 일시"
    }

    comments {
        bigint comment_id PK "댓글 고유 번호"
        bigint post_id FK "게시글 번호"
        bigint user_id FK "작성자 번호"
        text content "댓글 내용"
        datetime created_at "작성 일시"
    }

    post_likes {
        bigint user_id PK, FK "사용자 번호"
        bigint post_id PK, FK "게시글 번호"
    }

    prediction_events {
        bigint event_id PK "예측 이벤트 고유 번호"
        varchar title "이벤트 제목"
        datetime created_at "생성 일시"
        datetime voting_deadline "마감 기한"
        enum status "상태 (OPEN, CLOSED, ANNOUNCED)"
        date target_date "예측 대상 날짜"
        enum event_type "단위 (WEEKLY, MONTHLY)"
    }

    prediction_votes {
        bigint vote_id PK "투표 기록 고유 번호"
        bigint event_id FK "예측 이벤트 번호"
        bigint user_id FK "투표 사용자 번호"
        bigint program_id FK "대상 프로그램 번호"
    }
```