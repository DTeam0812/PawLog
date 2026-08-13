package com.pawlog.server.net;

import com.pawlog.common.dto.user.UserLoginDto;
import com.pawlog.common.dto.user.UserRegisterDto;
import com.pawlog.common.protocol.ServerProtocol;
import com.pawlog.server.service.UserService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Date;

public class ClientThread extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private UserService userService;

    public ClientThread(Socket socket) {
        try {
            this.socket = socket;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.userService = new UserService();
            System.out.println(socket + " 접속 성공.......");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        // 요청 분석 (Command : Data)
        String[] commandData = parseRoutine(line, ServerProtocol.MODE1);
        String command = commandData[0];
        String data = commandData[1];

        /*
        * 라우팅 영역
        * */
        if (ServerProtocol.REGISTER.equals(command)) {
            handleRegister(data);
        }
        //
        else if (ServerProtocol.LOGIN.equals(command)) {
            handleLogin(data);
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
            Date birthDate = Date.valueOf(params[4]);

            // Request DTO 생성
            UserRegisterDto.Request reqDto = new UserRegisterDto.Request(email, password, nickname, gender, birthDate);
            
            // Service 호출 및 Response DTO 수신
            UserRegisterDto.Response resDto = userService.register(reqDto);

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
            // data 포맷: email;password
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
            UserLoginDto.Response resDto = userService.login(reqDto);

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
