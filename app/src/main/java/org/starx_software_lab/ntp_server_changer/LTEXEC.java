package org.starx_software_lab.ntp_server_changer;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

class LTEXEC {
    private final String tag = "LTEXEC";
    private final Process su = Runtime.getRuntime().exec("su");
    private final DataOutputStream ops = new DataOutputStream(su.getOutputStream());

    void exec(String cmd) {
        final String tcd = cmd.trim() + "\n";
        try {
            ops.writeBytes(tcd);
            ops.flush();
        } catch (IOException e) {
            Log.e(tag,e.toString());
        }
    }

    void exit() {
        this.exec("exit");
    }

    LTEXEC() throws IOException {
        Log.i(tag,"Session Started.");
    }
}
