// DataCollector.java
package com.yourorg.pentest.monitor;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;

public class DataCollector {

    private static final String TAG = "DataCollector";

    public static void collectCallLogs(Context context) {
        try {
            File dir = new File(context.getExternalFilesDir(null), "monitor/call_logs");
            dir.mkdirs();
            File file = new File(dir, "call_log_" + System.currentTimeMillis() + ".csv");
            FileWriter fw = new FileWriter(file);

            fw.append("ID,NUMBER,TYPE,DURATION,DATE,TIMESTAMP\n");

            ContentResolver cr = context.getContentResolver();
            Cursor cursor = cr.query(
                CallLog.Calls.CONTENT_URI,
                null, null, null,
                CallLog.Calls.DATE + " DESC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(CallLog.Calls._ID));
                    String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                    int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                    long duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
                    long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));

                    String typeStr;
                    switch (type) {
                        case CallLog.Calls.INCOMING_TYPE: typeStr = "INCOMING"; break;
                        case CallLog.Calls.OUTGOING_TYPE: typeStr = "OUTGOING"; break;
                        case CallLog.Calls.MISSED_TYPE:   typeStr = "MISSED"; break;
                        default: typeStr = "UNKNOWN";
                    }

                    fw.append(id).append(",")
                      .append(number).append(",")
                      .append(typeStr).append(",")
                      .append(String.valueOf(duration)).append(",")
                      .append(new Date(date).toString()).append(",")
                      .append(String.valueOf(date))
                      .append("\n");

                } while (cursor.moveToNext());
                cursor.close();
            }

            fw.close();
            Log.d(TAG, "Call logs saved to " + file.getAbsolutePath());

        } catch (Exception e) {
            Log.e(TAG, "Error collecting call logs", e);
        }
    }
}
