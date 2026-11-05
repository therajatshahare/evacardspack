package io.rns.evacardspack.clock;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import io.rns.evacardspack.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ClockCardActivity extends AppCompatActivity {

    private TextView clockTimeText;
    private TextView clockDateText;
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
        setContentView(R.layout.activity_clock_card);

        clockTimeText = findViewById(R.id.clockTimeText);
        clockDateText = findViewById(R.id.clockDateText);
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
