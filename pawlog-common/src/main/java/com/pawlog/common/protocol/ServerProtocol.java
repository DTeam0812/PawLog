package com.pawlog.common.protocol;

public class ServerProtocol {
    // 요청 커맨드 (Command)
    public static final String REGISTER = "REGISTER";
    public static final String LOGIN = "LOGIN";
    public static final String USAGE_LOG_SAVE = "USAGE_LOG_SAVE";
    public static final String USAGE_LOG_GET = "USAGE_LOG_GET";
    public static final String CATEGORY_GET = "CATEGORY_GET";

    // 구분자 (Delimiter)
    public static final String MODE1 = ":"; // Command와 Data 분리
    public static final String MODE2 = ";"; // Data 내 필드 분리

    // 응답 결과 (Status)
    public static final String SUCCESS = "SUCCESS";
    public static final String FAIL = "FAIL";
}
