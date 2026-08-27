package com.pawlog.server.main;

import com.pawlog.server.db.DatabaseConfig;
import com.pawlog.server.net.ClientThread;
import com.pawlog.server.net.DailyStatsScheduler;
import com.pawlog.server.net.SummaryScheduler;

import java.net.ServerSocket;
import java.net.Socket;

public class PawLogServer {
    public static final int PORT = 8005;

    public PawLogServer() {
        DatabaseConfig.initDatabase();

        // daily_usage_summary 5분 주기 스케줄러
        SummaryScheduler.getInstance().start();
        // program_usage_stats 매일 자정 배치 스케줄러
        DailyStatsScheduler.getInstance().start();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("==========================================");
            System.out.println("   PawLog Server Started on Port: " + PORT);
            System.out.println("==========================================");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("접속 성공");
                ClientThread clientThread = new ClientThread(socket);
                clientThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new PawLogServer();
    }
}
