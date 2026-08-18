package com.pawlog.common.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestPacket {
    private String command; // REGISTER, LOGIN, USAGE_LOG_SAVE 등 명령어
    private String data;    // 각 DTO를 JSON 문자열로 직렬화한 값
}