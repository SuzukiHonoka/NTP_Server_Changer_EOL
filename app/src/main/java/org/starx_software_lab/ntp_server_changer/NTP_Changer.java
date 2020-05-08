package org.starx_software_lab.ntp_server_changer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


public class NTP_Changer extends AppCompatActivity implements View.OnClickListener {

    final int MSG_FROM_EXECUTE = 1;
    final int MSG_FROM_LOCATION = 2;
    final int MSG_NEEDED_SHOW = 3;
    final int MSG_STARTUP = 4;
    final int MSG_TIME_CHECK = 5;
    final int MSG_FINAL_LOCATION = 6;
    final int MSG_FINAL_DIFF = 7;
    final int MSG_SET_TIME = 8;
    //固定字符串
    final String check_ntp = "settings get global ntp_server";
    final String set_ntp = "settings put global ntp_server ";
    final String def1_a = "ntp1.aliyun.com";
    final String ping_c = "ping -c 4 ";
    public Long exmill = null;
    //
    SharedPreferences preferences;
    //绑定控件
    Button start;
    EditText result, set;
    CheckBox def1;
    Button test;
    Button ntp_sync;
    TextView date_t, date_ntp, location_ntp, date_diff;
    Handler date_h;
    //
    int isdownloading = 1;
    boolean dialog_enable_status = true;
    int global_interval = 500;
    boolean ping_enable_status = false;
    private Handler.Callback main_handler = new Handler.Callback() {
        public boolean handleMessage(@NotNull Message msg) {
            String TAG = "Main_handler";
            String resultx = msg.obj.toString().trim();
            switch (msg.what) {
                case MSG_FROM_EXECUTE:
                    if (resultx.equals("")) {
                        Snackbar.make(getWindow().getDecorView(), "对于NTP服务器参数:[" + set.getText().toString() + "]的刷新无法确认，请检查所需权限是否已被授予!", Snackbar.LENGTH_LONG).show();
                    } else {
                        result.setText(resultx);
                        Snackbar.make(getWindow().getDecorView(), "本机NTP服务器地址已成功刷新!", Snackbar.LENGTH_LONG).show();
                    }
                    break;
                case MSG_STARTUP:
                    result.setText(resultx);
                    break;
                case MSG_FROM_LOCATION:
                    if (dialog_enable_status)
                        sdialog(new String[]{"NTP地理位置", "获取到的NTP服务器位置:\n" + resultx});
                    if (ping_enable_status) {
                        exec_su test = new exec_su();
                        test.cvh(getWindow().getDecorView(), main_handler);
                        test.addc(ping_c + result.getText().toString(), MSG_NEEDED_SHOW);
                        test.execute();
                    }
                    break;
                case MSG_NEEDED_SHOW:
                    if (dialog_enable_status) sdialog(new String[]{"显示普通异步线程的消息", resultx});
                    break;
                case MSG_TIME_CHECK:
                    if (dialog_enable_status) sdialog(new String[]{"时间戳校对", resultx});
                    break;
                case MSG_FINAL_LOCATION:
                    location_ntp.setText(resultx);
                    break;
                case MSG_FINAL_DIFF:
                    date_diff.setText(resultx);
                    break;
                case MSG_SET_TIME:
                    Log.d("SET_FALL_BACK", resultx);
                default:
                    break;
            }
            return false;
        }
    };
    //初始化获取字符串
    private Long ntp_time = null;
    final Runnable date_k = new Runnable() {
        @Override
        public void run() {
            date_t.setText(SimpleDateFormat.getDateTimeInstance().format(System.currentTimeMillis()));
            if (ntp_time != null) {
                ntp_time += 100;
                date_ntp.setText(SimpleDateFormat.getDateTimeInstance().format(ntp_time));
            }
            date_h.postDelayed(date_k, 100);
        }
    };
    private Long ntp_diff = null;
    private String ntp_location = null;

    //初始化NTP时间
    //New Thread lock
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = findViewById(R.id.start);
        result = findViewById(R.id.Result_ntp);
        set = findViewById(R.id.Set_ntp);
        def1 = findViewById(R.id.def1);
        test = findViewById(R.id.test);
        date_t = findViewById(R.id.date_t);
        date_ntp = findViewById(R.id.date_ntp);
        location_ntp = findViewById(R.id.location_ntp);
        date_diff = findViewById(R.id.date_diff);
        ntp_sync = findViewById(R.id.ntp_sync);
        //绑定监听器
        start.setOnClickListener(this);
        test.setOnClickListener(this);
        ntp_sync.setOnClickListener(this);
        //使获取结果框不能被编辑并设置结果
        result.setKeyListener(null);
        //权限检查
        per_check();
        //获取关键变量
        global_interval = getSharedPreferences("ntp_interval", MODE_PRIVATE).getInt("ntp_interval", 500);
        dialog_enable_status = (getSharedPreferences("dialog_enable", MODE_PRIVATE).getBoolean("dialog_enable", false));
        ping_enable_status = (getSharedPreferences("ping_enable", MODE_PRIVATE).getBoolean("ping_enable", false));
        //
        exec_su startup = new exec_su();
        startup.cvh(getWindow().getDecorView(), main_handler);
        startup.addc(check_ntp, MSG_STARTUP);
        startup.execute();


        date_h = new Handler();
        date_h.post(date_k);
        preferences = getSharedPreferences("count", MODE_PRIVATE);
        int count = preferences.getInt("count", MODE_PRIVATE);
        if (count == 0) {
            sdialog(new String[]{"欢迎使用本修改器-首次启动", getString(R.string.ste0)});
            sdialog(new String[]{"关于自动校对时间不准的问题说明",getString(R.string.ste1)});
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("count", ++count);
        editor.apply();
        if ((getSharedPreferences("random_enable", MODE_PRIVATE).getBoolean("random_enable", true))) {
            get_random_pic();
        } else {
            isdownloading = 0;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this)
                .inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.asrc:
                new MaterialDialog.Builder(this)
                        .title("Open Source License")
                        .content("Material-Dialogs\nApache.Net")
                        .positiveText("OK")
                        .show();
                break;
            case R.id.about:
                sdialog(new String[]{"关于", "此程序由 Starx 开发.\n为了更方便的解决你的问题, 我们邀请你加入我们的用户群.\nQQ 群号:985171352\n首发于酷安 @夜空中闪耀的星\n注意: 需要获取ROOT权限以便执行一些SHELL命令."});

                break;
            case R.id.contact:
                try {
                    startActivity(new Intent()
                            .setData(Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=1787074172"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.exit:
                finishAndRemoveTask();
                break;
            case R.id.nor_reboot:
                sdialog(new String[] {"警告","即将执行正常重启，该操作无法取消，"});
                exec_su nrb = new exec_su();
                nrb.addc("am start -a android.intent.action.REBOOT", 0);
                nrb.execute();
                break;
            case R.id.reboot:
                sdialog(new String[] {"警告","即将执行硬重启，该操作无法取消，"});
                exec_su rebootc = new exec_su();
                rebootc.addc("reboot", 0);
                rebootc.execute();
                break;
            case R.id.clock:
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), MiClock.class);
                startActivity(intent);
                break;
            case R.id.adb_intent:
                startActivity(new Intent().setClass(getApplicationContext(), ADB_DEBUG.class));
                break;
            case R.id.open_time_check_website:
                Uri uri = Uri.parse("https://time.is");
                Intent openwebsite = new Intent()
                        .setAction(Intent.ACTION_VIEW)
                        .setData(uri);
                startActivity(openwebsite);
                break;
            case R.id.open_global_time_website:
                Uri uri2 = Uri.parse("https://www.timeanddate.com/worldclock/");
                Intent openwebsite2 = new Intent()
                        .setAction(Intent.ACTION_VIEW)
                        .setData(uri2);
                startActivity(openwebsite2);
                break;
            case R.id.reboot_to_recovery:
                sdialog(new String[] {"警告","即将硬重启至恢复模式，该操作无法取消，"});
                exec_su commedx2 = new exec_su();
                commedx2.addc("reboot recovery", 0);
                commedx2.execute();
                break;
            case R.id.reboot_to_bootloader:
                sdialog(new String[] {"警告","即将硬重启至线刷模式，该操作无法取消，"});
                exec_su commedx3 = new exec_su();
                commedx3.addc("reboot bootloader", 0);
                commedx3.execute();
                break;
            case R.id.nor_poweroff:
                sdialog(new String[] {"警告","即将执行正常关机，该操作无法取消，"});
                exec_su npf = new exec_su();
                npf.addc("am start -a com.android.internal.intent.action.REQUEST_SHUTDOWN", 0);
                npf.execute();
                break;
            case R.id.ha_poweroff:
                sdialog(new String[] {"警告","即将执行硬关机，该操作无法取消，"});
                exec_su hpf = new exec_su();
                hpf.addc("reboot -p", 0);
                hpf.execute();
                break;
            case R.id.captive_portal_intent:
                startActivity(new Intent().setClass(getApplicationContext(), Captive_Portal.class));
                break;
            case R.id.ddos_intent:
                startActivity(new Intent().setClass(getApplicationContext(), DDOS.class));
                break;
            case R.id.go_to_blog:
                Uri uri3 = Uri.parse("https://www.ioflow.xyz/");
                Intent openwebsite3 = new Intent()
                        .setAction(Intent.ACTION_VIEW)
                        .setData(uri3);
                startActivity(openwebsite3);
                break;
            case R.id.super_su_exec:
                startActivity(new Intent().setClass(getApplicationContext(), Shell_ex.class));
                break;
            case R.id.setting:
                settings();
                break;
            case R.id.music_player:
                startActivity(new Intent().setClass(getApplicationContext(), Music_Player.class));
                break;
            case R.id.set_dns:
                startActivity(new Intent().setClass(getApplicationContext(), DNS_Changer.class));
                break;
            case R.id.set_ringtone:
                startActivity(new Intent().setClass(getApplicationContext(), Ringtone_Changer.class));
                break;
            case R.id.global_media_ctrl:
                startActivity(new Intent().setClass(getApplicationContext(),SoundCTRL.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void sdialog(String[] t) {
        try {
            new MaterialDialog.Builder(this)
                    .title(t[0])
                    .content(t[1])
                    .positiveText(R.string.ok)
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    protected void gettime() {
        String TAG = "NTP_SYNC";
        Log.d(TAG, "ID:" + getTaskId());
        //记录进程开始执行的时间
        final Long processing = new Date().getTime();
        final Thread ntp = new Thread() {
            @Override
            public void run() {
                NTPUDPClient client = new NTPUDPClient();
                try {
                    InetAddress server = InetAddress.getByName(result.getText().toString().replace("\n", ""));
                    client.setDefaultTimeout(10000);
                    client.open();
                    TimeInfo timeInfo = client.getTime(server);
                    client.close();
                    TimeStamp timeStamp = timeInfo.getMessage().getTransmitTimeStamp();
                    String NTP_Time = SimpleDateFormat.getDateTimeInstance().format(timeStamp.getDate());
                    Date ntp = timeStamp.getDate();
                    //获取到NTP的时间
                    ntp_time = ntp.getTime();
                    //获取现在的时间
                    final Date nowdate = new Date();
                    final Long nowlong = new Date().getTime();
                    //实际NTP时间=NTP时间+差值
                    final Long time_diff = nowlong - processing;
                    //实际NTP值
                    ntp_time = ntp_time + time_diff;
                    if (!nowlong.equals(ntp_time)) {
                        long d = nowlong - ntp_time;
                        ntp_diff = d;
                        runOnUiThread(() -> {
                            String diff = ntp_diff + getString(R.string.ms);
                            date_diff.setText(diff);
                        });
                        if (Math.abs(d) >= global_interval) {
                            if (!SystemClock.setCurrentTimeMillis(ntp_time)) {
                                //完善时间主动校准
                                String termi_date = new SimpleDateFormat("MMddHHmmYYYY.ss", Locale.getDefault()).format(new Date(ntp_time));
                                Process force_p = Runtime.getRuntime().exec("su");
                                DataOutputStream dos = new DataOutputStream(force_p.getOutputStream());
                                dos.writeBytes("date " + termi_date + "\n");
                                dos.flush();
                                dos.writeBytes("exit\n");
                                dos.flush();
                                Log.d("SET_DATE", termi_date);
                                runOnUiThread(() -> Toast.makeText(getApplicationContext(), termi_date, Toast.LENGTH_SHORT).show());
                                Looper.prepare();
                                sdialog(new String[]{"时间戳过期", "当前系统时间与NTP服务器时间差值较大，已主动进行校准，若无效请手动进行配置。\n当前差值:" + d + "\n请重新执行测试。"});
                                //startActivity(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
                                Looper.loop();
                            } else {
                                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "好耶!系统软件赛高!", Toast.LENGTH_SHORT).show());
                            }
                        }

                        String thread_diff = String.valueOf(nowlong - processing);
                        String msg = "当前NTP时间与本地时间差值:" + d + "ms" + "\n系统时间戳:" + nowdate.getTime() + "\nNTP服务器时间戳:" + ntp.getTime() + "\n从线程执行消耗时间:" + thread_diff + "ms";

                        Snackbar.make(getWindow().getDecorView(), "Thread_DIFF:" + thread_diff, Snackbar.LENGTH_LONG).show();
                        Log.d("OFFSET", msg);
                        Message message = Message.obtain();
                        message.what = MSG_TIME_CHECK;
                        //message.obj = "System: "+nowdate.getTime()+"\nNTP: "+ntp.getTime();
                        message.obj = msg;
                        Looper.prepare();
                        new Handler(main_handler).sendMessage(message);
                        Looper.loop();
                    } else {
                        Snackbar.make(getWindow().getDecorView(), "当前NTP时间:" + NTP_Time, Snackbar.LENGTH_LONG).show();
                    }

                    Message message = Message.obtain();
                    message.what = MSG_NEEDED_SHOW;
                    message.obj = "当前NTP时间:" + NTP_Time;
                    Looper.prepare();
                    new Handler(main_handler).sendMessage(message);
                    Looper.loop();


                } catch (IOException e) {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }

                super.run();
            }

//            private void runOnUiThread() {
//                date_diff.setText(ntp_diff.toString());
//            }
        };
        ntp.start();
    }

    @Override
    protected void onDestroy() {
        date_h.removeCallbacks(date_k);
        super.onDestroy();
    }

    public void onBackPressed() {
        if (isdownloading == 0) {
            Snackbar.make(getWindow().getDecorView(), "再按一次即可退出", Snackbar.LENGTH_SHORT).show();
            if (exmill == null) {
                exmill = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - exmill <= 2000) {
                finishAndRemoveTask();
            } else {
                exmill = System.currentTimeMillis();
            }
            //super.onBackPressed();
        } else {
            Snackbar.make(getWindow().getDecorView(), "有数据正在被处理，请稍后再试。", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                if (Build.VERSION.SDK_INT >= 26) {

                    if (def1.isChecked()) {
                        set.setText(def1_a);
                    }
                    if (set.getText().toString().length() == 0) {
                        Snackbar.make(getWindow().getDecorView(), "被修改的NTP服务器地址不能为空。", Snackbar.LENGTH_SHORT).show();
                    } else {
                        exec_su set_up = new exec_su();
                        //分两个异步线程读取
                        set_up.addc(set_ntp + set.getText().toString(), 0);
                        set_up.execute();
                        exec_su checkon = new exec_su();
                        checkon.cvh(getWindow().getDecorView(), main_handler);
                        checkon.addc(check_ntp, MSG_FROM_EXECUTE);
                        checkon.execute();
                    }
                } else {
                    Snackbar.make(getWindow().getDecorView(), "当前设备的Android版本不支持本修改器。\n[Android Ver < 8.0]", Snackbar.LENGTH_SHORT).show();
                }
                break;

            case R.id.test:
                Snackbar.make(getWindow().getDecorView(), "需要保持正常的网络连接，请等待。", Snackbar.LENGTH_SHORT).show();
                try {
                    gettime();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }
                location location = new location();
                location.address = result.getText().toString();
                location.start();
                break;
            case R.id.ntp_sync:
                try {
                    gettime();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;

        }

    }

    public void get_random_pic() {

        StringBuilder content = new StringBuilder();

        try {
            //获取wifi服务
            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            //判断wifi是否开启
            if (wifiManager != null && wifiManager.isWifiEnabled()) {
                Log.d("Random", "Starting get download_address");
                new Thread(() -> {
                    String image_add = "";
                    try {
                        HttpURLConnection con = (HttpURLConnection) (new URL("https://pic.ioflow.xyz/?random").openConnection());
                        con.setConnectTimeout(5000);
                        con.setInstanceFollowRedirects(false);
                        con.connect();
                        int responseCode = con.getResponseCode();
                        //System.out.println(responseCode);
                        String location = con.getHeaderField("Location");
                        //System.out.println(location);
                        con.disconnect();
                        if (responseCode == 301) {
                            potato("301: IMG API Request Succeeded.",Toast.LENGTH_SHORT);
                            HttpURLConnection download_add = (HttpURLConnection) (new URL(location).openConnection());
                            download_add.setConnectTimeout(5000);
                            download_add.connect();
                            if (download_add.getResponseCode() == 200) {
                                InputStream in = download_add.getInputStream();
                                InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
                                BufferedReader reader = new BufferedReader(isr);
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    content.append(line);
                                }
                                String image_addex = content.toString().substring(content.toString().lastIndexOf("<link rel=\"image_src\" href=\"") + "<link rel=\"image_src\" href=\"".length());
                                image_add = image_addex.substring(0, image_addex.indexOf("\">"));
                                //image_add=StringUtils.substringBefore(StringUtils.substringAfterLast(content.toString(), "<link rel=\"image_src\" href=\""),"\">");
                            }
                        } else {
                            potato(responseCode + ": IMG API Request Failed.",Toast.LENGTH_SHORT);
                        }
                        //显示random获取的地址
                        String finalImage_add = image_add;
                        //保存的文件名
                        String filename = finalImage_add.substring(finalImage_add.lastIndexOf("/"));
                        //取出程序在Data的私有目录
                        File savedir = getApplicationContext().getFilesDir();
                        //
                        String absolute_path = savedir.toString() + filename;
                        //准备创建文件
                        File file = new File(savedir, filename);
                        int imgs_num = Objects.requireNonNull(savedir.list()).length;
                        int range = getSharedPreferences("random_num", MODE_PRIVATE).getInt("random_num", 2);
                        if (range == 0) {
                            range = 100;
                        }
                        Log.d("Random", "Preload_Range:" + range + " Files_ab_Range:" + imgs_num);
                        if (imgs_num >= range) {
                            isdownloading = 0;
                            Log.d("Random", "Canceled the download");
                            return;
                        }

                        if (!file.exists()) {
                            Log.d("Random", "Starting download");
                            //File save
                            int count;
                            try {
                                URL url = new URL(finalImage_add);
                                URLConnection conection = url.openConnection();
                                conection.setConnectTimeout(5000);
                                conection.connect();
                                // this will be useful so that you can show a tipical 0-100%
                                // progress bar
                                int lenghtOfFile = conection.getContentLength();
                                // download the file
                                InputStream input = new BufferedInputStream(url.openStream(),
                                        8192);
                                // Output stream
                                OutputStream output = new FileOutputStream(absolute_path);

                                byte[] data = new byte[1024];

                                long total = 0;

                                while ((count = input.read(data)) != -1) {
                                    total += count;
                                    // publishing the progress....
                                    // After this onProgressUpdate will be called
                                    //publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                                    Log.d("ACG", "" + (int) ((total * 100) / lenghtOfFile));
                                    // writing data to file
                                    output.write(data, 0, count);
                                }
                                // flushing output
                                output.flush();
                                // closing streams
                                output.close();
                                input.close();
                                isdownloading = 0;
                                Log.d("random", "Closed the stream");
                            } catch (Exception e) {
                                isdownloading = 0;
                                Log.e("Error: ", Objects.requireNonNull(e.getMessage()));
                            }
                        } else {
                            isdownloading = 0;
                            Log.d("random", "file exist");
                        }
                        //
                        //runOnUiThread(() -> Toast.makeText(NTP_Changer.this, responseCode + "\n" + location +"\n"+ finalImage_add+"\n"+savedir.toString()+"\n"+filename, Toast.LENGTH_SHORT).show());
                        Log.d("random", responseCode + "\n" + location + "\n" + finalImage_add + "\n" + savedir.toString() + "\n" + filename);
                        potato("IMG Cached.",Toast.LENGTH_SHORT);
                    } catch (Exception e) {
                        isdownloading = 0;
                        potato(e.toString(),Toast.LENGTH_SHORT);
                    }

                }).start();
            } else {
                isdownloading = 0;
                Log.d("random", "wifi not enable");
            }
        } catch (Exception e) {
            Toast.makeText(NTP_Changer.this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void per_check() {
        String[] per_inneed = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.WAKE_LOCK, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE};
        for (int per_i = 0; per_inneed.length > per_i; per_i++) {
            //hinting("Process","必要权限状态正在开始被检查，共:"+per_inneed.length+"项，还剩:"+per_i+"项\nPermission:"+per_inneed[per_i]);
            //Toast.makeText(this,,Toast.LENGTH_SHORT).show();
            if (checkSelfPermission(per_inneed[per_i]) != PackageManager.PERMISSION_GRANTED) {
                sdialog(new String[]{"权限检查", "检测到必要权限未被授权，正在申请。\n必要权限无法获取时程序将无法正常运行，\n如未被授权，\n下次启动应用时将会重新发起申请授权。\n权限:\n" + per_inneed[per_i]});
                requestPermissions(per_inneed, per_i);
            } else {
                Snackbar.make(getWindow().getDecorView(), "所需权限已被授权", 2000).show();
            }
        }

    }
    //Toast
    public void potato(String msg,int time) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(),msg,time).show());
    }

    //权限检查

    public void settings() {
        //创建一个DIALOG
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //设置标题
        builder.setTitle("Settings");
        //创建一个线性布局
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);


        // builder.setCancelable(false);
        View setting_view = View.inflate(getApplicationContext(), R.layout.settings, null);
        builder.setView(setting_view);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.show();
        //
        EditText ntp_interval = setting_view.findViewById(R.id.ntp_interval);
        EditText random_stat = setting_view.findViewById(R.id.random_stat);
        Switch random_enable = setting_view.findViewById(R.id.random_enable);
        Switch dialog_enable = setting_view.findViewById(R.id.dialog_enable);
        Switch ping_enable = setting_view.findViewById(R.id.ping_enable);
        Button apply = setting_view.findViewById(R.id.setap);
        try {
            String interval = String.valueOf(getSharedPreferences("ntp_interval", MODE_PRIVATE).getInt("ntp_interval", 500));
            boolean dialog = (getSharedPreferences("dialog_enable", MODE_PRIVATE).getBoolean("dialog_enable", false));
            String num = String.valueOf(getSharedPreferences("random_num", MODE_PRIVATE).getInt("random_num", 0));
            boolean enable = (getSharedPreferences("random_enable", MODE_PRIVATE).getBoolean("random_enable", true));
            boolean ping = (getSharedPreferences("ping_enable", MODE_PRIVATE).getBoolean("ping_enable", false));

            ntp_interval.setText(interval);
            dialog_enable.setChecked(dialog);
            random_stat.setText(num);
            random_enable.setChecked(enable);
            ping_enable.setChecked(ping);

            Log.d("Settings_Get", "num:" + num + "\n" + enable);
        } catch (Exception e) {
            Log.d("Settings_Error", e.toString());
        }
        apply.setOnClickListener(v -> {
            int interval = Integer.parseInt(ntp_interval.getText().toString());
            boolean dialog = dialog_enable.isChecked();
            int num = Integer.parseInt(random_stat.getText().toString());
            boolean enable = random_enable.isChecked();
            boolean ping = ping_enable.isChecked();

            SharedPreferences.Editor interval_editor = getSharedPreferences("ntp_interval", MODE_PRIVATE).edit();
            interval_editor.putInt("ntp_interval", interval);
            interval_editor.apply();
            global_interval = interval;

            SharedPreferences.Editor dialog_enable_editor = getSharedPreferences("dialog_enable", MODE_PRIVATE).edit();
            dialog_enable_editor.putBoolean("dialog_enable", dialog);
            dialog_enable_editor.apply();
            dialog_enable_status = dialog;

            SharedPreferences.Editor num_editor = getSharedPreferences("random_num", MODE_PRIVATE).edit();
            num_editor.putInt("random_num", num);
            num_editor.apply();

            SharedPreferences.Editor enable_editor = getSharedPreferences("random_enable", MODE_PRIVATE).edit();
            enable_editor.putBoolean("random_enable", enable);
            enable_editor.apply();

            SharedPreferences.Editor ping_enable_editor = getSharedPreferences("ping_enable", MODE_PRIVATE).edit();
            ping_enable_editor.putBoolean("ping_enable", ping);
            ping_enable_editor.apply();
            ping_enable_status = ping;


            Log.d("Settings", "num:" + num + "\n" + enable);

            if (((Switch) setting_view.findViewById(R.id.random_enable)).isChecked()) {
                Toast.makeText(getApplicationContext(), "qwqqq\n我又可以变得可爱了!!!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "qaqqq\n呜呜呜呜你居然把这么可爱的我给关掉了!!!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    class location extends Thread {
        String address = null;

        @Override
        public void run() {
            final URL url;
            HttpURLConnection httpURLConnection = null;
            StringBuilder content = new StringBuilder();
            try {

                url = new URL("https://ip.sb/ip/" + InetAddress.getByName(address.replace("\n", "")).getHostAddress());
                Log.d("LOCATION", url.toString());
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();
                int code = httpURLConnection.getResponseCode();
                if (code == 200) {
                    InputStream in = httpURLConnection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
                    BufferedReader reader = new BufferedReader(isr);
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line);
                    }
                } else {
                    Log.d("LOCATION", "ERROR CODE:" + code);
                }
                //Log.d("LOCATION",content.toString());
                Message message = Message.obtain();
                message.what = MSG_FROM_LOCATION;
                //字符串处理
                String contents = content.toString();
                String Filfer1 = "<td class=\"proto_location\">";
                String Filfer2 = "</td>";
                String image_add = contents.substring(contents.lastIndexOf(Filfer1) + Filfer1.length());
                String data = image_add.substring(0, image_add.indexOf(Filfer2)).trim().replace(" ", "").replace("\n", "");
                //String data=StringUtils.substringBefore(StringUtils.substringAfterLast(content.toString(), "<span style=\"display: inline-block;text-align: center;width: 720px;float: left;line-height: 46px;height: 46px;\">"),"</span>");
                message.obj = data;
                ntp_location = data;
                runOnUiThread(() -> location_ntp.setText(ntp_location));
                Looper.prepare();
                new Handler(main_handler).sendMessage(message);
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
