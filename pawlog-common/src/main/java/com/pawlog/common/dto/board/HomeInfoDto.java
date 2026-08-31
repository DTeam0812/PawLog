package com.pawlog.common.dto.board;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class HomeInfoDto {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request{
        String id;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Recentactiveprogram{
        String programname ;
        String programusedtime;
        int programratio;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Homeinfo{
        String todatactivetime;
        String mostusedprogram;
        int todayactivecount;
        List<Recentactiveprogram> list;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{
        boolean success;
        int code;
        String message;
        Homeinfo data;
    }


}
