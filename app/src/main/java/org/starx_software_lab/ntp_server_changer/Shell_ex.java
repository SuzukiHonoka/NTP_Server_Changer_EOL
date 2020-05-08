package org.starx_software_lab.ntp_server_changer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class Shell_ex extends AppCompatActivity {

    final int MSG_FROM_RETURN = 1;
    SharedPreferences preferences;
    EditText commend, result;
    private Handler.Callback handler = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_FROM_RETURN) {
                result.setText(msg.obj.toString());
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main6);
        commend = findViewById(R.id.command);
        result = findViewById(R.id.result);
        preferences = getSharedPreferences("exec_times", MODE_PRIVATE);
        int count = preferences.getInt("exec_times", MODE_PRIVATE);
        if (count == 0) {
            new AlertDialog.Builder(this)
                    .setTitle("首次使用说明")
                    .setMessage("看起来，你是第一次使用此功能。请勿执行类似于top等循环返回数据的命令，否则本应用的所有shell功能将因为被系统占用而无法使用。如不小心执行了这些命令，请强制停止本应用。")
                    .create()
                    .show();
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("exec_times", ++count);
        editor.apply();
    }

    public void execute(View view) {
        String commands = commend.getText().toString();
        exec_su process = new exec_su();
        process.addc(commands, MSG_FROM_RETURN);
        process.cvh(getWindow().getDecorView(), handler);
        process.execute();
    }

    public void clean(View view) {
        commend.setText("");
        result.setText("");
    }
}
