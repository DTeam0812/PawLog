package com.pawlog.Sync;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.pawlog.Device.DeviceIdManager;
import com.pawlog.client.AuthStore.AuthStore;
import com.pawlog.client.tracker.UsageKey;

public class UsagebatchFactory {
    private UsagebatchFactory() {
    }
    
    public static Usagebatch create(Map<UsageKey, Long> data) {
        String deviceId = DeviceIdManager.getDeviceId();
        String batchId = UUID.randomUUID().toString();

        List<Usageitem> items = new ArrayList<>();

        for(Map.Entry<UsageKey, Long> entry : data.entrySet()){
            UsageKey key = entry.getKey();
            Long value = entry.getValue();

            Usageitem item = new Usageitem(key.usageDate() ,key.hour(), key.programName(), value);
            items.add(item);
        }
        System.out.println(items);
        return new Usagebatch(
            deviceId,
            batchId,
            AuthStore.getInstance().getEmail(),
            items
        );
    }
}