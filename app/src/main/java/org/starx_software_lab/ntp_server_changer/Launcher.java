package org.starx_software_lab.ntp_server_changer;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;

public class Launcher extends AppCompatActivity {
    int isfirstlaunch;
    Handler countdown;
    int count_time = 3;
    final Runnable date_k = new Runnable() {
        @Override
        public void run() {
            count_time -= 1;
            //Toast.makeText(getApplicationContext(),"正在启动..."+count_time+"s后将自动跳转。",Toast.LENGTH_SHORT).show();
            if (count_time == 0) {
                startActivity(new Intent().setClass(getApplicationContext(), NTP_Changer.class));
                countdown.removeCallbacks(date_k);
                finish();
                return;
            }
            countdown.postDelayed(date_k, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        /* Hide both the navigation bar and the status bar.
         SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
         a general rule, you should design your app to hide the status bar whenever you
         hide the navigation bar. */
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_main7);
        if (!(getSharedPreferences("random_enable", MODE_PRIVATE).getBoolean("random_enable", true))) {
            startActivity(new Intent().setClass(getApplicationContext(), NTP_Changer.class));
            finish();
        }
        try {
            File savedir = getApplicationContext().getFilesDir();
            String[] imgs = savedir.list();
            Log.d("ACG", Arrays.toString(imgs));
            //Toast.makeText(getApplicationContext(), Arrays.toString(imgs),Toast.LENGTH_SHORT).show();
            assert imgs != null;
            if (imgs.length == 0) {
                isfirstlaunch = 1;
                startActivity(new Intent().setClass(getApplicationContext(), NTP_Changer.class));
                finish();
            } else {
                Random random = new Random();
                int acg_det = random.nextInt(imgs.length);
                String absolute_img = savedir + "/" + imgs[acg_det];
                Log.d("ACG", absolute_img);
                InputStream inputStream = new FileInputStream(absolute_img);
                ((ImageView) findViewById(R.id.acg_container)).setImageDrawable(Drawable.createFromStream(inputStream, "acg"));
                countdown = new Handler();
                countdown.post(date_k);
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }

    }

    public void jump(View view) {
        countdown.removeCallbacks(date_k);
        startActivity(new Intent().setClass(getApplicationContext(), NTP_Changer.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        countdown.removeCallbacks(date_k);
        super.onBackPressed();
    }
}
