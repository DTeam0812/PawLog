package com.pawlog.common.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponsePacket {
    private boolean success;
    private String message;
    private String data;    // 응답 데이터 (JSON 문자열)
}