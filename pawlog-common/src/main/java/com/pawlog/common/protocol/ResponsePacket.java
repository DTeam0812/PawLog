package com.pawlog.common.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponsePacket {
    private String command; // 명령어 (REGISTER, LOGIN 등)
    private boolean success; // 성공 여부
    private int code;       // 상태 코드 (200, 400, 500 등)
    private String message; // 결과 메시지
    private String data;    // 응답 데이터 (JSON 문자열 또는 null)
}