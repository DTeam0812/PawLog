package com.pawlog.client.ui.widget;

import javax.swing.SwingUtilities;

public final class WidgetAlertService {
    private static Runnable startBarkHandler = () -> {};
    private static Runnable stopBarkHandler = () -> {};

    private WidgetAlertService() {
    }

    public static synchronized void setBarkHandlers(Runnable startHandler, Runnable stopHandler) {
        startBarkHandler = startHandler == null ? () -> {} : startHandler;
        stopBarkHandler = stopHandler == null ? () -> {} : stopHandler;
    }

    public static void startBarking() {
        Runnable handler;

        synchronized (WidgetAlertService.class) {
            handler = startBarkHandler;
        }

        SwingUtilities.invokeLater(handler);
    }

    public static void stopBarking() {
        Runnable handler;

        synchronized (WidgetAlertService.class) {
            handler = stopBarkHandler;
        }

        SwingUtilities.invokeLater(handler);
    }
}
