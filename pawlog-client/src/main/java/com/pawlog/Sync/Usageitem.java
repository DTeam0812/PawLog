package com.pawlog.Sync;

import java.time.LocalDate;

public record Usageitem(
    LocalDate usageDate,
    int hour,
    String processName,
    long durationSec
) {
}
