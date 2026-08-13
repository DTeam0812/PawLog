package com.pawlog.server.main;

import com.pawlog.server.net.ClientThread;

import java.net.ServerSocket;
import java.net.Socket;

public class PawLogServer {
    public static final int PORT = 8005;

    public PawLogServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("==========================================");
            System.out.println("   PawLog Server Started on Port: " + PORT);
            System.out.println("==========================================");

            while (true) {
                Socket socket = serverSocket.accept();
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
