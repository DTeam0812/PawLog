package com.pawlog.client.main;

import javax.swing.SwingUtilities;

import com.pawlog.client.tracker.ProgramTracker;
import com.pawlog.client.ui.widget.Widget;

public class App {
    
    public static void main(String[] args) {
        ProgramTracker.startTracking();
        
        SwingUtilities.invokeLater(() -> {
            Widget widget = new Widget();
            widget.show();
        });
    }
}
