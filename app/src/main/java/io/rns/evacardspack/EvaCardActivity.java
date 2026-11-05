package io.rns.evacardspack;

import android.content.SharedPreferences;
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

    private static final String PREFS_NAME = "eva_cards_pack_prefs";
    private static final String KEY_ACTIVE_CARD = "active_card";

    private TextView clockTimeText;
    private TextView clockDateText;

    private LinearLayout panelWeather;
    private LinearLayout panelClock;
    private LinearLayout panelTrending;

    private TextView badgeWeather;
    private TextView badgeClock;
    private TextView badgeTrending;

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

        badgeWeather = findViewById(R.id.badgeWeather);
        badgeClock = findViewById(R.id.badgeClock);
        badgeTrending = findViewById(R.id.badgeTrending);

        panelWeather.setOnClickListener(v -> selectActiveCard("weather", "Weather Live"));
        panelClock.setOnClickListener(v -> selectActiveCard("clock", "Minimal Clock"));
        panelTrending.setOnClickListener(v -> selectActiveCard("trending", "Trending Movies"));

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String currentActive = prefs.getString(KEY_ACTIVE_CARD, "weather");
        updateBadges(currentActive);
    }

    private void selectActiveCard(String cardKey, String cardName) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_ACTIVE_CARD, cardKey).apply();
        updateBadges(cardKey);
        Toast.makeText(this, "Selected " + cardName + " as Active Card", Toast.LENGTH_SHORT).show();
    }

    private void updateBadges(String activeKey) {
        if (badgeWeather != null) {
            badgeWeather.setVisibility("weather".equals(activeKey) ? View.VISIBLE : View.GONE);
        }
        if (badgeClock != null) {
            badgeClock.setVisibility("clock".equals(activeKey) ? View.VISIBLE : View.GONE);
        }
        if (badgeTrending != null) {
            badgeTrending.setVisibility("trending".equals(activeKey) ? View.VISIBLE : View.GONE);
        }
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
