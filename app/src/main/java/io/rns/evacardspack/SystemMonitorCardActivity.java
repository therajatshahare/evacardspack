package io.rns.evacardspack;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class SystemMonitorCardActivity extends AppCompatActivity {

    private TextView ramStatusText;
    private TextView storageStatusText;
    private Button btnBoostMemory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_monitor_card);

        ramStatusText = findViewById(R.id.ramStatusText);
        storageStatusText = findViewById(R.id.storageStatusText);
        btnBoostMemory = findViewById(R.id.btnBoostMemory);

        btnBoostMemory.setOnClickListener(v -> cleanMemoryAndRefresh());

        refreshSystemStats();

        btnBoostMemory.requestFocus();
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

        if (ramStatusText != null) {
            ramStatusText.setText(String.format(Locale.getDefault(),
                    "RAM: %d%% Used (%.1f GB / %.1f GB)", ramPercent, usedRamGb, totalRamGb));
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

        if (storageStatusText != null) {
            storageStatusText.setText(String.format(Locale.getDefault(),
                    "Storage: %d%% Used (%.1f GB / %.1f GB)", storagePercent, usedStorageGb, totalStorageGb));
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
        Toast.makeText(this, "Memory Boosted! Freed " + freedMb + " MB RAM", Toast.LENGTH_LONG).show();

        refreshSystemStats();
    }
}
