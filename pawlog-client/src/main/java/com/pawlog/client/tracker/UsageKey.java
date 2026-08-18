package com.pawlog.client.tracker;

import java.time.LocalDate;

public record UsageKey(
    LocalDate usageDate,
    int hour,
    String programName
) {
}