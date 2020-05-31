package org.starx_software_lab.ntp_server_changer;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;


public class ADB_DEBUG extends AppCompatActivity {

    final String getprop = "getprop service.adb.tcp.port";
    final String setprop = "setprop service.adb.tcp.port ";
    final int MSG_FOR_PORT = 1;
    final int MSG_FOR_CONFIRM = 2;
    int autosw = 0;
    EditText lanip, lanport;
    Switch switch1;
    final Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String resultx = msg.obj.toString().trim();
            switch (msg.what) {
                case MSG_FOR_PORT:
                    autosw = 1;
                    lanport.setText(resultx);
                    try {
                        int int_port = Integer.parseInt(resultx);
                        if (int_port < 0) {
                            Snackbar.make(getWindow().getDecorView(), "当前未启用远程ADB调试。", Snackbar.LENGTH_SHORT).show();
                            switch1.setChecked(false);
                            autosw = 0;

                        } else if (int_port == 0) {
                            Snackbar.make(getWindow().getDecorView(), "无法获取当前ADB调试端口数据。", Snackbar.LENGTH_SHORT).show();
                            autosw = 0;
                        } else {
                            Snackbar.make(getWindow().getDecorView(), "当前已启用远程ADB调试。", Snackbar.LENGTH_SHORT).show();
                            switch1.setChecked(true);
                        }
                        break;
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        autosw = 0;
                    }
                case MSG_FOR_CONFIRM:
                    EXEC confirm = new EXEC();
                    confirm.cvh(getWindow().getDecorView(), callback);
                    confirm.addc(getprop, 0);
                    confirm.execute();
                    break;
                default:
                    switch (resultx) {
                        case "":
                            Snackbar.make(getWindow().getDecorView(), "无法获取当前ADB调试端口数据。", Snackbar.LENGTH_SHORT).show();
                            break;
                        case "-1":
                            Snackbar.make(getWindow().getDecorView(), "ADB调试端口已关闭。", Snackbar.LENGTH_SHORT).show();
                            switch1.setChecked(false);
                            break;
                        case "5555":
                            Snackbar.make(getWindow().getDecorView(), "默认ADB调试端口已启用。", Snackbar.LENGTH_SHORT).show();
                            switch1.setChecked(true);
                            break;
                        default:
                            Snackbar.make(getWindow().getDecorView(), "未知的ADB调试端口。", Snackbar.LENGTH_SHORT).show();
                            break;
                    }
                    break;
            }
            return false;
        }
    };
    CheckBox def;

    private static String intToIp(int i) {

        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        lanip = findViewById(R.id.lanip);
        lanport = findViewById(R.id.adbport);
        switch1 = findViewById(R.id.switch1);
        def = findViewById(R.id.def);


        try {
            //获取wifi服务
            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            //判断wifi是否开启
            if (wifiManager != null && wifiManager.isWifiEnabled()) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int ipAddress = wifiInfo.getIpAddress();
                String ip = intToIp(ipAddress);
                lanip.setText(ip);
            } else {
                Snackbar.make(getWindow().getDecorView(), "无法从WIFI管理器中取出本机IP。", Snackbar.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        checkself();


        switch1.setOnCheckedChangeListener((buttonView, isChecked) -> {

            Log.d("TAG", "onCheckedChanged: START" + autosw);
            if (autosw == 0) {
                String port = lanport.getText().toString();
                if (isChecked) {
                    if (def.isChecked()) {
                        port = "5555";
                    }
                    if (!port.equals("")) {
                        EXEC set = new EXEC();
                        set.cvh(getWindow().getDecorView(), callback);
                        set.addc(setprop + port, MSG_FOR_CONFIRM);
                        set.execute();
                        lanport.setText(port);
                    } else {
                        Snackbar.make(getWindow().getDecorView(), "不能接受空端口。", Snackbar.LENGTH_SHORT).show();
                        switch1.setChecked(false);
                    }
                } else {
                    EXEC unset = new EXEC();
                    unset.cvh(getWindow().getDecorView(), callback);
                    unset.addc(setprop + "-1", MSG_FOR_CONFIRM);
                    unset.execute();
                    lanport.setText("-1");
                }
            } else {
                autosw = 0;
            }
        });

    }

    private void checkself() {
        EXEC process1 = new EXEC();
        process1.cvh(getWindow().getDecorView(), callback);
        process1.addc(getprop, MSG_FOR_PORT);
        process1.execute();
        int adb = Settings.Secure.getInt(this.getContentResolver(),Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,0);
        if (adb == 0) {
            Toast.makeText(getApplicationContext(),"请注意，在未启用开发者选项时，可能无法正常授权远程主机的调试权限。",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(),"请注意，开发者选项已启用，请在调试完成后及时关闭，以避免未经授权的骇入。",Toast.LENGTH_SHORT).show();
        }
    }
}

