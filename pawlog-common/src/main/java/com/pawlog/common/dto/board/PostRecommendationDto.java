package com.pawlog.common.dto.board;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PostRecommendationDto {


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request{
        int count;
        
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecommendProgram{
        String program;
        int usercount;
    }
    

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        boolean success ;
        int code;
        String message;
        List<RecommendProgram> data;
    }


}
