package io.rns.evacardspack;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EvaCardActivity extends AppCompatActivity {

    private TextView clockTimeText;
    private TextView clockDateText;

    private LinearLayout panelWeather;
    private LinearLayout panelClock;
    private LinearLayout panelTrending;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            updateTimeAndDate();
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eva_card);

        clockTimeText = findViewById(R.id.clockTimeText);
        clockDateText = findViewById(R.id.clockDateText);

        panelWeather = findViewById(R.id.panelWeatherCard);
        panelClock = findViewById(R.id.panelClockCard);
        panelTrending = findViewById(R.id.panelTrendingCard);

        setupPanelFocusAndClick(panelWeather, "Weather Live");
        setupPanelFocusAndClick(panelClock, "Minimal Clock");
        setupPanelFocusAndClick(panelTrending, "Trending Movies");

        if (panelWeather != null) {
            panelWeather.requestFocus();
        }
    }

    private void setupPanelFocusAndClick(View panel, String cardName) {
        if (panel == null) return;

        panel.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(150).start();
            } else {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
            }
        });

        panel.setOnClickListener(v ->
            Toast.makeText(EvaCardActivity.this, cardName + " Widget", Toast.LENGTH_SHORT).show()
        );
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
}
