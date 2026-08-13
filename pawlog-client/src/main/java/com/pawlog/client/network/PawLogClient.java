package com.pawlog.client.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDate;

public class PawLogClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8005;
    private static final int CONNECT_TIMEOUT_MILLIS = 5000;
    private static final String MODE1 = ":";
    private static final String MODE2 = ";";
    private static final String LOGIN = "LOGIN";
    private static final String REGISTER = "REGISTER";
    private static final String SUCCESS = "SUCCESS";

    private final String host;
    private final int port;

    public PawLogClient() {
        this(System.getProperty("pawlog.server.host", DEFAULT_HOST), Integer.getInteger("pawlog.server.port", DEFAULT_PORT));
    }

    public PawLogClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Response login(String email, String password) throws Exception {
        String data = email + MODE2 + password;
        return send(LOGIN, data);
    }

    public Response register(String email, String password, String nickname, String gender, LocalDate birthDate) throws Exception {
        String data = email
            + MODE2 + password
            + MODE2 + nickname
            + MODE2 + gender
            + MODE2 + birthDate;

        return send(REGISTER, data);
    }

    private Response send(String command, String data) throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MILLIS);

            try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

                output.println(command + MODE1 + data);
                String line = input.readLine();

                if (line == null) {
                    return new Response(false, "서버 응답이 없습니다.");
                }

                int delimiterIndex = line.indexOf(MODE1);
                String status = delimiterIndex < 0 ? line : line.substring(0, delimiterIndex);
                String message = delimiterIndex < 0 ? "" : line.substring(delimiterIndex + MODE1.length());

                return new Response(SUCCESS.equals(status), message);
            }
        }
    }

    public record Response(boolean success, String message) {
    }
}
