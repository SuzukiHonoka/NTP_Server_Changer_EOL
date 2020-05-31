package org.starx_software_lab.ntp_server_changer;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

public class EXEC extends AsyncTask<Void, Void, Boolean> {
    //init_vars
    @SuppressLint("StaticFieldLeak")
    private View view = null;
    private Handler.Callback handler = null;
    private StringBuilder result_t = new StringBuilder();
    private String commends = null;
    private int model = 0;

    void cvh(View view, Handler.Callback handler) {
        if (view != null) {
            this.view = view;
        }
        if (handler != null) {
            this.handler = handler;
        }
    }

    void addc(String str, int model) {
        this.commends = str;
        this.model = model;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            BufferedReader din = new BufferedReader(new InputStreamReader(p.getInputStream()));
            dos.writeBytes(commends + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            String line;
            while ((line = din.readLine()) != null) {
                result_t.append(line).append("\n");
            }
            p.waitFor();
        } catch (Exception e) {
            Looper.prepare();
            if (view != null) {
                Snackbar.make(view, e.toString(), Snackbar.LENGTH_SHORT).show();
            }
            Looper.loop();
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (view != null) {
            Snackbar.make(view, "异步线程执行完毕", Snackbar.LENGTH_SHORT).show();
        }
        Log.d("Return", result_t.toString());
        Message message = Message.obtain();
        message.what = model;
        message.obj = result_t;
        if (handler != null) {
            new Handler(handler).sendMessage(message);
        }
        super.onPostExecute(aBoolean);
    }

    protected void onPreExecute() {
        if (view != null) {
            Snackbar.make(view, "异步线程准备执行", Snackbar.LENGTH_SHORT).show();
        }
    }

}