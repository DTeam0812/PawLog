package com.pawlog.client.network;
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
import com.pawlog.common.dto.usage.UsageLogSaveDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.pawlog.client.AuthStore.AuthStore;
import com.pawlog.client.tracker.UsageKey;
import com.pawlog.common.util.JsonUtil;
public class PawLogClient {
    private static final String DEFAULT_HOST = "192.168.0.13";//서버주소
    private static final int DEFAULT_PORT = 8005;
    private static final int CONNECT_TIMEOUT_MILLIS = 5000;
    private static final int READ_TIMEOUT_MILLIS = 5000;
    private static final String MODE1 = ":";
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private static final String MODE2 = ";";
    private static final String LOGIN = "LOGIN";
    private static final String MODE3 = "|";
    private static final String REGISTER = "REGISTER";
    private static final String SUCCESS = "SUCCESS";
    private static final String INIT = "INIT";
    private static final String DJOIN = "DJOIN";
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

    public Response login(String email, String password) throws Exception {
        AuthLoginDto.Request loginData = new AuthLoginDto.Request(email, password);
        JsonNode dataNode = JsonUtil.toJsonNode(loginData);
        RequestPacket requestPacket = new RequestPacket(LOGIN, dataNode);
        String requestJson = JsonUtil.toJson(requestPacket);
        return sendJson(requestJson);
    }
    public Response DateJoin(String id ,String startdate , String enddate) throws Exception{
        String data =  id + MODE2 + startdate + MODE2 + enddate;
        //여기서 JSON 변환 ?

        return send(DJOIN ,data);
    }



    public Response saveUsage(Map<UsageKey, Long> data) throws Exception {
        if (data == null || data.isEmpty()) {
            return new Response(false, "저장할 사용 기록이 없습니다.");
        }

        //map - > list변환 
        List<UsageLogSaveDto.LogItem> logs = new ArrayList<>();
        for(Map.Entry<UsageKey, Long> entry : data.entrySet()) {
            UsageKey key = entry.getKey();
            long seconds = entry.getValue();

            UsageLogSaveDto.LogItem logItem = new UsageLogSaveDto.LogItem(key.hour(), key.programName(), seconds);
            logs.add(logItem);
        }

        UsageLogSaveDto.Request request = new UsageLogSaveDto.Request(AuthStore.getInstance().getEmail(), LocalDate.now().toString(), logs);
        JsonNode dataNode = JsonUtil.toJsonNode(request);
        RequestPacket packet = new RequestPacket("USAGE_LOG_SAVE", dataNode);
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

    private synchronized Response send(String command, String data) throws Exception {
        connect();
        output.println(command + MODE1 + data);
        String line = input.readLine();

        if (line == null) {
            disconnect();
            return new Response(false, "서버 연결이 종료되었습니다.");
        }

        int delimiterIndex = line.indexOf(MODE1);
        String status = delimiterIndex < 0 ? line : line.substring(0, delimiterIndex);
        String message = delimiterIndex < 0 ? "" : line.substring(delimiterIndex + MODE1.length());
        return new Response(SUCCESS.equals(status), message);
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
