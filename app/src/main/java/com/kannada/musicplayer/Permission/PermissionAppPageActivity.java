package com.kannada.musicplayer.Permission;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.kannada.musicplayer.R;
import com.kannada.musicplayer.activities.MainActivity;
import com.kannada.musicplayer.ads.MyApplication;

import java.util.Map;

public class PermissionAppPageActivity extends AppCompatActivity {

    // ✅ Android 13+ (API 33+)
    private final String[] permissions13 = {
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_VIDEO,
            android.Manifest.permission.READ_MEDIA_AUDIO
    };

    // ✅ Android 6–12 (API 23–32)
    private final String[] permissionsBelow13 = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
    };

    // ✅ Modern Permission Launcher
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                // 🛡 Safety check (no crash)
                if (result == null || result.isEmpty()) {
                    Toast.makeText(this, "Permission request cancelled", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean granted = false;
                // ✅ Check if ANY permission granted
                for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                    if (Boolean.TRUE.equals(entry.getValue())) {
                        granted = true;
                        break;
                    }
                }
                if (granted) {
                    nextAct();
                } else {
                    Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
                }
            });

    private void applyDisplayCutouts() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_home), (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout());

            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(false);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightNavigationBars(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setNavigationBarContrastEnforced(false);
        }

        setContentView(R.layout.activity_permission_app_page);
        applyDisplayCutouts();

        CardView btnAllow = findViewById(R.id.btnAllow);

        btnAllow.setOnClickListener(view -> {
            // ✅ Android 13+ (API 33–36)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(permissions13);
                // ✅ Android 6–12 (API 23–32)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionLauncher.launch(permissionsBelow13);
            } else {
                // ✅ Below API 23 (no runtime permission)
                nextAct();
            }
        });
    }

    private void nextAct() {
        MyApplication.setuser_permission(1);
        Intent intent = new Intent(PermissionAppPageActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}