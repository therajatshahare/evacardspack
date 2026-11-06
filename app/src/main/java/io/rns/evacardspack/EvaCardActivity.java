package io.rns.evacardspack;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class EvaCardActivity extends AppCompatActivity {

    private TextView clockTimeText;
    private TextView clockDateText;

    private TextView dashboardRamText;
    private TextView dashboardStorageText;

    private boolean isShowingTvShows = false;

    // World Clock Dialog Views
    private Dialog clockDialog;
    private TextView dcTimeText;
    private TextView londonTimeText;
    private TextView seoulTimeText;
    private TextView bangkokTimeText;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            updateTimeAndDate();
            refreshSystemStats();
            updateWorldClocks();
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
        LinearLayout panelCalendar = findViewById(R.id.panelCalendarCard);
        LinearLayout panelSystemMonitor = findViewById(R.id.panelSystemMonitorCard);

        setupPanelFocusAndClick(panelWeather, this::showWeatherDetailDialog);
        setupPanelFocusAndClick(panelClock, this::showClockDetailDialog);
        setupPanelFocusAndClick(panelTrending, this::showTrendingDetailDialog);
        setupPanelFocusAndClick(panelCalendar, this::showCalendarDetailDialog);
        setupPanelFocusAndClick(panelSystemMonitor, this::cleanMemoryAndRefresh);

        if (panelWeather != null) {
            panelWeather.requestFocus();
        }

        refreshSystemStats();

        // Check for GitHub updates asynchronously
        new UpdateManager().checkForUpdates(this);
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

    // 1. Weather Detail Dialog
    private void showWeatherDetailDialog() {
        Dialog dialog = createStyledDialog(R.layout.dialog_weather_detail);
        Button btnClose = dialog.findViewById(R.id.btnCloseWeather);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
            btnClose.requestFocus();
        }
        dialog.show();
    }

    // 2. World Clock Detail Dialog
    private void showClockDetailDialog() {
        clockDialog = createStyledDialog(R.layout.dialog_clock_detail);
        dcTimeText = clockDialog.findViewById(R.id.dcTimeText);
        londonTimeText = clockDialog.findViewById(R.id.londonTimeText);
        seoulTimeText = clockDialog.findViewById(R.id.seoulTimeText);
        bangkokTimeText = clockDialog.findViewById(R.id.bangkokTimeText);

        Button btnClose = clockDialog.findViewById(R.id.btnCloseClock);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                clockDialog.dismiss();
                clockDialog = null;
            });
            btnClose.requestFocus();
        }
        updateWorldClocks();
        clockDialog.show();
    }

    // 3. Trending Movies & TV Shows Dialog
    private void showTrendingDetailDialog() {
        Dialog dialog = createStyledDialog(R.layout.dialog_trending_detail);
        TextView title = dialog.findViewById(R.id.trendingHeaderTitle);
        TextView subtitle = dialog.findViewById(R.id.trendingHeaderSubtitle);
        TextView item1 = dialog.findViewById(R.id.trendingItem1);
        TextView item2 = dialog.findViewById(R.id.trendingItem2);
        TextView item3 = dialog.findViewById(R.id.trendingItem3);
        TextView item4 = dialog.findViewById(R.id.trendingItem4);
        TextView item5 = dialog.findViewById(R.id.trendingItem5);
        Button btnToggle = dialog.findViewById(R.id.btnToggleTrendingType);
        Button btnClose = dialog.findViewById(R.id.btnCloseTrending);

        Runnable updateTrendingContent = () -> {
            if (isShowingTvShows) {
                if (title != null) title.setText("Top 5 Trending TV & Web Shows");
                if (subtitle != null) subtitle.setText("Currently showing top TV & Web Shows");
                if (item1 != null) item1.setText("1. 🧇 Stranger Things — ⭐ 8.7/10");
                if (item2 != null) item2.setText("2. 🧪 Breaking Bad — ⭐ 9.5/10");
                if (item3 != null) item3.setText("3. 🍄 The Last of Us — ⭐ 8.8/10");
                if (item4 != null) item4.setText("4. ⚔️ Game of Thrones — ⭐ 9.2/10");
                if (item5 != null) item5.setText("5. 🏢 Severance — ⭐ 8.7/10");
                if (btnToggle != null) btnToggle.setText("Switch to Movies 🎬");
            } else {
                if (title != null) title.setText("Top 5 Trending Movies");
                if (subtitle != null) subtitle.setText("Currently showing top Movies");
                if (item1 != null) item1.setText("1. 🍿 Interstellar (2014) — ⭐ 8.7/10");
                if (item2 != null) item2.setText("2. 🏜️ Dune: Part Two (2024) — ⭐ 8.5/10");
                if (item3 != null) item3.setText("3. ⚛️ Oppenheimer (2023) — ⭐ 8.9/10");
                if (item4 != null) item4.setText("4. 🦇 The Dark Knight (2008) — ⭐ 9.0/10");
                if (item5 != null) item5.setText("5. 🌀 Inception (2010) — ⭐ 8.8/10");
                if (btnToggle != null) btnToggle.setText("Switch to TV Shows 📺");
            }
        };

        updateTrendingContent.run();

        if (btnToggle != null) {
            btnToggle.setOnClickListener(v -> {
                isShowingTvShows = !isShowingTvShows;
                updateTrendingContent.run();
            });
            btnToggle.requestFocus();
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    // 4. Buddhist & Neo-Buddhist Calendar Detail Dialog
    private void showCalendarDetailDialog() {
        Dialog dialog = createStyledDialog(R.layout.dialog_calendar_detail);
        Button btnClose = dialog.findViewById(R.id.btnCloseCalendar);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
            btnClose.requestFocus();
        }
        dialog.show();
    }

    private Dialog createStyledDialog(int layoutResId) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layoutResId);
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        return dialog;
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

    private void updateWorldClocks() {
        if (clockDialog == null || !clockDialog.isShowing()) return;

        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());

        if (dcTimeText != null) {
            sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
            dcTimeText.setText(sdf.format(now));
        }
        if (londonTimeText != null) {
            sdf.setTimeZone(TimeZone.getTimeZone("Europe/London"));
            londonTimeText.setText(sdf.format(now));
        }
        if (seoulTimeText != null) {
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            seoulTimeText.setText(sdf.format(now));
        }
        if (bangkokTimeText != null) {
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Bangkok"));
            bangkokTimeText.setText(sdf.format(now));
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
