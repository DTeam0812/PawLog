package com.pawlog.common.dto.usage;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


public class MyinfoDto {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String id;//사용자 계정
    }
}
