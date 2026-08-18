package com.pawlog.server.net;

import com.pawlog.common.dto.ranking.PopularRankingDto;
import com.pawlog.common.dto.user.UserLoginDto;
import com.pawlog.common.dto.user.UserRegisterDto;
import com.pawlog.common.protocol.RequestPacket;
import com.pawlog.common.protocol.ResponsePacket;
import com.pawlog.common.protocol.ServerProtocol;
import com.pawlog.common.util.JsonUtil;
import com.pawlog.server.entity.UserEntity;
import com.pawlog.server.service.AuthService;
import com.pawlog.server.service.RankingService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Date;

public class ClientThread extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private AuthService authService;
    private RankingService rankingService;

    // 로그인된 유저 세션 정보
    private Long loggedInUserId;
    private UserEntity currentUser;

    public ClientThread(Socket socket) {
        try {
            this.socket = socket;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.authService = new AuthService();
            this.rankingService = new RankingService();
            System.out.println(socket + " 접속 성공.......");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Long getLoggedInUserId() {
        return loggedInUserId;
    }

    public UserEntity getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return loggedInUserId != null;
    }

    public void setCurrentUser(UserEntity user) {
        this.currentUser = user;
        this.loggedInUserId = user != null ? user.getUserId() : null;
    }


    @Override
    public void run() {
        try {
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                routine(line);
            }
        } catch (Exception e) {
            System.err.println(socket + " 접속 종료.......");
        } finally {
            closeAll();
        }
    }

    // [Controller 역할]
    private void routine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return;
        }

        // 1. JSON 기반 패킷 처리 ({ 로 시작하는 경우)
        if (line.trim().startsWith("{")) {
            try {
                RequestPacket packet = JsonUtil.fromJson(line, RequestPacket.class);
                String command = packet.getCommand();
                String dataJson = packet.getData();

                if (ServerProtocol.REGISTER.equals(command)) {
                    handleRegisterJson(dataJson);
                }
                else if (ServerProtocol.LOGIN.equals(command)) {
                    handleLogin(dataJson);
                }
                else if (ServerProtocol.POPULAR_RANKING_GET.equals(command)) {
                    handlePopularRanking(dataJson);
                }
                else if (ServerProtocol.USAGE_LOG_SAVE.equals(command)){
                    handleUsageLogSave();
                }
                else if (ServerProtocol.USAGE_LOG_GET.equals(command)){
                    handleUsageLogGet();
                }
                else if (ServerProtocol.CATEGORY_GET.equals(command)){
                    handleCategoryGet();
                }
                return;
            } catch (Exception e) {
                e.printStackTrace();
                ResponsePacket errorRes = new ResponsePacket("UNKNOWN", false, 400, "잘못된 JSON 요청 형식입니다.", null);
                sendMessage(JsonUtil.toJson(errorRes));
                return;
            }
        }

        // 2. 기존 구분자(:) 방식 폴백 (하위 호환성)
        String[] commandData = parseRoutine(line, ServerProtocol.MODE1);
        String command = commandData[0];
        String data = commandData[1];

        /*
        * 라우팅 영역
        * */
        if (ServerProtocol.REGISTER.equals(command)) {
            handleRegister(data);
        }
        else if (ServerProtocol.LOGIN.equals(command)) {
            handleLogin(data);
        }
        else if (ServerProtocol.POPULAR_RANKING_GET.equals(command)) {
            handlePopularRanking(data);
        }
        else if (ServerProtocol.USAGE_LOG_SAVE.equals(command)){
            handleUsageLogSave();
        }
        else if (ServerProtocol.USAGE_LOG_GET.equals(command)){
            handleUsageLogGet();
        }
        else if (ServerProtocol.CATEGORY_GET.equals(command)){
            handleCategoryGet();
        }
    }

    // 회원가입 (JSON 처리)
    private void handleRegisterJson(String dataJson) {
        try {
            UserRegisterDto.Request reqDto = JsonUtil.fromJson(dataJson, UserRegisterDto.Request.class);
            UserRegisterDto.Response resDto = authService.register(reqDto);

            ResponsePacket responsePacket = new ResponsePacket(
                ServerProtocol.REGISTER,
                resDto.isSuccess(),
                resDto.getCode(),
                resDto.getMessage(),
                null
            );

            sendMessage(JsonUtil.toJson(responsePacket));
        } catch (Exception e) {
            e.printStackTrace();
            ResponsePacket responsePacket = new ResponsePacket(
                ServerProtocol.REGISTER,
                false,
                400,
                "회원가입 요청 데이터 파싱 실패: " + e.getMessage(),
                null
            );
            sendMessage(JsonUtil.toJson(responsePacket));
        }
    }

    // 회원가입 파싱 및 처리
    private void handleRegister(String data) {
        try {
            // data 포맷: email;password;nickname;gender;birthDate
            String[] params = data.split(ServerProtocol.MODE2);
            if (params.length < 5) {
                sendMessage(ServerProtocol.FAIL + ServerProtocol.MODE1 + "파라미터가 부족합니다.");
                return;
            }

            String email = params[0];
            String password = params[1];
            String nickname = params[2];
            String gender = params[3];
            String birth = params[4];

            // Request DTO 생성
            UserRegisterDto.Request reqDto = new UserRegisterDto.Request(nickname, email, password, birth, gender);
            
            // Service 호출 및 Response DTO 수신
            UserRegisterDto.Response resDto = authService.register(reqDto);

            // 결과 소켓 응답 전송
            if (resDto.isSuccess()) {
                sendMessage(ServerProtocol.SUCCESS + ServerProtocol.MODE1 + resDto.getMessage());
            } else {
                sendMessage(ServerProtocol.FAIL + ServerProtocol.MODE1 + resDto.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(ServerProtocol.FAIL + ServerProtocol.MODE1 + "회원가입 처리 중 오류 발생");
        }
    }

    // 로그인 파싱 및 처리
    private void handleLogin(String data) {
        try {
            String[] params = data.split(ServerProtocol.MODE2);
            if (params.length < 2) {
                sendMessage(ServerProtocol.FAIL + ServerProtocol.MODE1 + "파라미터가 부족합니다.");
                return;
            }

            String email = params[0];
            String password = params[1];

            // Request DTO 생성
            UserLoginDto.Request reqDto = new UserLoginDto.Request(email, password);

            // Service 호출 및 Response DTO 수신
            UserLoginDto.Response resDto = authService.login(reqDto);

            // 결과 소켓 응답 전송
            if (resDto.isSuccess()) {
                sendMessage(ServerProtocol.SUCCESS + ServerProtocol.MODE1 + resDto.getMessage());
            } else {
                sendMessage(ServerProtocol.FAIL + ServerProtocol.MODE1 + resDto.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(ServerProtocol.FAIL + ServerProtocol.MODE1 + "로그인 처리 중 오류 발생");
        }
    }

    // 인기 프로그램 순위 파싱 및 처리
    private void handlePopularRanking(String data) {
        try {
            String ageGroup = "ALL";
            String gender = "ALL";
            String periodType = "DAILY";

            if (data != null && !data.isEmpty()) {
                String[] params = data.split(ServerProtocol.MODE2);
                if (params.length > 0 && !params[0].isEmpty()) ageGroup = params[0];
                if (params.length > 1 && !params[1].isEmpty()) gender = params[1];
                if (params.length > 2 && !params[2].isEmpty()) periodType = params[2];
            }

            // Request DTO 생성
            PopularRankingDto.Request reqDto = new PopularRankingDto.Request(ageGroup, gender, periodType);

            // Service 호출
            PopularRankingDto.Response resDto = rankingService.getPopularRankings(reqDto);

            if (resDto.isSuccess()) {
                StringBuilder responseData = new StringBuilder();
                responseData.append(ServerProtocol.SUCCESS).append(ServerProtocol.MODE1).append(resDto.getMessage());

                for (PopularRankingDto.RankingItem item : resDto.getRankings()) {
                    responseData.append(ServerProtocol.MODE2)
                                .append(item.getRank()).append(",")
                                .append(item.getProgramName()).append(",")
                                .append(item.getCategoryName()).append(",")
                                .append(item.getAvgUsageFormatted()).append(",")
                                .append(item.getUsagePercentage()).append("%");
                }
                sendMessage(responseData.toString());
            } else {
                sendMessage(ServerProtocol.FAIL + ServerProtocol.MODE1 + resDto.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(ServerProtocol.FAIL + ServerProtocol.MODE1 + "인기 순위 조회 중 오류 발생");
        }
    }

    public void handleUsageLogSave(){

    }

    public void handleUsageLogGet(){

    }

    public void handleCategoryGet(){

    }

    // 클라이언트로 메시지 전송
    public void sendMessage(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    // 구분자 기준 라인 파싱
    private String[] parseRoutine(String line, String mode) {
        int idx = line.indexOf(mode);
        if (idx == -1) {
            return new String[] {line, ""};
        }
        return new String[] {line.substring(0, idx), line.substring(idx + 1)};
    }

    // 스트림 및 소켓 종료
    private void closeAll() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
