package com.devicemonitor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import androidx.core.app.NotificationCompat;

public class NotificationCaptureService extends NotificationListenerService {
    private static final String CHANNEL_ID = "monitor_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DeviceMonitor")
            .setContentText("Monitoring notifications...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build();
        startForeground(1, notification);
        return START_STICKY;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName();
        Notification notif = sbn.getNotification();
        String title = "";
        String text = "";

        if (notif.extras != null) {
            Bundle extras = notif.extras;
            title = extras.getString(Notification.EXTRA_TITLE, "");
            text = extras.getString(Notification.EXTRA_TEXT, "");
        }

        Intent intent = new Intent("com.devicemonitor.NOTIFICATION_CAPTURED");
        intent.putExtra("package", pkg);
        intent.putExtra("title", title);
        intent.putExtra("text", text);
        sendBroadcast(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Notification dismissed
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Monitor Service", NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }
}
