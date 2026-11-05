package io.rns.evacardspack;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EvaCardActivity extends AppCompatActivity {

    private TextView clockTimeText;
    private TextView clockDateText;

    private TextView dashboardRamText;
    private TextView dashboardStorageText;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            updateTimeAndDate();
            refreshSystemStats();
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eva_card);

        clockTimeText = findViewById(R.id.clockTimeText);
        clockDateText = findViewById(R.id.clockDateText);

        dashboardRamText = findViewById(R.id.dashboardRamText);
        dashboardStorageText = findViewById(R.id.dashboardStorageText);

        LinearLayout panelWeather = findViewById(R.id.panelWeatherCard);
        LinearLayout panelClock = findViewById(R.id.panelClockCard);
        LinearLayout panelTrending = findViewById(R.id.panelTrendingCard);
        LinearLayout panelSystemMonitor = findViewById(R.id.panelSystemMonitorCard);

        setupPanelFocusAndClick(panelWeather, () ->
            Toast.makeText(this, "Weather Live Widget", Toast.LENGTH_SHORT).show()
        );
        setupPanelFocusAndClick(panelClock, () ->
            Toast.makeText(this, "Minimal Clock Widget", Toast.LENGTH_SHORT).show()
        );
        setupPanelFocusAndClick(panelTrending, () ->
            Toast.makeText(this, "Trending Movies Widget", Toast.LENGTH_SHORT).show()
        );
        setupPanelFocusAndClick(panelSystemMonitor, this::cleanMemoryAndRefresh);

        if (panelWeather != null) {
            panelWeather.requestFocus();
        }

        refreshSystemStats();
    }

    private void setupPanelFocusAndClick(View panel, Runnable onClick) {
        if (panel == null) return;

        panel.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(150).start();
            } else {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
            }
        });

        panel.setOnClickListener(v -> onClick.run());
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(timeRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(timeRunnable);
    }

    private void updateTimeAndDate() {
        Date now = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault());

        if (clockTimeText != null) {
            clockTimeText.setText(timeFormat.format(now));
        }
        if (clockDateText != null) {
            clockDateText.setText(dateFormat.format(now));
        }
    }

    private void refreshSystemStats() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        if (am != null) {
            am.getMemoryInfo(mi);
        }

        double totalRamGb = mi.totalMem / (1024.0 * 1024.0 * 1024.0);
        double availRamGb = mi.availMem / (1024.0 * 1024.0 * 1024.0);
        double usedRamGb = totalRamGb - availRamGb;
        int ramPercent = totalRamGb > 0 ? (int) ((usedRamGb / totalRamGb) * 100) : 0;

        if (dashboardRamText != null) {
            dashboardRamText.setText(String.format(Locale.getDefault(), "RAM: %d%% Used", ramPercent));
        }

        File dataPath = Environment.getDataDirectory();
        StatFs stat = new StatFs(dataPath.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        long availBlocks = stat.getAvailableBlocksLong();

        double totalStorageGb = (totalBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0);
        double availStorageGb = (availBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0);
        double usedStorageGb = totalStorageGb - availStorageGb;
        int storagePercent = totalStorageGb > 0 ? (int) ((usedStorageGb / totalStorageGb) * 100) : 0;

        if (dashboardStorageText != null) {
            dashboardStorageText.setText(String.format(Locale.getDefault(), "Storage: %d%% Used", storagePercent));
        }
    }

    private void cleanMemoryAndRefresh() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        long freeBefore = 0;
        if (am != null) {
            ActivityManager.MemoryInfo miBefore = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(miBefore);
            freeBefore = miBefore.availMem;

            List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
            if (processes != null) {
                for (ActivityManager.RunningAppProcessInfo info : processes) {
                    if (info.pkgList != null) {
                        for (String pkg : info.pkgList) {
                            if (!pkg.equals(getPackageName())) {
                                try {
                                    am.killBackgroundProcesses(pkg);
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                }
            }
        }
        System.gc();

        long freeAfter = 0;
        if (am != null) {
            ActivityManager.MemoryInfo miAfter = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(miAfter);
            freeAfter = miAfter.availMem;
        }

        long freedMb = Math.max(0, freeAfter - freeBefore) / (1024 * 1024);
        Toast.makeText(this, "⚡ Memory Boosted! Freed " + freedMb + " MB RAM", Toast.LENGTH_LONG).show();

        refreshSystemStats();
    }
}
