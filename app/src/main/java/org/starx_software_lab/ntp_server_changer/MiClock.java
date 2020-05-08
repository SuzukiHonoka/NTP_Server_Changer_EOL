package org.starx_software_lab.ntp_server_changer;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;

public class MiClock extends AppCompatActivity {
    Handler date_h;
    TextView miclock;
    final Runnable date_k = new Runnable() {
        @Override
        public void run() {
            miclock.setText(SimpleDateFormat.getDateTimeInstance().format(System.currentTimeMillis()));
            date_h.postDelayed(date_k, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        miclock = findViewById(R.id.miclock);
        date_h = new Handler();
        date_h.post(date_k);
    }

    protected void onDestroy() {
        date_h.removeCallbacks(date_k);
        super.onDestroy();
    }
}
