package com.pawlog.Sync;

import java.util.List;

import com.pawlog.Device.DeviceIdManager;

public record Usagebatch(
    String deviceId,
    String batchId,
    String id,
    List<Usageitem> logs
) {
    
   
}

