package com.pawlog.client.network;
import com.pawlog.common.dto.board.*;
import com.pawlog.common.protocol.RequestPacket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.pawlog.common.dto.auth.AuthLoginDto;
import com.pawlog.common.dto.auth.AuthRegisterDto;
import com.pawlog.common.dto.statistics.MyStatisticsDto;
import com.pawlog.common.dto.ranking.PopularRankingDto;
import com.pawlog.common.dto.usage.MyInfoDto;
import com.pawlog.common.dto.usage.UsageLogSaveDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.pawlog.Sync.Usagebatch;
import com.pawlog.Sync.Usageitem;
import com.pawlog.client.AuthStore.AuthStore;
import com.pawlog.client.tracker.UsageKey;
import com.pawlog.common.util.JsonUtil;
import com.pawlog.common.protocol.ServerProtocol;
public class PawLogClient {
    private static final int CliVersion = 1; //클라이언트 버전관리
    private static final String DEFAULT_HOST = "127.0.0.1";//서버주소
    private static final int DEFAULT_PORT = 8005;//활성 포트
    private static final int CONNECT_TIMEOUT_MILLIS = 5000;
    private static final int READ_TIMEOUT_MILLIS = 5000;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private static final String LOGIN = "LOGIN";
    private static final String NoticeBoardDto = "NoticeBoardDto";
    private static final String REGISTER = "REGISTER";
    private static final String POPULAR_RANKING_GET ="POPULAR_RANKING_GET";
    private static final String TODAY_STATISTICS_GET = "TODAY_STATISTICS_GET";
    private static final PawLogClient instance = new PawLogClient();
    private final String host;
    private final int port;

    public PawLogClient() {
        this(System.getProperty("pawlog.server.host", DEFAULT_HOST), Integer.getInteger("pawlog.server.port", DEFAULT_PORT));
    }

    public PawLogClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static PawLogClient getInstance() {
        return instance;
    }

    private synchronized void connect() throws Exception {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            return;
        }

        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MILLIS);
        socket.setSoTimeout(READ_TIMEOUT_MILLIS);

        input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        output = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
    }
    //======================================================================================================================//
    private synchronized ProgramSearchDto.Response sendProgramSearchJson(String requestJson) throws Exception {
        connect();
        output.println(requestJson);
        String responseJson = input.readLine();

        if (responseJson == null) {
            disconnect();
            return new ProgramSearchDto.Response(false, 500, "서버 연결이 종료되었습니다.", List.of());
        }

        JsonNode root = JsonUtil.fromJson(responseJson, JsonNode.class);
        boolean success = root.path("success").asBoolean();
        int code = root.path("code").asInt(success ? 200 : 400);
        String message = root.path("message").asText("");

        if (!success) {
            return new ProgramSearchDto.Response(false, code, message, List.of());
        }

        JsonNode dataNode = root.path("data");

        if (!dataNode.isArray()) {
            return new ProgramSearchDto.Response(false, 500, "프로그램 검색 응답 형식이 올바르지 않습니다.", List.of());
        }

        List<ProgramSearchDto.ProgramItem> programs = new ArrayList<>();

        for(JsonNode itemNode : dataNode) {
            ProgramSearchDto.ProgramItem program = JsonUtil.fromJson(
                itemNode.toString(),
                ProgramSearchDto.ProgramItem.class
            );

            programs.add(program);
        }

        return new ProgramSearchDto.Response(true, code, message, programs);
    }

    public ProgramSearchDto.Response searchPrograms(String keyword, int limit) throws Exception {
        ProgramSearchDto.Request request = new ProgramSearchDto.Request(keyword, limit);
        JsonNode dataNode = JsonUtil.toJsonNode(request);
        RequestPacket requestPacket = new RequestPacket(ServerProtocol.PROGRAM_SEARCH, dataNode);
        String requestJson = JsonUtil.toJson(requestPacket);
        return sendProgramSearchJson(requestJson);
    }

    // 서버가 반환한 게시글 목록 응답 JSON을 PostListDto.Response으로 변환한다.
    private synchronized PostListDto.Response sendPostListJson(String requestJson) throws Exception {
        try {
            connect();
            output.println(requestJson);
            String responseJson = input.readLine();

            if(responseJson == null) {
                disconnect();
                return new PostListDto.Response(false, 500, "서버 연결이 종료되었습니다.", null);
            }

            JsonNode root = JsonUtil.fromJson(responseJson, JsonNode.class);
            boolean success = root.path("success").asBoolean();
            int code = root.path("code").asInt(success ? 200 : 400);
            String message = root.path("message").asText("");

            if(!success) {
                return new PostListDto.Response(false, code, message, null);
            }

            JsonNode dataNode = root.path("data");
            if(dataNode.isMissingNode() || dataNode.isNull()) {
                return new PostListDto.Response(true, code, message, null);
            }

            PostListDto.PageData pageData = JsonUtil.fromJson(
                dataNode.toString(),
                PostListDto.PageData.class
            );

            return new PostListDto.Response(true, code, message, pageData);
        } catch(Exception exception) {
            disconnect();
            throw exception;
        }
    }

    // 프로그램 ID와 페이지 정보를 묶어 게시글 목록을 서버에 요청한다.
    public PostListDto.Response getPostList(long programId, int page, int pageSize) throws Exception {
        PostListDto.Request request = new PostListDto.Request(
            programId,
            page,
            pageSize,
            "LATEST",
            ""
        );
        JsonNode dataNode = JsonUtil.toJsonNode(request);
        RequestPacket requestPacket = new RequestPacket(ServerProtocol.POST_LIST_GET, dataNode);
        String requestJson = JsonUtil.toJson(requestPacket);
        return sendPostListJson(requestJson);
    }

    private synchronized AuthLoginDto.Response sendLoginJson(String requestJson) throws Exception {
        connect();
        output.println(requestJson);
        String responseJson = input.readLine();

        if (responseJson == null) {
            disconnect();
            return new AuthLoginDto.Response(false, 500, "서버 연결이 종료되었습니다.");
        }

        JsonNode root = JsonUtil.fromJson(responseJson, JsonNode.class);
        boolean success = root.path("success").asBoolean();
        int code = root.path("code").asInt(success ? 200 : 400);
        String message = root.path("message").asText("");
        JsonNode dataNode = root.path("data");

        if (!success || dataNode.isMissingNode() || dataNode.isNull()) {
            return new AuthLoginDto.Response(success, code, message);
        }

        AuthLoginDto.UserInfo user = JsonUtil.fromJson(
            dataNode.toString(),
            AuthLoginDto.UserInfo.class
        );

        return new AuthLoginDto.Response(true, code, message, user);
    }

    public AuthLoginDto.Response login(String email, String password) throws Exception {
        AuthLoginDto.Request loginData = new AuthLoginDto.Request(email, password);
        JsonNode dataNode = JsonUtil.toJsonNode(loginData);
        RequestPacket requestPacket = new RequestPacket(LOGIN, dataNode);
        String requestJson = JsonUtil.toJson(requestPacket);
        return sendLoginJson(requestJson);
    }

    // AUTHOR TITLE CATEGORY CONTENT SEND
    //ID AUTHOR TITLE CATEGORY CONTENT POST_LIKE COMMENT REUTRN  
   /*  private synchronized Enroll_PostDto.Response Enroll_Post(String Author , String Title , String Category , String Content){
`   
    }*/
    private synchronized MyInfoDto.Response sendMyInfoJson(String requestJson) throws Exception {
        connect();
        output.println(requestJson);
        String responseJson = input.readLine();

        if (responseJson == null) {
            disconnect();
            return new MyInfoDto.Response(false, 500, "서버 연결이 종료되었습니다.", null);
        }

        JsonNode root = JsonUtil.fromJson(responseJson, JsonNode.class);
        boolean success = root.path("success").asBoolean();
        int code = root.path("code").asInt(success ? 200 : 400);
        String message = root.path("message").asText("");

        if (!success) {
            return new MyInfoDto.Response(false, code, message, null);
        }

        JsonNode dataNode = root.path("data");
        if (dataNode.isMissingNode() || dataNode.isNull()) {
            return new MyInfoDto.Response(true, code, message, null);
        }

        MyInfoDto.TodayInfo data = JsonUtil.fromJson(
            dataNode.toString(),
            MyInfoDto.TodayInfo.class
        );

        return new MyInfoDto.Response(true, code, message, data);
    }

    public MyInfoDto.Response getTodayInfo(String email, LocalDate usageDate) throws Exception {
        MyInfoDto.Request request = new MyInfoDto.Request(email, usageDate.toString());
        JsonNode dataNode = JsonUtil.toJsonNode(request);
        RequestPacket requestPacket = new RequestPacket(TODAY_STATISTICS_GET, dataNode);
        String requestJson = JsonUtil.toJson(requestPacket);
        return sendMyInfoJson(requestJson);
    }
    private synchronized PopularRankingDto.Response sendPopularRankingJson(String requestJson) throws Exception {
        connect();
        output.println(requestJson);
        String responseJson = input.readLine();

        if (responseJson == null) {
            disconnect();
            return new PopularRankingDto.Response(false,500, "서버 연결이 종료되었습니다.", List.of());
        }

        JsonNode root = JsonUtil.fromJson(responseJson, JsonNode.class);
        boolean success = root.path("success").asBoolean();
        int code = root.path("code").asInt(success ? 200 : 400);
        String message = root.path("message").asText("");

        if (!success) {
            return new PopularRankingDto.Response(false, code, message, List.of());
        }

        JsonNode dataNode = root.path("data");
        if (dataNode.isMissingNode() || dataNode.isNull()) {
            return new PopularRankingDto.Response(true, code, message, List.of());
        }

        List<PopularRankingDto.RankingItem> rankings = new ArrayList<>();

        for(JsonNode itemNode : dataNode) {
            PopularRankingDto.RankingItem item = JsonUtil.fromJson(
                itemNode.toString(),
                PopularRankingDto.RankingItem.class
            );

            rankings.add(item);
        }

        return new PopularRankingDto.Response(true, code, message, rankings);
    }

    public PopularRankingDto.Response getPopularRankings(LocalDate startDate, LocalDate endDate, String gender, String ageGroup) throws Exception {
        PopularRankingDto.Request rankingData = new PopularRankingDto.Request(ageGroup, gender, startDate, endDate);
        JsonNode dataNode = JsonUtil.toJsonNode(rankingData);
        RequestPacket requestPacket = new RequestPacket(POPULAR_RANKING_GET, dataNode);
        String requestJson = JsonUtil.toJson(requestPacket);
        return sendPopularRankingJson(requestJson);
    }
    
    private synchronized MyStatisticsDto.Response sendMyStatisticsJson(String requestJson) throws Exception {
        connect();
        output.println(requestJson);
        String responseJson = input.readLine();
        if (responseJson == null) {
            disconnect();
            return new MyStatisticsDto.Response(false, 500, "서버 연결이 종료되었습니다.", null);
        }
        JsonNode root = JsonUtil.fromJson(responseJson, JsonNode.class);
        boolean success = root.path("success").asBoolean();
        int code = root.path("code").asInt(success ? 200 : 400);
        String message = root.path("message").asText("");
        if (!success) {
            return new MyStatisticsDto.Response(false, code, message, null);
        }

        JsonNode dataNode = root.path("data");
        if (dataNode.isMissingNode() || dataNode.isNull()) {
            return new MyStatisticsDto.Response(true, code, message, null);
        }

        MyStatisticsDto.DataPayload data = JsonUtil.fromJson(
            dataNode.toString(),
            MyStatisticsDto.DataPayload.class
        );

        return new MyStatisticsDto.Response(true, code, message, data);
    }
    public synchronized MyStatisticsDto.Response getMyStatistics(String id, LocalDate startDate, LocalDate endDate) throws Exception {
        MyStatisticsDto.Request request = new MyStatisticsDto.Request(id, startDate, endDate);
        JsonNode dataNode = JsonUtil.toJsonNode(request);
        RequestPacket requestPacket = new RequestPacket(ServerProtocol.MY_STATISTICS_GET, dataNode);
        String requestJson = JsonUtil.toJson(requestPacket);

        return sendMyStatisticsJson(requestJson);
    }
    
    public synchronized Response createPost(long programId, String title, String content) throws Exception{
        PostEnrollDto.Request request = new PostEnrollDto.Request(programId,title,content);
        JsonNode dataNode = JsonUtil.toJsonNode(request);
        RequestPacket requestPacket = new RequestPacket(ServerProtocol.POST_ENROLL,dataNode );
        String requestJson = JsonUtil.toJson(requestPacket);
        System.out.println(requestJson);
        return sendJson(requestJson);

    }//게시글 만들기


    private synchronized PostLikeDto.Response sendPostLikeToggleJson(String requestJson) throws Exception {
        connect();
        output.println(requestJson);
        String responseJson = input.readLine();

        if (responseJson == null) {
            disconnect();
            return new PostLikeDto.Response(false, 500, "서버 연결이 종료되었습니다.", null);
        }

        JsonNode root = JsonUtil.fromJson(responseJson, JsonNode.class);
        boolean success = root.path("success").asBoolean();
        int code = root.path("code").asInt(success ? 200 : 400);
        String message = root.path("message").asText("");
        JsonNode dataNode = root.path("data");

        PostLikeDto.LikeData likeData = null;
        if (success && dataNode != null && !dataNode.isMissingNode() && !dataNode.isNull()) {
            likeData = JsonUtil.fromJson(dataNode.toString(), PostLikeDto.LikeData.class);
        }

        return new PostLikeDto.Response(success, code, message, likeData);
    }

    public synchronized PostLikeDto.Response togglePostLike(long postId) throws Exception {
        PostLikeDto.Request request = new PostLikeDto.Request(postId);
        JsonNode dataNode = JsonUtil.toJsonNode(request);
        RequestPacket requestPacket = new RequestPacket(ServerProtocol.POST_LIKE_TOGGLE, dataNode);
        String requestJson = JsonUtil.toJson(requestPacket);
        return sendPostLikeToggleJson(requestJson);
    }

    public synchronized PostDetailDto.Response sendJoinPost(String requestJson) throws Exception{
        connect();
        output.println(requestJson);
        String responseJson = input.readLine();
        if (responseJson == null) {
            disconnect();
            return new PostDetailDto.Response(false, 500, "서버 연결이 종료되었습니다.", null);
        }

        JsonNode root = JsonUtil.fromJson(responseJson, JsonNode.class);
        boolean success = root.path("success").asBoolean();
        int code = root.path("code").asInt(success ? 200 : 400);
        String message = root.path("message").asText("");
        JsonNode dataNode = root.path("data");
        PostDetailDto.PostDetailData data = null;

        if(!dataNode.isMissingNode() && !dataNode.isNull()) {
            data = JsonUtil.fromJson(
                dataNode.toString(),
                PostDetailDto.PostDetailData.class
            );
        }

        return new PostDetailDto.Response(success, code, message, data);
    }

    public synchronized PostDetailDto.Response joinPost(long postId) throws Exception{
        PostDetailDto.Request request = new PostDetailDto.Request(postId);
        JsonNode dataNode = JsonUtil.toJsonNode(request);
        RequestPacket requestPacket = new RequestPacket(ServerProtocol.POST_DETAIL_GET, dataNode);
        String requestJson = JsonUtil.toJson(requestPacket);
        return sendJoinPost(requestJson);
    }


   /*  public Response saveUsage(Map<UsageKey, Long> data) throws Exception {
        if (data == null || data.isEmpty()) {
            return new Response(false, "저장할 사용 기록이 없습니다.");
        }
        //map - > list변환 
        List<UsageLogSaveDto.LogItem> logs = new ArrayList<>();
        for(Map.Entry<UsageKey, Long> entry : data.entrySet()) {
            UsageKey key = entry.getKey();
            long seconds = entry.getValue();

            UsageLogSaveDto.LogItem logItem = new UsageLogSaveDto.LogItem(key.hour(), key.processName(), seconds, key.usageDate().toString());
            logs.add(logItem);
        }   

        UsageLogSaveDto.Request request = new UsageLogSaveDto.Request(AuthStore.getInstance().getEmail(), LocalDate.now().toString(), logs);
        JsonNode dataNode = JsonUtil.toJsonNode(request);
        RequestPacket packet = new RequestPacket("USAGE_LOG_SAVE", dataNode);
        String requestJson = JsonUtil.toJson(packet);
        System.out.println(requestJson);
        return sendJson(requestJson);
    }
*/

    public  Response saveUsage(Usagebatch batch) throws Exception{
        if (batch == null) {
        return new Response(false, "전송할 배치가 없습니다.");
    }

        if (batch.logs() == null || batch.logs().isEmpty()) {
            return new Response(false, "전송할 사용 기록이 없습니다.");
    }

        if (batch.email() == null || batch.email().isBlank()) {
            return new Response(false, "사용자 이메일이 없습니다.");
    }
     List<UsageLogSaveDto.LogItem> logs = new ArrayList<>();

     for(Usageitem item : batch.logs()) {
        UsageLogSaveDto.LogItem logItem =
            new UsageLogSaveDto.LogItem(
                item.hour(),
                item.processName(),
                item.durationSec(),
                item.usageDate().toString()
            );
        logs.add(logItem);
    }
    String usageDate = batch.logs().get(0).usageDate().toString();

    UsageLogSaveDto.Request request = new UsageLogSaveDto.Request(
            batch.email(),
            usageDate,
            logs
        );
        JsonNode dataNode = JsonUtil.toJsonNode(request);
        RequestPacket packet =
            new RequestPacket("USAGE_LOG_SAVE", dataNode);

        String requestJson = JsonUtil.toJson(packet);

        return sendJson(requestJson);
    }
    public Response register(String email, String password, String nickname, String gender, LocalDate birthDate) throws Exception {
        AuthRegisterDto.Request registerData = new AuthRegisterDto.Request(nickname, email, password, birthDate.toString(), gender);
        JsonNode dataNode = JsonUtil.toJsonNode(registerData);
        RequestPacket requestPacket = new RequestPacket(REGISTER, dataNode);
        String requestJson = JsonUtil.toJson(requestPacket);
        return sendJson(requestJson);
    }
    
   
    private synchronized Response sendJson(String requestJson) throws Exception {
        connect();
        output.println(requestJson);
        String responseJson = input.readLine();

        if (responseJson == null) {
            disconnect();
            return new Response(false, "서버 연결이 종료되었습니다.");
        }

        JsonNode root = JsonUtil.fromJson(responseJson, JsonNode.class);
        boolean success = root.path("success").asBoolean();
        String message = root.path("message").asText("");
        return new Response(success, message);
    }

    public synchronized void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        socket = null;
        input = null;
        output = null;
    }

    public record Response(boolean success, String message) {
    }
}
