package com.pawlog.client.ui.widget;

import java.util.prefs.Preferences;
import java.util.function.IntConsumer;

public final class WidgetSettings {
    private static final String ALWAYS_ANIMATED_KEY = "widgetAlwaysAnimated";
    private static final String OPACITY_PERCENT_KEY = "widgetOpacityPercent";
    private static final String APP_OPACITY_PERCENT_KEY = "appOpacityPercent";
    private static final int MIN_OPACITY_PERCENT = 30;
    private static final int DEFAULT_OPACITY_PERCENT = 100;
    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(WidgetSettings.class);
    private static IntConsumer opacityChangeListener = value -> {};

    private WidgetSettings() {
    }

    public static boolean isAlwaysAnimated() {
        return PREFERENCES.getBoolean(ALWAYS_ANIMATED_KEY, false);
    }

    public static void setAlwaysAnimated(boolean alwaysAnimated) {
        PREFERENCES.putBoolean(ALWAYS_ANIMATED_KEY, alwaysAnimated);
    }

    public static int getOpacityPercent() {
        return clampOpacity(PREFERENCES.getInt(OPACITY_PERCENT_KEY, DEFAULT_OPACITY_PERCENT));
    }

    public static void setOpacityPercent(int opacityPercent) {
        int safeOpacity = clampOpacity(opacityPercent);
        PREFERENCES.putInt(OPACITY_PERCENT_KEY, safeOpacity);
        opacityChangeListener.accept(safeOpacity);
    }

    public static int getAppOpacityPercent() {
        return clampAppOpacity(PREFERENCES.getInt(APP_OPACITY_PERCENT_KEY, DEFAULT_OPACITY_PERCENT));
    }

    public static void setAppOpacityPercent(int opacityPercent) {
        PREFERENCES.putInt(APP_OPACITY_PERCENT_KEY, clampAppOpacity(opacityPercent));
    }

    public static void setOpacityChangeListener(IntConsumer listener) {
        opacityChangeListener = listener == null ? value -> {} : listener;
        opacityChangeListener.accept(getOpacityPercent());
    }

    private static int clampOpacity(int opacityPercent) {
        return Math.max(MIN_OPACITY_PERCENT, Math.min(100, opacityPercent));
    }

    private static int clampAppOpacity(int opacityPercent) {
        return Math.max(50, Math.min(100, opacityPercent));
    }
}
