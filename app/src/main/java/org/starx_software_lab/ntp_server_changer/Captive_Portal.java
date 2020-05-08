package org.starx_software_lab.ntp_server_changer;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.net.HttpURLConnection;
import java.net.URL;

public class Captive_Portal extends AppCompatActivity {
    //定义模式
    final int MSG_FROM_CP_STATUS = 1;
    final int MSG_FROM_HTTPS_STATUS = 2;
    final int MSG_FROM_HTTPS_204_ADDRESS = 3;
    final int MSG_FROM_HTTP_204_ADDRESS = 4;
    final int MSG_FROM_204_TEST_HTTPS = 6;
    final int MSG_FROM_204_TEST_HTTP = 7;
    final int MSG_FROM_HTTPS_ADD_CHANGE_CONFIRM = 8;
    final int MSG_FROM_HTTP_ADD_CHANGE_CONFIRM = 9;
    final int MSG_FROM_CP_CHANGE_CONFIRM = 10;
    final int MSG_FROM_HTTPS_CHANGE_CONFIRM = 11;
    final int MSG_FROM_DATA_NETWORK_CONFIRM = 12;
    final int MSG_FROM_WIFI_CONFIRM = 13;
    //固定字符串
    final String check_cp_mode = "settings get global captive_portal_mode";
    final String set_cp_mode = "settings put global captive_portal_mode ";
    final String check_https_mode = "settings get global captive_portal_use_https";
    final String set_https_mode = "settings put global captive_portal_use_https ";
    final String check_https_depens = "settings get global captive_portal_https_url";
    final String set_https_depens = "settings put global captive_portal_https_url ";
    final String check_http_depens = "settings get global captive_portal_http_url";
    final String set_http_depens = "settings put global captive_portal_http_url ";
    final String alive = "测试可用!";
    final String fail = "测试不可用!\n状态码:";
    final String miui_https_server = "https://connect.rom.miui.com/generate_204";
    //
    WifiManager wifiManager;
    //绑定控件
    Switch cp_status, https_status;
    EditText https_204, http_204;
    CheckBox set_def;
    //全局变量
    private Handler.Callback update_ui = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String result = msg.obj.toString().trim();
            switch (msg.what) {
                case MSG_FROM_CP_STATUS:
                    int cp_status_mode;
                    try {
                        cp_status_mode = Integer.parseInt(result);
                        if (cp_status_mode == 1) {
                            cp_status.setChecked(true);
                        } else {
                            cp_status.setChecked(false);
                        }
                    } catch (Exception e) {
                        Snackbar.make(getWindow().getDecorView(), e.toString(), Snackbar.LENGTH_SHORT);
                        e.printStackTrace();
                    }
                    break;
                case MSG_FROM_HTTPS_STATUS:
                    int https_status_mode;
                    try {
                        https_status_mode = Integer.parseInt(result);
                        if (https_status_mode == 1) {
                            https_status.setChecked(true);
                        } else {
                            https_status.setChecked(false);
                        }
                    } catch (Exception e) {
                        Snackbar.make(getWindow().getDecorView(), e.toString(), Snackbar.LENGTH_SHORT);
                        e.printStackTrace();
                    }
                    break;
                case MSG_FROM_HTTPS_204_ADDRESS:
                    if (!result.equals("") & !result.equals("null")) {
                        https_204.setText(result);
                    }
                    break;
                case MSG_FROM_HTTP_204_ADDRESS:
                    if (!result.equals("") & !result.equals("null")) {
                        http_204.setText(result);
                    }
                    break;
                case MSG_FROM_204_TEST_HTTPS:
                    Log.d("Handle", "HTTPS_REC:" + result);
                    String https_code;
                    if (result.equals("204")) {
                        https_code = alive;
                    } else {
                        https_code = fail + result;
                    }
                    new AlertDialog.Builder(Captive_Portal.this)
                            .setTitle("HTTPS-测试完毕")
                            .setMessage(https_code)
                            .create()
                            .show();
                    break;
                case MSG_FROM_204_TEST_HTTP:
                    Log.d("Handle", "HTTP_REC:" + result);
                    String http_code;
                    if (result.equals("204")) {
                        http_code = alive;
                    } else {
                        http_code = fail + result;
                    }
                    Toast.makeText(Captive_Portal.this, "HTTP-测试完毕\n" + http_code, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_FROM_HTTPS_ADD_CHANGE_CONFIRM:
                    https_204_check();
                    break;
                case MSG_FROM_HTTP_ADD_CHANGE_CONFIRM:
                    http_204_check();
                    break;
                case MSG_FROM_CP_CHANGE_CONFIRM:
                    cp_check();
                    break;
                case MSG_FROM_HTTPS_CHANGE_CONFIRM:
                    https_check();
                    break;
                case MSG_FROM_DATA_NETWORK_CONFIRM:
                    Log.d("SVC", "Mobile Data " + result);
                    if (result.equals("killed")) {
                        Toast.makeText(getApplicationContext(), "请自行重启移动网络...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                    }
                    break;
                case MSG_FROM_WIFI_CONFIRM:
                    Log.d("SVC", "WIFI " + result);
                    if (result.equals("killed")) {
                        Toast.makeText(getApplicationContext(), "请自行重启Wi-Fi网络...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                    break;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        cp_status = findViewById(R.id.cp_status);
        https_status = findViewById(R.id.https_status);
        https_204 = findViewById(R.id.https_204);
        http_204 = findViewById(R.id.http_204);
        set_def = findViewById(R.id.cp_def1);
        selfcheck();
        cp_status.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String mode;
            if (isChecked) {
                mode = "1";
            } else {
                mode = "0";
            }
            exec_su set_cp = new exec_su();
            set_cp.addc(set_cp_mode + mode, MSG_FROM_CP_CHANGE_CONFIRM);
            set_cp.cvh(getWindow().getDecorView(), update_ui);
            set_cp.execute();
        });
        https_status.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String mode;
            if (isChecked) {
                mode = "1";
            } else {
                mode = "0";
            }
            exec_su set_https = new exec_su();
            set_https.addc(set_https_mode + mode, MSG_FROM_HTTPS_CHANGE_CONFIRM);
            set_https.cvh(getWindow().getDecorView(), update_ui);
            set_https.execute();
        });
    }


    public void startClick(View view) {
        String https_add = https_204.getText().toString().trim();
        String http_add = http_204.getText().toString().trim();
        if (set_def.isChecked()) {
            https_add = miui_https_server;
        }
        if (https_add.length() > 0) {
            exec_su set_https = new exec_su();
            set_https.addc(set_https_depens + https_add, MSG_FROM_HTTPS_ADD_CHANGE_CONFIRM);
            set_https.cvh(getWindow().getDecorView(), update_ui);
            set_https.execute();
        }
        //Giving you the second chance
        if (http_add.length() > 0) {
            exec_su set_http = new exec_su();
            set_http.addc(set_http_depens + http_add, MSG_FROM_HTTP_ADD_CHANGE_CONFIRM);
            set_http.cvh(getWindow().getDecorView(), update_ui);
            set_http.execute();
        }
    }

    public void testClick(View view) {
        String https_add = https_204.getText().toString();
        String http_add = http_204.getText().toString();
        if (https_add.length() > 0) {
            responsecode responsecode = new responsecode();
            responsecode.mode = MSG_FROM_204_TEST_HTTPS;
            responsecode.address = https_add;
            responsecode.start();
        }
        if (http_add.length() > 0) {
            responsecode responsecode2 = new responsecode();
            responsecode2.mode = MSG_FROM_204_TEST_HTTP;
            responsecode2.address = http_add;
            responsecode2.start();
        }
    }

    public void flushClick(View view) {
        View dec = getWindow().getDecorView();
        try {
            wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            assert wifiManager != null;
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
                Snackbar.make(getWindow().getDecorView(), "正在重新打开Wi-Fi...", Snackbar.LENGTH_SHORT).show();
                wifiManager.setWifiEnabled(true);
            } else {
                wifiManager.setWifiEnabled(true);
                Snackbar.make(getWindow().getDecorView(), "正在重新关闭Wi-Fi...", Snackbar.LENGTH_SHORT).show();
                wifiManager.setWifiEnabled(false);
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString() + "\n强制重启WiFi...", Toast.LENGTH_SHORT).show();
            exec_su set_wifi_d = new exec_su();
            set_wifi_d.cvh(dec, update_ui);
            set_wifi_d.addc("svc wifi disable", MSG_FROM_WIFI_CONFIRM);
            set_wifi_d.execute();
            exec_su set_wifi_o = new exec_su();
            set_wifi_o.cvh(dec, update_ui);
            set_wifi_o.addc("svc wifi enable", MSG_FROM_WIFI_CONFIRM);
            set_wifi_o.execute();
        }
        exec_su set_data_d = new exec_su();
        set_data_d.cvh(dec, update_ui);
        set_data_d.addc("svc data disable", MSG_FROM_DATA_NETWORK_CONFIRM);
        set_data_d.execute();
        exec_su set_data_o = new exec_su();
        set_data_o.cvh(dec, update_ui);
        set_data_o.addc("svc data enable", MSG_FROM_DATA_NETWORK_CONFIRM);
        set_data_o.execute();
    }

    public void selfcheck() {
        cp_check();
        https_check();
        https_204_check();
        http_204_check();
    }

    public void cp_check() {
        exec_su cp_check = new exec_su();
        cp_check.addc(check_cp_mode, MSG_FROM_CP_STATUS);
        cp_check.cvh(getWindow().getDecorView(), update_ui);
        cp_check.execute();
    }

    public void https_check() {
        exec_su https_check = new exec_su();
        https_check.addc(check_https_mode, MSG_FROM_HTTPS_STATUS);
        https_check.cvh(getWindow().getDecorView(), update_ui);
        https_check.execute();
    }

    public void https_204_check() {
        exec_su https_204_check = new exec_su();
        https_204_check.addc(check_https_depens, MSG_FROM_HTTPS_204_ADDRESS);
        https_204_check.cvh(getWindow().getDecorView(), update_ui);
        https_204_check.execute();
    }

    public void http_204_check() {
        exec_su http_204_check = new exec_su();
        http_204_check.addc(check_http_depens, MSG_FROM_HTTP_204_ADDRESS);
        http_204_check.cvh(getWindow().getDecorView(), update_ui);
        http_204_check.execute();
    }

    class responsecode extends Thread {
        int mode = 0;
        String address = "";

        @Override
        public void run() {
            Log.d("Thread", "Start with " + mode);
            final URL url;
            HttpURLConnection httpURLConnection = null;
            try {
                url = new URL(address);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setRequestMethod("GET");
                int code = httpURLConnection.getResponseCode();
                Message message = Message.obtain();
                message.what = mode;
                message.obj = code;
                Looper.prepare();
                new Handler(update_ui).sendMessage(message);
                Looper.loop();

            } catch (Exception e) {
                e.printStackTrace();
                Looper.prepare();
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                Looper.loop();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
            super.run();
        }


    }
}
