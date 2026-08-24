package com.pawlog.server.net;

import com.fasterxml.jackson.databind.JsonNode;
import com.pawlog.common.dto.auth.AuthLoginDto;
import com.pawlog.common.dto.auth.AuthRegisterDto;
import com.pawlog.common.dto.ranking.PopularRankingDto;
import com.pawlog.common.dto.statistics.MyStatisticsDto;
import com.pawlog.common.protocol.RequestPacket;
import com.pawlog.common.protocol.ResponsePacket;
import com.pawlog.common.protocol.ServerProtocol;
import com.pawlog.common.util.JsonUtil;
import com.pawlog.server.entity.UserEntity;
import com.pawlog.server.service.AuthService;
import com.pawlog.server.service.PopularRankingService;
import com.pawlog.server.service.StatisticsService;
import com.pawlog.common.dto.usage.UsageLogSaveDto;
import com.pawlog.server.service.UsageLogService;
import com.pawlog.common.dto.auth.AuthUpdateDto;
import com.pawlog.common.dto.board.ProgramSearchDto;
import com.pawlog.server.service.UtilService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClientThread extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private AuthService authService;
    private PopularRankingService popularRankingService;
    private UsageLogService usageLogService;
    private StatisticsService statisticsService;
    private UtilService utilService;

    // 로그인된 유저 세션 정보
    private Long loggedInUserId;
    private UserEntity currentUser;

    public ClientThread(Socket socket) {
        try {
            this.socket = socket;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            this.authService = new AuthService();
            this.popularRankingService = new PopularRankingService();
            this.usageLogService = new UsageLogService();
            this.statisticsService = new StatisticsService();
            this.utilService = new UtilService();
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
            // 연결 종료 시 세션 레지스트리에서 제거 (본인 세션만 제거)
            SessionRegistry.getInstance().remove(loggedInUserId, this);
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
                JsonNode dataNode = packet.getData();
                String dataJson = dataNode == null || dataNode.isNull() ? "{}" : dataNode.toString();

                if (ServerProtocol.REGISTER.equals(command)) {
                    handleRegisterJson(dataJson);
                }
                else if (ServerProtocol.LOGIN.equals(command)) {
                    handleLoginJson(dataJson);
                }
                else if (ServerProtocol.AUTH_UPDATE.equals(command)) {
                    handleAuthUpdateJson(dataJson);
                }
                else if (ServerProtocol.POPULAR_RANKING_GET.equals(command)) {
                    handlePopularRankingJson(dataJson);
                }
                else if (ServerProtocol.USAGE_LOG_SAVE.equals(command) || "INIT".equals(command)){
                    handleUsageLogSaveJson(dataJson);
                }
                else if (ServerProtocol.USAGE_LOG_GET.equals(command)){
                    handleUsageLogGet();
                }
                else if (ServerProtocol.MY_STATISTICS_GET.equals(command)) {
                    handleMyStatisticsJson(dataJson);
                }
                else if (ServerProtocol.PROGRAM_SEARCH.equals(command)) {
                    handleProgramSearchJson(dataJson);
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
        else if (ServerProtocol.AUTH_UPDATE.equals(command)) {
            handleAuthUpdate(data);
        }
        else if (ServerProtocol.POPULAR_RANKING_GET.equals(command)) {
            handlePopularRanking(data);
        }
        else if (ServerProtocol.USAGE_LOG_SAVE.equals(command) || "INIT".equals(command)){
            handleUsageLogSave(data);
        }
        else if (ServerProtocol.USAGE_LOG_GET.equals(command)){
            handleUsageLogGet();
        }
        else if (ServerProtocol.MY_STATISTICS_GET.equals(command)) {
            handleMyStatistics(data);
        }
        else if (ServerProtocol.PROGRAM_SEARCH.equals(command)) {
            handleProgramSearch(data);
        }
    }

    // 회원가입 (JSON 처리)
    private void handleRegisterJson(String dataJson) {
        try {
            AuthRegisterDto.Request reqDto = JsonUtil.fromJson(dataJson, AuthRegisterDto.Request.class);
            AuthRegisterDto.Response resDto = authService.register(reqDto);

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
            AuthRegisterDto.Request reqDto = new AuthRegisterDto.Request(nickname, email, password, birth, gender);
            
            // Service 호출 및 Response DTO 수신
            AuthRegisterDto.Response resDto = authService.register(reqDto);

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

    // 로그인 (JSON 처리)
    private void handleLoginJson(String dataJson) {
        try {
            AuthLoginDto.Request reqDto = JsonUtil.fromJson(dataJson, AuthLoginDto.Request.class);
            AuthLoginDto.Response resDto = authService.login(reqDto);

            if (resDto.isSuccess() && resDto.getUser() != null) {
                // 소켓 세션에 로그인 유저 정보 저장
                UserEntity userEntity = authService.getUserByEmailAndPassword(reqDto.getEmail(), reqDto.getPassword());
                this.setCurrentUser(userEntity);

                // 세션 레지스트리에 등록 (동일 userId 기존 세션이 있으면 자동으로 강제 로그아웃 푸시)
                SessionRegistry.getInstance().register(this.loggedInUserId, this);

                JsonNode dataNode = JsonUtil.toJsonNode(resDto.getUser());

                ResponsePacket responsePacket = new ResponsePacket(
                    ServerProtocol.LOGIN,
                    true,
                    resDto.getCode(),
                    resDto.getMessage(),
                    dataNode
                );
                sendMessage(JsonUtil.toJson(responsePacket));
            } else {
                ResponsePacket responsePacket = new ResponsePacket(
                    ServerProtocol.LOGIN,
                    false,
                    resDto.getCode(),
                    resDto.getMessage(),
                    null
                );
                sendMessage(JsonUtil.toJson(responsePacket));
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponsePacket responsePacket = new ResponsePacket(
                ServerProtocol.LOGIN,
                false,
                400,
                "로그인 요청 데이터 파싱 실패: " + e.getMessage(),
                null
            );
            sendMessage(JsonUtil.toJson(responsePacket));
        }
    }

    // 로그인 파싱 및 처리 (구분자 기반 폴백)
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
            AuthLoginDto.Request reqDto = new AuthLoginDto.Request(email, password);

            // Service 호출 및 Response DTO 수신
            AuthLoginDto.Response resDto = authService.login(reqDto);

            // 결과 소켓 응답 전송
            if (resDto.isSuccess()) {
                UserEntity userEntity = authService.getUserByEmailAndPassword(email, password);
                this.setCurrentUser(userEntity);
                sendMessage(ServerProtocol.SUCCESS + ServerProtocol.MODE1 + resDto.getMessage());
            } else {
                sendMessage(ServerProtocol.FAIL + ServerProtocol.MODE1 + resDto.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(ServerProtocol.FAIL + ServerProtocol.MODE1 + "로그인 처리 중 오류 발생");
        }
    }

    // 회원 정보 수정 (JSON 처리)
    private void handleAuthUpdateJson(String dataJson) {
        try {
            // 1. 비로그인 세션 차단 (401 방어)
            if (!isLoggedIn()) {
                ResponsePacket responsePacket = new ResponsePacket(
                        ServerProtocol.AUTH_UPDATE,
                        false,
                        401,
                        "로그인이 필요한 서비스입니다.",
                        null
                );
                sendMessage(JsonUtil.toJson(responsePacket));
                return;
            }

            // 2. DTO 파싱 및 Service 호출
            AuthUpdateDto.Request reqDto = JsonUtil.fromJson(dataJson, AuthUpdateDto.Request.class);
            AuthUpdateDto.Response resDto = authService.updateUser(this.loggedInUserId, reqDto);

            // 3. 닉네임이 성공적으로 변경된 경우 현재 스레드 메모리(세션)의 닉네임도 동기화
            if (resDto.isSuccess() && resDto.getUpdatedNickname() != null && this.currentUser != null) {
                this.currentUser.setNickname(resDto.getUpdatedNickname());
            }

            // 4. 응답 패킷 생성 및 전송
            ResponsePacket responsePacket = new ResponsePacket(
                    ServerProtocol.AUTH_UPDATE,
                    resDto.isSuccess(),
                    resDto.getCode(),
                    resDto.getMessage(),
                    resDto.isSuccess() ? JsonUtil.toJsonNode(resDto) : null
            );
            sendMessage(JsonUtil.toJson(responsePacket));

        } catch (Exception e) {
            e.printStackTrace();
            ResponsePacket responsePacket = new ResponsePacket(
                    ServerProtocol.AUTH_UPDATE,
                    false,
                    400,
                    "회원 정보 수정 요청 데이터 파싱 실패: " + e.getMessage(),
                    null
            );
            sendMessage(JsonUtil.toJson(responsePacket));
        }
    }

    // 회원 정보 수정 파싱 및 처리 (구분자 기반 폴백)
    private void handleAuthUpdate(String data) {
        try {
            // 1. 세션 로그인 여부 검증
            if (!isLoggedIn()) {
                sendMessage(ServerProtocol.FAIL + ServerProtocol.MODE1 + "로그인이 필요한 서비스입니다.");
                return;
            }

            if (data == null || data.trim().isEmpty()) {
                sendMessage(ServerProtocol.FAIL + ServerProtocol.MODE1 + "수정할 데이터가 없습니다.");
                return;
            }

            // data 포맷: nickname;currentPassword;newPassword
            // (구분자 뒤 빈 값 보존을 위해 limit -1 사용)
            String[] params = data.split(ServerProtocol.MODE2, -1);

            String nickname = params.length > 0 ? params[0] : "";
            String currentPassword = params.length > 1 ? params[1] : "";
            String newPassword = params.length > 2 ? params[2] : "";

            // Request DTO 조립
            AuthUpdateDto.Request reqDto = new AuthUpdateDto.Request();
            if (!nickname.trim().isEmpty()) reqDto.setNickname(nickname.trim());
            if (!currentPassword.trim().isEmpty()) reqDto.setCurrentPassword(currentPassword.trim());
            if (!newPassword.trim().isEmpty()) reqDto.setNewPassword(newPassword.trim());

            // Service 호출
            AuthUpdateDto.Response resDto = authService.updateUser(this.loggedInUserId, reqDto);

            if (resDto.isSuccess()) {
                // 현재 세션의 닉네임 동기화
                if (resDto.getUpdatedNickname() != null && this.currentUser != null) {
                    this.currentUser.setNickname(resDto.getUpdatedNickname());
                }
                sendMessage(ServerProtocol.SUCCESS + ServerProtocol.MODE1 + resDto.getMessage());
            } else {
                sendMessage(ServerProtocol.FAIL + ServerProtocol.MODE1 + resDto.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(ServerProtocol.FAIL + ServerProtocol.MODE1 + "회원 정보 수정 중 오류 발생: " + e.getMessage());
        }
    }

    // 인기 프로그램 순위 JSON 처리
    private void handlePopularRankingJson(String dataJson) {
        try {
            PopularRankingDto.Request reqDto = JsonUtil.fromJson(dataJson, PopularRankingDto.Request.class);

            if (reqDto.getStartDate() == null || reqDto.getEndDate() == null) {
                sendPopularRankingError(400, "시작일과 종료일이 필요합니다.");
                return;
            }

            if (reqDto.getStartDate().isAfter(reqDto.getEndDate())) {
                sendPopularRankingError(400, "시작일은 종료일보다 늦을 수 없습니다.");
                return;
            }

            PopularRankingDto.Response resDto = popularRankingService.getPopularRankings(reqDto);
            ResponsePacket responsePacket = new ResponsePacket(
                ServerProtocol.POPULAR_RANKING_GET,
                resDto.isSuccess(),
                resDto.getCode(),
                resDto.getMessage(),
                resDto.isSuccess() ? JsonUtil.toJsonNode(resDto.getRankings()) : null
            );
            sendMessage(JsonUtil.toJson(responsePacket));
        } catch (Exception e) {
            e.printStackTrace();
            sendPopularRankingError(400, "인기 순위 요청 데이터 파싱 실패: " + e.getMessage());
        }
    }

    private void sendPopularRankingError(int code, String message) {
        ResponsePacket responsePacket = new ResponsePacket(
            ServerProtocol.POPULAR_RANKING_GET,
            false,
            code,
            message,
            null
        );
        sendMessage(JsonUtil.toJson(responsePacket));
    }

    // 인기 프로그램 순위 구분자 방식 처리
    private void handlePopularRanking(String data) {
        try {
            String ageGroup = "ALL";
            String gender = "ALL";
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = LocalDate.now();

            if (data != null && !data.isEmpty()) {
                String[] params = data.split(ServerProtocol.MODE2);
                if (params.length > 0 && !params[0].isEmpty()) ageGroup = params[0];
                if (params.length > 1 && !params[1].isEmpty()) gender = params[1];
                if (params.length > 2 && !params[2].isEmpty()) startDate = LocalDate.parse(params[2]);
                if (params.length > 3 && !params[3].isEmpty()) endDate = LocalDate.parse(params[3]);
            }

            // Request DTO 생성
            PopularRankingDto.Request reqDto = new PopularRankingDto.Request(ageGroup, gender, startDate, endDate);

            // Service 호출
            PopularRankingDto.Response resDto = popularRankingService.getPopularRankings(reqDto);

            if (resDto.isSuccess()) {
                StringBuilder responseData = new StringBuilder();
                responseData.append(ServerProtocol.SUCCESS).append(ServerProtocol.MODE1).append(resDto.getMessage());

                // NPE 방어 로직
                if(resDto.getRankings() != null){
                    for (PopularRankingDto.RankingItem item : resDto.getRankings()) {
                        responseData.append(ServerProtocol.MODE2)
                                .append(item.getRank()).append(",")
                                .append(item.getProgramName()).append(",")
                                .append(item.getAvgUsageFormatted()).append(",")
                                .append(item.getUsagePercentage()).append("%");
                    }
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

    // 사용 기록 저장 (JSON 처리)
    private void handleUsageLogSaveJson(String dataJson) {
        try {
            UsageLogSaveDto.Request reqDto = JsonUtil.fromJson(dataJson, UsageLogSaveDto.Request.class);
            UsageLogSaveDto.Response resDto = usageLogService.saveUsageLogs(getLoggedInUserId(), reqDto);
            ResponsePacket responsePacket = new ResponsePacket(
                ServerProtocol.USAGE_LOG_SAVE,
                resDto.isSuccess(),
                resDto.getCode(),
                resDto.getMessage(),
                JsonUtil.toJsonNode(resDto)
            );
            sendMessage(JsonUtil.toJson(responsePacket));
        } catch (Exception e) {
            e.printStackTrace();
            ResponsePacket responsePacket = new ResponsePacket(
                ServerProtocol.USAGE_LOG_SAVE,
                false,
                400,
                "사용 기록 저장 요청 데이터 파싱 실패: " + e.getMessage(),
                null
            );
            sendMessage(JsonUtil.toJson(responsePacket));
        }
    }

    // 사용 기록 저장 (기존 구분자 방식)
    public void handleUsageLogSave(String data) {
        try {
            if (data == null || data.trim().isEmpty()) {
                sendMessage(ServerProtocol.FAIL + ServerProtocol.MODE1 + "저장할 데이터가 없습니다.");
                return;
            }

            String[] entries = data.split("\\|");
            String email = null;
            String usageDate = null;
            List<UsageLogSaveDto.LogItem> logItems = new ArrayList<>();

            for(String entry : entries) {
                String[] params = entry.split(ServerProtocol.MODE2);
                if (params.length < 5) continue;

                email = params[0];
                usageDate = params[1];
                int hour = Integer.parseInt(params[2]);
                String programName = params[3];
                long seconds = Long.parseLong(params[4]);
                logItems.add(new UsageLogSaveDto.LogItem(hour, programName, seconds));
            }

            UsageLogSaveDto.Request reqDto = new UsageLogSaveDto.Request(email, usageDate, logItems);
            UsageLogSaveDto.Response resDto = usageLogService.saveUsageLogs(getLoggedInUserId(), reqDto);
            String status = resDto.isSuccess() ? ServerProtocol.SUCCESS : ServerProtocol.FAIL;
            sendMessage(status + ServerProtocol.MODE1 + resDto.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(ServerProtocol.FAIL + ServerProtocol.MODE1 + "사용 기록 저장 중 오류 발생");
        }
    }

    public void handleUsageLogGet(){

    }

    // 개인 통계 조회 (JSON 처리)
    private void handleMyStatisticsJson(String dataJson) {
        try {
            MyStatisticsDto.Request reqDto = JsonUtil.fromJson(dataJson, MyStatisticsDto.Request.class);
            MyStatisticsDto.Response resDto = statisticsService.getMyStatistics(reqDto);

            ResponsePacket responsePacket = new ResponsePacket(
                ServerProtocol.MY_STATISTICS_GET,
                resDto.isSuccess(),
                resDto.getCode(),
                resDto.getMessage(),
                resDto.isSuccess() && resDto.getData() != null ? JsonUtil.toJsonNode(resDto.getData()) : null
            );
            sendMessage(JsonUtil.toJson(responsePacket));
        } catch (Exception e) {
            e.printStackTrace();
            ResponsePacket responsePacket = new ResponsePacket(
                ServerProtocol.MY_STATISTICS_GET,
                false,
                400,
                "개인 통계 요청 데이터 파싱 실패: " + e.getMessage(),
                null
            );
            sendMessage(JsonUtil.toJson(responsePacket));
        }
    }

    // 개인 통계 조회 (구분자 방식 폴백: id;startDate;endDate)
    private void handleMyStatistics(String data) {
        try {
            if (data == null || data.trim().isEmpty()) {
                sendMessage(ServerProtocol.FAIL + ServerProtocol.MODE1 + "요청 데이터가 없습니다.");
                return;
            }

            String[] params = data.split(ServerProtocol.MODE2);
            if (params.length < 3) {
                sendMessage(ServerProtocol.FAIL + ServerProtocol.MODE1 + "파라미터가 부족합니다 (id;startDate;endDate).");
                return;
            }

            String id = params[0];
            LocalDate startDate = LocalDate.parse(params[1]);
            LocalDate endDate = LocalDate.parse(params[2]);

            MyStatisticsDto.Request reqDto = new MyStatisticsDto.Request(id, startDate, endDate);
            MyStatisticsDto.Response resDto = statisticsService.getMyStatistics(reqDto);

            if (resDto.isSuccess() && resDto.getData() != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(ServerProtocol.SUCCESS).append(ServerProtocol.MODE1).append(resDto.getMessage());
                MyStatisticsDto.DataPayload payload = resDto.getData();
                sb.append(ServerProtocol.MODE2)
                  .append(payload.getTotalTime()).append(",")
                  .append(payload.getMostUsedItem()).append(",")
                  .append(payload.getAmountUsedItem());

                if (payload.getRatio() != null) {
                    for (MyStatisticsDto.ProgramRatioItem item : payload.getRatio()) {
                        sb.append(ServerProtocol.MODE2)
                          .append(item.getProgramName()).append(",")
                          .append(item.getProgramTime()).append(",")
                          .append(item.getProgramRatio()).append("%");
                    }
                }
                sendMessage(sb.toString());
            } else {
                sendMessage(ServerProtocol.FAIL + ServerProtocol.MODE1 + resDto.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(ServerProtocol.FAIL + ServerProtocol.MODE1 + "개인 통계 조회 중 오류 발생: " + e.getMessage());
        }
    }

    // 프로그램 검색 (JSON 처리)
    private void handleProgramSearchJson(String dataJson) {
        try {
            ProgramSearchDto.Request reqDto = JsonUtil.fromJson(dataJson, ProgramSearchDto.Request.class);
            ProgramSearchDto.Response resDto = utilService.searchPrograms(reqDto);

            JsonNode dataNode = null;
            if (resDto.isSuccess() && resDto.getPrograms() != null) {
                dataNode = JsonUtil.toJsonNode(resDto.getPrograms());
            }

            ResponsePacket responsePacket = new ResponsePacket(
                ServerProtocol.PROGRAM_SEARCH,
                resDto.isSuccess(),
                resDto.getCode(),
                resDto.getMessage(),
                dataNode
            );

            sendMessage(JsonUtil.toJson(responsePacket));
        } catch (Exception e) {
            e.printStackTrace();
            ResponsePacket responsePacket = new ResponsePacket(
                ServerProtocol.PROGRAM_SEARCH,
                false,
                400,
                "프로그램 검색 요청 데이터 파싱 실패: " + e.getMessage(),
                null
            );
            sendMessage(JsonUtil.toJson(responsePacket));
        }
    }

    // 프로그램 검색 (구분자 처리)
    private void handleProgramSearch(String data) {
        try {
            String[] params = data.split(ServerProtocol.MODE2);
            String keyword = params.length > 0 ? params[0] : "";
            int limit = 10;
            if (params.length > 1) {
                try {
                    limit = Integer.parseInt(params[1]);
                } catch (NumberFormatException ignored) {}
            }

            ProgramSearchDto.Request reqDto = new ProgramSearchDto.Request(keyword, limit);
            ProgramSearchDto.Response resDto = utilService.searchPrograms(reqDto);

            if (resDto.isSuccess()) {
                sendMessage(ServerProtocol.SUCCESS + ServerProtocol.MODE1 + JsonUtil.toJson(resDto.getPrograms()));
            } else {
                sendMessage(ServerProtocol.FAIL + ServerProtocol.MODE1 + resDto.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(ServerProtocol.FAIL + ServerProtocol.MODE1 + "프로그램 검색 중 오류 발생");
        }
    }

    /**
     * 중복 로그인 감지 시 기존 세션(클라이언트)에 강제 로그아웃을 푸시한다.
     * 소켓을 서버에서 직접 닫지 않으며, 클라이언트가 데이터를 업로드한 후 스스로 연결을 종료한다.
     * SessionRegistry에서 호출된다.
     */
    public void forceLogout() {
        ResponsePacket push = new ResponsePacket(
            ServerProtocol.SESSION_FORCE_LOGOUT,
            true,
            200,
            "다른 기기에서 로그인이 감지되었습니다. 데이터를 업로드 후 로그아웃됩니다.",
            null
        );
        sendMessage(JsonUtil.toJson(push));
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
