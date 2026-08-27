package com.pawlog.client.network;

import java.io.BufferedReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.pawlog.common.protocol.ServerProtocol;
import com.pawlog.common.util.JsonUtil;

public class SocketResponseReader {//수신 전용 스레드 강제 로그아웃용
    private final BufferedReader input;
    private final BlockingQueue<String> responseQueue; // 응답 대기 큐 (사실상 그냥 통로)
    private volatile boolean running;
    private Thread readerThread;
    private final Consumer<String> forceLogoutHandler;
    public SocketResponseReader(BufferedReader input , Consumer<String> forceLogoutHandler) {
        this.input = input;
        this.forceLogoutHandler = forceLogoutHandler;
        this.responseQueue = new LinkedBlockingQueue<>();
    }

    public void start() {
        if (running) {
            return;
        }

        running = true;

        readerThread = new Thread(
            this::readLoop,
            "pawlog-socket-reader"
        );

        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void handleMessage(String responseJson) {
    try {
        JsonNode root = JsonUtil.fromJson(
            responseJson,
            JsonNode.class
        );

        String command = root.path("command").asText("");

        if (ServerProtocol.SESSION_FORCE_LOGOUT.equals(command)) {
            String message = root.path("message").asText(
                "다른 기기에서 로그인되어 로그아웃됩니다."
            );

            forceLogoutHandler.accept(message);
            return;
        }
        responseQueue.offer(responseJson);
    } catch (Exception exception) {
        responseQueue.offer(responseJson);
    }
}
    private void readLoop() {
        try {
            while (running) {
                String responseJson = input.readLine();

                if (responseJson == null) {
                    break;
                }

                handleMessage(responseJson);
            }
        } catch (Exception exception) {
            if (running) {
                exception.printStackTrace();
            }
        } finally {
            running = false;
        }
    }

    public String waitResponse() throws InterruptedException {
        return responseQueue.poll(5, TimeUnit.SECONDS);
    }

    public void stop() {
        running = false;

        if (readerThread != null) {
            readerThread.interrupt();
        }
    }
}