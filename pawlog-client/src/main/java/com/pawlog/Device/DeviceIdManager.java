package com.pawlog.Device;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class DeviceIdManager {
    private DeviceIdManager() {
    }

    public static String createDeviceId() {
        return UUID.randomUUID().toString();
    }

    private static Path getDeviceIdPath() {
        String localAppData = System.getenv("LOCALAPPDATA");
        return Path.of(localAppData, "PawLog", "device.id");
    }

    private static void saveDeviceId(String deviceId) throws IOException {
        Path path = getDeviceIdPath();
        Files.createDirectories(path.getParent());
        Files.writeString(path, deviceId, StandardCharsets.UTF_8);
    }

    private static String createAndSaveDeviceId() throws IOException {
        String deviceId = createDeviceId();
        saveDeviceId(deviceId);
        return deviceId;
    }

    public static String getDeviceId() {
        Path path = getDeviceIdPath();

        try {
            if (Files.exists(path)) {
                return Files.readString(path, StandardCharsets.UTF_8).trim();
            }

            return createAndSaveDeviceId();
        } catch (IOException exception) {
            throw new IllegalStateException(
                "Device ID를 불러오지 못했습니다.",
                exception
            );
        }
    }

    public static void main(String[] args) {
        String deviceId = DeviceIdManager.getDeviceId();
        System.out.println(deviceId);
    }
}