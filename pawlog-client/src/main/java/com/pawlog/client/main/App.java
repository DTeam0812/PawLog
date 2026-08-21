package com.pawlog.client.main;

import java.sql.SQLException;

import javax.swing.SwingUtilities;

import com.pawlog.client.Storage.SQLiteInitializer;
import com.pawlog.client.tracker.ProgramTracker;
import com.pawlog.client.ui.widget.Widget;

public class App {
    
    public static void main(String[] args) {
        try {
        SQLiteInitializer.initialize();
    } catch (SQLException exception) {
        System.out.println("SQLite 초기화 실패");
        exception.printStackTrace();
        return;
    }

        SwingUtilities.invokeLater(() -> {
            Widget widget = new Widget();
            widget.show();
        });
    }
}
