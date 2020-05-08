package org.starx_software_lab.ntp_server_changer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

public class DNS_Changer extends AppCompatActivity {

    final int MSG_FROM_RETURN_DNS1 = 1;
    final int MSG_FROM_RETURN_DNS2 = 2;
    final int MSG_FORM_RETURN_RULES_CHECK = 3;
    final String get_dns1 = "getprop net.dns1";
    final String get_dns2 = "getprop net.dns2";
    final String set_dns1 = "setprop net.dns1 ";
    final String set_dns2 = "setprop net.dns2 ";
    final String set_main_dns_check = "iptables -t nat -L OUTPUT -n -v --line-numbers|grep 53";
    final String set_main_dns_dnat = "iptables -t nat -A OUTPUT -p udp --dport 53 -j DNAT --to-destination ";
    final String set_main_dns_dnat_u = "iptables -t nat -A OUTPUT -p tcp --dport 53 -j DNAT --to-destination ";
    final String set_main_dns_remove = "iptables -t nat -D OUTPUT 1";
    //1:>=Android5 2:<Android5
    int change_way = 1;
    EditText dns1, dns2, main_dns1, main_dns1_port;
    private Handler.Callback handler = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String TAG = "Handlemsg";
            switch (msg.what) {
                case MSG_FROM_RETURN_DNS1:
                    dns1.setText(msg.obj.toString().trim());
                    break;
                case MSG_FROM_RETURN_DNS2:
                    dns2.setText(msg.obj.toString().trim());
                    break;
                case MSG_FORM_RETURN_RULES_CHECK:
                    String rules = msg.obj.toString();
                    String formal_rules = main_dns1.getText().toString().trim() + ":" + main_dns1_port.getText().toString().trim();
                    if (rules.length() > 0) {
                        new AlertDialog.Builder(DNS_Changer.this)
                                .setTitle("已存在规则警告")
                                .setMessage("看起来，你的规则中已存在关于53端口NAT的规则了，以下是规则的内容，请确认。\n" + rules)
                                .setNeutralButton("删除第一行规则(TCP/UDP 需要删除两次)", (dialog, which) -> {
                                    exec(set_main_dns_remove,0,null);
//                                    exec_su rm_rules = new exec_su();
//                                    rm_rules.addc(set_main_dns_remove, 0);
//                                    rm_rules.cvh(getWindow().getDecorView(), handler);
//                                    rm_rules.execute();
                                })
                                .setPositiveButton("来肝!", (dialog, which) -> {
                                    exec(set_main_dns_dnat + formal_rules,0,null);
                                    exec(set_main_dns_dnat_u + formal_rules,0,null);
//                                    exec_su set_rules = new exec_su();
//                                    set_rules.addc(set_main_dns_dnat + formal_rules, 0);
//                                    set_rules.cvh(getWindow().getDecorView(), handler);
//                                    set_rules.execute();
                                })
                                .setNegativeButton("拒绝", (dialog, which) -> Snackbar.make(getWindow().getDecorView(), "你拒绝了!", Snackbar.LENGTH_SHORT).show())
                                .create()
                                .show();
                    } else {
                        exec(set_main_dns_dnat + formal_rules,0,null);
                        exec(set_main_dns_dnat_u + formal_rules,0,null);
//                        exec_su set_rules = new exec_su();
//                        set_rules.addc(set_main_dns_dnat + formal_rules, 0);
//                        Log.d(TAG, set_main_dns_dnat + formal_rules);
//                        set_rules.cvh(getWindow().getDecorView(), handler);
//                        set_rules.execute();
                    }
                    break;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main9);
        init_w();
        get_defaults_dns_address();
    }


    public void exec(String string,int mode,Handler.Callback handler) {
        int d_mode = 0;
        exec_su exec1 = new exec_su();
        if (mode > 0) {
            d_mode = mode;
        }
        exec1.addc(string, d_mode);
        if (handler != null) {
            exec1.cvh(getWindow().getDecorView(), handler);
        } else {
            exec1.cvh(getWindow().getDecorView(), null);
        }

        exec1.execute();
    }

    public void init_w() {
        dns1 = findViewById(R.id.set_dns1);
        dns2 = findViewById(R.id.set_dns2);
        if (change_way == 1) {
            main_dns1 = findViewById(R.id.set_main_dns1);
            main_dns1_port = findViewById(R.id.set_main_dns1_port);
        }
    }

    public void get_defaults_dns_address() {
        exec(get_dns1, MSG_FROM_RETURN_DNS1,handler);
        //dns1
//        exec_su getdns1 = new exec_su();
//        getdns1.addc(get_dns1, MSG_FROM_RETURN_DNS1);
//        getdns1.cvh(getWindow().getDecorView(), handler);
//        getdns1.execute();
        exec(get_dns2, MSG_FROM_RETURN_DNS2,handler);
        //dns2
//        exec_su getdns2 = new exec_su();
//        getdns2.addc(get_dns2, MSG_FROM_RETURN_DNS2);
//        getdns2.cvh(getWindow().getDecorView(), handler);
//        getdns2.execute();
    }

    public void dns_change(View view) {
        if (main_dns1.getText().toString().isEmpty() || main_dns1_port.getText().toString().isEmpty()) {
            Toast.makeText(DNS_Changer.this, "请确保IP地址及端口输入正确!", Toast.LENGTH_SHORT).show();
            return;
        }
        //check if the rules already exist
        exec(set_main_dns_check, MSG_FORM_RETURN_RULES_CHECK,handler);
//        exec_su check_rules = new exec_su();
//        check_rules.addc(set_main_dns_check, MSG_FORM_RETURN_RULES_CHECK);
//        check_rules.cvh(getWindow().getDecorView(), handler);
//        check_rules.execute();
        //在handler中执行判断
        //way1 END
        if (change_way == 2) {
            exec(set_dns1 + dns1.getText().toString().trim(), 0,null);
            //dns1
//            exec_su setdns1 = new exec_su();
//            setdns1.addc(set_dns1 + dns1.getText().toString().trim(), 0);
//            setdns1.cvh(getWindow().getDecorView(), handler);
//            setdns1.execute();
            exec(set_dns2 + dns2.getText().toString().trim(), 0,null);
            //dns2
//            exec_su setdns2 = new exec_su();
//            setdns2.addc(set_dns2 + dns2.getText().toString().trim(), 0);
//            setdns2.cvh(getWindow().getDecorView(), handler);
//            setdns2.execute();
        }
    }

    public void dns_flush(View view) {
        if (change_way == 2) {
            get_defaults_dns_address();
        }
        exec(set_main_dns_remove, 0,null);
        exec(set_main_dns_remove, 0,null);
//        exec_su rm_rules = new exec_su();
//        rm_rules.addc(set_main_dns_remove, 0);
//        rm_rules.cvh(getWindow().getDecorView(), handler);
//        rm_rules.execute();
    }

    public void old_adnroid(View view) {
        //change_way = 2;
        Toast.makeText(getApplicationContext(),"Not Allowed for now.",Toast.LENGTH_SHORT).show();
        //setContentView(R.layout.layout_old_android);
        //init_w();
        //get_defaults_dns_address();
    }
}
