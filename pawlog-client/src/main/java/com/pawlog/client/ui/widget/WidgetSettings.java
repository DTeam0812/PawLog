package com.pawlog.client.ui.widget;

import java.util.prefs.Preferences;

public final class WidgetSettings {
    private static final String ALWAYS_ANIMATED_KEY = "widgetAlwaysAnimated";
    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(WidgetSettings.class);

    private WidgetSettings() {
    }

    public static boolean isAlwaysAnimated() {
        return PREFERENCES.getBoolean(ALWAYS_ANIMATED_KEY, false);
    }

    public static void setAlwaysAnimated(boolean alwaysAnimated) {
        PREFERENCES.putBoolean(ALWAYS_ANIMATED_KEY, alwaysAnimated);
    }
}
