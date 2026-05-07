// MainActivity.java
package com.yourorg.pentest.monitor;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView status = findViewById(R.id.status);
        Button btnAccessibility = findViewById(R.id.btn_accessibility);
        Button btnNotifications = findViewById(R.id.btn_notifications);
        Button btnCollectCallLogs = findViewById(R.id.btn_calllogs);
        Button btnExport = findViewById(R.id.btn_export);

        btnAccessibility.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        });

        btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(
                "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        });

        btnCollectCallLogs.setOnClickListener(v -> {
            if (checkSelfPermission(android.Manifest.permission.READ_CALL_LOG)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                DataCollector.collectCallLogs(this);
                status.setText("Call logs collected!");
            } else {
                requestPermissions(
                    new String[]{android.Manifest.permission.READ_CALL_LOG}, 1001);
            }
        });

        btnExport.setOnClickListener(v -> {
            // Opens the monitor data directory
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(
                android.net.Uri.fromFile(getExternalFilesDir(null)),
                "resource/folder");
            startActivity(Intent.createChooser(intent, "Open data folder"));
        });
    }
}
