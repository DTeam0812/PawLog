package com.pawlog.common.protocol;

public class ServerProtocol {
    // 요청 커맨드 (Command)
    public static final String REGISTER = "REGISTER";
    public static final String LOGIN = "LOGIN";
    public static final String AUTH_UPDATE = "AUTH_UPDATE"; // 마이페이지 정보 수정
    public static final String POPULAR_RANKING_GET = "POPULAR_RANKING_GET"; // 인기 프로그램 순위 조회
    public static final String MY_STATISTICS_GET = "MY_STATISTICS_GET";     // 기간 지정 통계 조회
    public static final String TODAY_STATISTICS_GET = "TODAY_STATISTICS_GET"; // 오늘 통계 조회
    public static final String USAGE_LOG_SAVE = "USAGE_LOG_SAVE";
    public static final String USAGE_LOG_GET = "USAGE_LOG_GET";
    public static final String PROGRAM_SEARCH = "PROGRAM_SEARCH";
    public static final String POST_LIST_GET = "POST_LIST_GET";
    public static final String POST_DETAIL_GET = "POST_DETAIL_GET";
    public static final String POST_ENROLL = "POST_ENROLL";
     
    public static final String COMMENT_LIST_GET = "COMMENT_LIST_GET";
    // 서버 푸시 커맨드 (Server → Client)
    public static final String SESSION_FORCE_LOGOUT = "SESSION_FORCE_LOGOUT"; // 중복 로그인 감지 시 기존 세션에 강제 로그아웃 푸시

    // 구분자 (Delimiter)
    public static final String MODE1 = ":"; // Command와 Data 분리
    public static final String MODE2 = ";"; // Data 내 필드 분리

    // 응답 결과 (Status)
    public static final String SUCCESS = "SUCCESS";
    public static final String FAIL = "FAIL";
}
