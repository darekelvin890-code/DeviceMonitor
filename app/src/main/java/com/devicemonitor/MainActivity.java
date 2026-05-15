package com.devicemonitor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStart = findViewById(R.id.btnStart);
        Button btnPerms = findViewById(R.id.btnPermissions);
        Button btnAccess = findViewById(R.id.btnAccessibility);

        btnPerms.setOnClickListener(v -> requestAllPermissions());
        btnAccess.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationCaptureService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            Toast.makeText(this, "Notification service started", Toast.LENGTH_SHORT).show();
        });
        // Start the local HTTP server for live view
        new Thread(() -> {
            try {
                LogServer logServer = new LogServer(this);
                logServer.start();
                runOnUiThread(() ->
                    Toast.makeText(this, "Live view at http://localhost:8080", Toast.LENGTH_LONG).show()
                );
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                    Toast.makeText(this, "Server failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void requestAllPermissions() {
        String[] perms;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms = new String[]{
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.POST_NOTIFICATIONS
            };
        } else {
            perms = new String[]{
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_CONTACTS
            };
        }
        ActivityCompat.requestPermissions(this, perms, PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] perms, @NonNull int[] grants) {
        super.onRequestPermissionsResult(code, perms, grants);
        boolean allGranted = true;
        for (int g : grants) {
            if (g != PackageManager.PERMISSION_GRANTED) allGranted = false;
        }
        Toast.makeText(this, allGranted ? "All permissions granted" : "Some permissions denied", Toast.LENGTH_LONG).show();
    }
}
