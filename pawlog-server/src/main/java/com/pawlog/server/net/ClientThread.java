package com.pawlog.server.net;

import com.fasterxml.jackson.databind.JsonNode;
import com.pawlog.common.dto.analytics.ProgramAnalyticsDto;
import com.pawlog.common.dto.auth.AuthLoginDto;
import com.pawlog.common.dto.auth.AuthRegisterDto;
import com.pawlog.common.dto.auth.AuthUpdateDto;
import com.pawlog.common.dto.board.*;
import com.pawlog.common.dto.ranking.PopularRankingDto;
import com.pawlog.common.dto.recommend.RecommendProgramDto;
import com.pawlog.common.dto.statistics.MyStatisticsDto;
import com.pawlog.common.dto.statistics.TodayStatisticsDto;
import com.pawlog.common.dto.usage.UsageLogSaveDto;
import com.pawlog.common.protocol.RequestPacket;
import com.pawlog.common.protocol.ResponsePacket;
import com.pawlog.common.protocol.ServerProtocol;
import com.pawlog.common.util.JsonUtil;
import com.pawlog.server.entity.UserEntity;
import com.pawlog.server.service.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientThread extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private AuthService authService;
    private PopularRankingService popularRankingService;
    private UsageLogService usageLogService;
    private StatisticsService statisticsService;
    private UtilService utilService;
    private PostService postService;
    private CommentService commentService;
    private ProgramAnalyticsService programAnalyticsService;
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
            this.postService = new PostService();
            this.commentService = new CommentService();
            this.programAnalyticsService = new ProgramAnalyticsService();
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

    // [Controller 역할] - JSON 패킷 전용 라우팅
    private void routine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return;
        }

        try {
            RequestPacket packet = JsonUtil.fromJson(line, RequestPacket.class);
            if (packet == null || packet.getCommand() == null) {
                ResponsePacket errorRes = new ResponsePacket("UNKNOWN", false, 400, "잘못된 요청 형식입니다. command가 필요합니다.", null);
                sendMessage(JsonUtil.toJson(errorRes));
                return;
            }

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
            else if (ServerProtocol.MY_STATISTICS_GET.equals(command)) {
                handleMyStatisticsJson(dataJson);
            }
            else if (ServerProtocol.TODAY_STATISTICS_GET.equals(command)) {
                handleTodayStatisticsJson(dataJson);
            }
            else if (ServerProtocol.PROGRAM_SEARCH.equals(command)) {
                handleProgramSearchJson(dataJson);
            }
            else if (ServerProtocol.POST_LIST_GET.equals(command)) {
                handlePostListGetJson(dataJson);
            }
            else if (ServerProtocol.POST_LIKE_TOGGLE.equals(command)) {
                handlePostLikeToggleJson(dataJson);
            }
            else if (ServerProtocol.POST_DETAIL_GET.equals(command)) {
                handlePostDetailGetJson(dataJson);
            }
            else if (ServerProtocol.POST_ENROLL.equals(command)) {
                handlePostEnrollJson(dataJson);
            }
            else if (ServerProtocol.COMMENT_CREATE.equals(command)) {
                handleCommentCreateJson(dataJson);
            }
            else if (ServerProtocol.PROGRAM_ANALYTICS_GET.equals(command)) {
                handleProgramAnalyticsJson(dataJson);
            }
            else if (ServerProtocol.RECENT_POPULAR_PROGRAM_GET.equals(command)) {
                handleRecommendProgramJson(dataJson);
            }
            else {
                ResponsePacket errorRes = new ResponsePacket(command, false, 400, "지원하지 않는 커맨드입니다: " + command, null);
                sendMessage(JsonUtil.toJson(errorRes));
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponsePacket errorRes = new ResponsePacket("UNKNOWN", false, 400, "잘못된 JSON 요청 형식입니다: " + e.getMessage(), null);
            sendMessage(JsonUtil.toJson(errorRes));
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

    // 오늘 통계 조회 (JSON 처리)
    private void handleTodayStatisticsJson(String dataJson) {
        try {
            TodayStatisticsDto.Request reqDto = JsonUtil.fromJson(dataJson, TodayStatisticsDto.Request.class);
            TodayStatisticsDto.Response resDto = statisticsService.getTodayStatistics(reqDto);

            ResponsePacket responsePacket = new ResponsePacket(
                ServerProtocol.TODAY_STATISTICS_GET,
                resDto.isSuccess(),
                resDto.getCode(),
                resDto.getMessage(),
                resDto.isSuccess() && resDto.getData() != null ? JsonUtil.toJsonNode(resDto.getData()) : null
            );
            sendMessage(JsonUtil.toJson(responsePacket));
        } catch (Exception e) {
            e.printStackTrace();
            ResponsePacket responsePacket = new ResponsePacket(
                ServerProtocol.TODAY_STATISTICS_GET,
                false,
                400,
                "오늘 통계 요청 데이터 파싱 실패: " + e.getMessage(),
                null
            );
            sendMessage(JsonUtil.toJson(responsePacket));
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

    // 게시글 목록 조회 (JSON 처리)
    private void handlePostListGetJson(String dataJson) {
        try {
            PostListDto.Request reqDto = JsonUtil.fromJson(dataJson, PostListDto.Request.class);
            PostListDto.Response resDto = postService.getPostList(reqDto, loggedInUserId);

            JsonNode dataNode = null;
            if (resDto.isSuccess() && resDto.getData() != null) {
                dataNode = JsonUtil.toJsonNode(resDto.getData());
            }

            ResponsePacket responsePacket = new ResponsePacket(
                ServerProtocol.POST_LIST_GET,
                resDto.isSuccess(),
                resDto.getCode(),
                resDto.getMessage(),
                dataNode
            );

            sendMessage(JsonUtil.toJson(responsePacket));
        } catch (Exception e) {
            e.printStackTrace();
            ResponsePacket responsePacket = new ResponsePacket(
                ServerProtocol.POST_LIST_GET,
                false,
                400,
                "게시글 목록 조회 요청 데이터 파싱 실패: " + e.getMessage(),
                null
            );
            sendMessage(JsonUtil.toJson(responsePacket));
        }
    }

    // 게시글 좋아요 토글 (JSON 처리)
    private void handlePostLikeToggleJson(String dataJson) {
        try {
            // 1. 비로그인 세션 차단 (401 방어)
            if (!isLoggedIn()) {
                ResponsePacket errorPacket = new ResponsePacket(
                        ServerProtocol.POST_LIKE_TOGGLE,
                        false,
                        401,
                        "로그인이 필요한 서비스입니다.",
                        null
                );
                sendMessage(JsonUtil.toJson(errorPacket));
                return;
            }

            // 2. DTO 파싱 및 Service 호출
            PostLikeDto.Request reqDto = JsonUtil.fromJson(dataJson, PostLikeDto.Request.class);
            PostLikeDto.Response resDto = postService.togglePostLike(reqDto.getPostId(), getLoggedInUserId());

            // 3. 응답 패킷 전송
            JsonNode dataNode = (resDto.isSuccess() && resDto.getData() != null)
                    ? JsonUtil.toJsonNode(resDto.getData())
                    : null;

            ResponsePacket responsePacket = new ResponsePacket(
                    ServerProtocol.POST_LIKE_TOGGLE,
                    resDto.isSuccess(),
                    resDto.getCode(),
                    resDto.getMessage(),
                    dataNode
            );
            sendMessage(JsonUtil.toJson(responsePacket));

        } catch (Exception e) {
            e.printStackTrace();
            ResponsePacket errorPacket = new ResponsePacket(
                    ServerProtocol.POST_LIKE_TOGGLE,
                    false,
                    400,
                    "게시글 좋아요 요청 파싱 실패: " + e.getMessage(),
                    null
            );
            sendMessage(JsonUtil.toJson(errorPacket));
        }
    }

    // 게시글 상세 조회 (JSON 처리)
    private void handlePostDetailGetJson(String dataJson) {
        try {
            PostDetailDto.Request reqDto = JsonUtil.fromJson(dataJson, PostDetailDto.Request.class);
            PostDetailDto.Response resDto = postService.getPostDetail(reqDto, loggedInUserId);

            JsonNode dataNode = null;
            if (resDto.isSuccess() && resDto.getData() != null) {
                dataNode = JsonUtil.toJsonNode(resDto.getData());
            }

            ResponsePacket responsePacket = new ResponsePacket(
                ServerProtocol.POST_DETAIL_GET,
                resDto.isSuccess(),
                resDto.getCode(),
                resDto.getMessage(),
                dataNode
            );

            sendMessage(JsonUtil.toJson(responsePacket));
        } catch (Exception e) {
            e.printStackTrace();
            ResponsePacket responsePacket = new ResponsePacket(
                ServerProtocol.POST_DETAIL_GET,
                false,
                400,
                "게시글 상세 조회 요청 데이터 파싱 실패: " + e.getMessage(),
                null
            );
            sendMessage(JsonUtil.toJson(responsePacket));
        }
    }

    // 게시글 등록 (JSON 처리)
    private void handlePostEnrollJson(String dataJson) {
        try {
            PostEnrollDto.Request reqDto = JsonUtil.fromJson(dataJson, PostEnrollDto.Request.class);
            PostEnrollDto.Response resDto = postService.enrollPost(reqDto, loggedInUserId);

            JsonNode dataNode = null;
            if (resDto.isSuccess() && resDto.getData() != null) {
                dataNode = JsonUtil.toJsonNode(resDto.getData());
            }

            ResponsePacket responsePacket = new ResponsePacket(
                ServerProtocol.POST_ENROLL,
                resDto.isSuccess(),
                resDto.getCode(),
                resDto.getMessage(),
                dataNode
            );

            sendMessage(JsonUtil.toJson(responsePacket));
        } catch (Exception e) {
            e.printStackTrace();
            ResponsePacket responsePacket = new ResponsePacket(
                ServerProtocol.POST_ENROLL,
                false,
                400,
                "게시글 등록 요청 데이터 파싱 실패: " + e.getMessage(),
                null
            );
            sendMessage(JsonUtil.toJson(responsePacket));
        }
    }

    // 댓글 작성 (JSON 처리)
    private void handleCommentCreateJson(String dataJson) {
        try {
            // 1. 비로그인 세션 차단 (401 방어)
            if (!isLoggedIn()) {
                ResponsePacket errorPacket = new ResponsePacket(
                        ServerProtocol.COMMENT_CREATE,
                        false,
                        401,
                        "로그인이 필요한 서비스입니다.",
                        null
                );
                sendMessage(JsonUtil.toJson(errorPacket));
                return;
            }

            // 2. DTO 파싱 및 Service 호출
            CommentDto.Request reqDto = JsonUtil.fromJson(dataJson, CommentDto.Request.class);
            CommentDto.Response resDto = commentService.writeComment(reqDto, getLoggedInUserId());

            // 3. 응답 패킷 생성 및 전송
            JsonNode dataNode = (resDto.isSuccess() && resDto.getData() != null)
                    ? JsonUtil.toJsonNode(resDto.getData())
                    : null;

            ResponsePacket responsePacket = new ResponsePacket(
                    ServerProtocol.COMMENT_CREATE,
                    resDto.isSuccess(),
                    resDto.getCode(),
                    resDto.getMessage(),
                    dataNode
            );
            sendMessage(JsonUtil.toJson(responsePacket));

        } catch (Exception e) {
            e.printStackTrace();
            ResponsePacket errorPacket = new ResponsePacket(
                    ServerProtocol.COMMENT_CREATE,
                    false,
                    400,
                    "댓글 작성 요청 데이터 파싱 실패: " + e.getMessage(),
                    null
            );
            sendMessage(JsonUtil.toJson(errorPacket));
        }
    }

    // 프로그램 분석 상세 조회 (JSON 처리)
    private void handleProgramAnalyticsJson(String dataJson) {
        try {
            ProgramAnalyticsDto.Request reqDto = JsonUtil.fromJson(dataJson, ProgramAnalyticsDto.Request.class);
            ProgramAnalyticsDto.Response resDto = programAnalyticsService.getProgramAnalytics(reqDto);

            JsonNode dataNode = (resDto.isSuccess() && resDto.getData() != null)
                    ? JsonUtil.toJsonNode(resDto.getData())
                    : null;

            ResponsePacket responsePacket = new ResponsePacket(
                    ServerProtocol.PROGRAM_ANALYTICS_GET,
                    resDto.isSuccess(),
                    resDto.getCode(),
                    resDto.getMessage(),
                    dataNode
            );

            sendMessage(JsonUtil.toJson(responsePacket));
        } catch (Exception e) {
            e.printStackTrace();
            ResponsePacket responsePacket = new ResponsePacket(
                    ServerProtocol.PROGRAM_ANALYTICS_GET,
                    false,
                    400,
                    "프로그램 분석 요청 데이터 파싱 실패: " + e.getMessage(),
                    null
            );
            sendMessage(JsonUtil.toJson(responsePacket));
        }
    }

    // 추천 프로그램 조회 (JSON 처리)
    private void handleRecommendProgramJson(String dataJson) {
        try {
            RecommendProgramDto.Request reqDto = JsonUtil.fromJson(dataJson, RecommendProgramDto.Request.class);
            RecommendProgramDto.Response resDto = popularRankingService.getRecommendPrograms(reqDto);

            ResponsePacket responsePacket = new ResponsePacket(
                ServerProtocol.RECOMMEND_PROGRAM_GET,
                resDto.isSuccess(),
                resDto.getCode(),
                resDto.getMessage(),
                resDto.isSuccess() && resDto.getData() != null ? JsonUtil.toJsonNode(resDto.getData()) : null
            );
            sendMessage(JsonUtil.toJson(responsePacket));
        } catch (Exception e) {
            e.printStackTrace();
            ResponsePacket responsePacket = new ResponsePacket(
                ServerProtocol.RECOMMEND_PROGRAM_GET,
                false,
                400,
                "추천 프로그램 요청 데이터 파싱 실패: " + e.getMessage(),
                null
            );
            sendMessage(JsonUtil.toJson(responsePacket));
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
