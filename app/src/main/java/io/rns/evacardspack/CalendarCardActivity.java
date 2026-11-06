package io.rns.evacardspack;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class CalendarCardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showCalendarDetailDialog();
    }

    private void showCalendarDetailDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_calendar_detail);
        dialog.setOnDismissListener(d -> finish());

        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        Button btnClose = dialog.findViewById(R.id.btnCloseCalendar);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
            btnClose.requestFocus();
        }

        dialog.show();
    }
}
