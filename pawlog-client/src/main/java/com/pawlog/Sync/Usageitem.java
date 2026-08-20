package com.pawlog.Sync;

import java.time.LocalDate;

public record Usageitem(
    LocalDate usageDate,
    int hour,
    String programName,
    long durationSec
) {
}