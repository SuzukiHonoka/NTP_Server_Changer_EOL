package org.starx_software_lab.ntp_server_changer;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class Music_Player extends AppCompatActivity {

    static List<String> scandata = new ArrayList<>();
    static List<String> current_list = new ArrayList<>();
    final int MSG_FROM_SCAN = 1;
    Handler.Callback handler = msg -> {
        switch (msg.what) {
            case MSG_FROM_SCAN:

                break;
            default:
                break;
        }
        return false;
    };
    //
    ListView current_musics;
    TextView music_title, music_artist, music_album, music_op, music_ed, music_bitrate, music_album_artist;
    ImageView music_pic;
    SeekBar music_bar;
    Button music_play;
    //
    boolean isplaying = false;
    MediaPlayer mediaPlayer = null;
    Timer seek_timer;
    //
    String current_music_name = "";
    String next_music = "";
    String play_target = "";
    //
    int scanned_files_num = 0;
    int sw = 0;
    //0:播完即停 1:列表播放 2:随机播放 3:单曲循环
    int play_mode = 0;

    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main8);
        //初始化资源绑定
        init_wdts();
        //


//        ((EditText)findViewById(R.id.dirpath)).setText(Environment.getExternalStorageDirectory().getPath());

        music_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //TO-DO
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                kill_timer();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                try {
                    //如果播放已经结束，则重新播放
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                        music_play.setText("暂停");
                    }
                    mediaPlayer.seekTo(seekBar.getProgress());
                    getProgress();
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    public void ontouch(View view) {
        switch (view.getId()) {
            case R.id.music_scasn:
                //弹出一个选择路径的窗体
                AlertDialog.Builder builder_1 = new AlertDialog.Builder(Music_Player.this);
                builder_1.setTitle("Get ready to start the scan");
                //载入布局
                View scan_view = View.inflate(Music_Player.this, R.layout.scan_music, null);
                builder_1.setView(scan_view);

                //
                EditText dir = scan_view.findViewById(R.id.music_choose_dir);
                Button netease = scan_view.findViewById(R.id.uri_netease);
                Button download = scan_view.findViewById(R.id.uri_download);
                //
                //将本地存储路径赋值到编辑框
                dir.setText(Environment.getExternalStorageDirectory().getPath());
                //定义监听器
                netease.setOnClickListener(v -> {
                    String netease1 = Environment.getExternalStorageDirectory().getPath() + getString(R.string.netease);
                    dir.setText(netease1);
                });
                download.setOnClickListener(v -> {
                    String download1 = Environment.getExternalStorageDirectory().getPath() + getString(R.string.Download);
                    dir.setText(download1);
                });
                builder_1.setNegativeButton("确定", (dialog, which) -> {
                    scanned_files_num = 0;
                    Toast.makeText(getApplicationContext(), "Start to scan!", Toast.LENGTH_SHORT).show();
                    new Thread(() -> {
                        //String dir = ((EditText)findViewById(R.id.dirpath)).getText().toString();
                        //String source_dir= Environment.getExternalStorageDirectory().getPath()+"/netease";
                        String source_dir = dir.getText().toString();
                        getFileList(source_dir);
                        if (scandata.size() == 0) {
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Noting Found!", Toast.LENGTH_SHORT).show());
                            return;
                        }
                        runOnUiThread(() -> {
                            Toast.makeText(Music_Player.this, "Scanned Successfully!\nTotal Scanned Files Number:" + scanned_files_num, Toast.LENGTH_SHORT).show();
                            //refresh();
                            //
                            AlertDialog.Builder builder = new AlertDialog.Builder(Music_Player.this);
                            builder.setTitle("Scanned Music list");
                            View music_view = View.inflate(Music_Player.this, R.layout.scanned_list, null);
                            builder.setView(music_view);
                            builder.show();
                            //
                            ListView musics_item = music_view.findViewById(R.id.music_item_scanned);
                            Button confirm = music_view.findViewById(R.id.confirm_toadd);
                            //
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(Music_Player.this, android.R.layout.simple_list_item_1, scandata);
                            musics_item.setAdapter(arrayAdapter);
                            musics_item.setOnItemClickListener((parent, view1, position, id) -> {
                                String target = musics_item.getItemAtPosition(position).toString();
                                Toast.makeText(getApplicationContext(), "Setting target to \n" + target, Toast.LENGTH_SHORT).show();
                                play_target = target;
                            });
                            confirm.setOnClickListener(v -> {
                                current_list = scandata;
                                Toast.makeText(Music_Player.this, "已添加到列表。", Toast.LENGTH_SHORT).show();
                            });
                        });
                    }).start();
                });
                builder_1.show();

                break;
            case R.id.music_play:
                if (play_target.equals("")) {
                    Toast.makeText(getApplicationContext(), "请选择播放曲目。", Toast.LENGTH_SHORT).show();
                    break;
                }
                music_player(play_target, 0);
                break;
            case R.id.music_show_list:
                AlertDialog.Builder builder = new AlertDialog.Builder(Music_Player.this);
                builder.setTitle("Music list");
                View music_list_view = View.inflate(Music_Player.this, R.layout.musics_list, null);
                builder.setView(music_list_view);
                builder.show();
                //
                ListView musics_item = music_list_view.findViewById(R.id.music_item);
                Button play_by_list = music_list_view.findViewById(R.id.music_mode_bylist);
                //
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, current_list);
                musics_item.setAdapter(arrayAdapter);
                musics_item.setOnItemClickListener((parent, view1, position, id) -> {
                    String target = musics_item.getItemAtPosition(position).toString();
                    Toast.makeText(getApplicationContext(), "Setting target to \n" + target, Toast.LENGTH_SHORT).show();
                    play_target = target;
                });
                //监听按钮选中状态
                //play_by_list.setOnClickListener(v -> play_mode = 1);
                break;
            case R.id.music_title:
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Music_URI", current_music_name);
                assert cm != null;
                cm.setPrimaryClip(mClipData);
                Toast.makeText(getApplicationContext(), "已复制音乐文件路径到剪贴板。", Toast.LENGTH_SHORT).show();
                break;
            case R.id.music_image:
                new MaterialDialog.Builder(this)
                        .title("音乐信息")
                        .content("---")
                        .positiveText("OK")
                        .show();
            default:
                break;
        }


    }

    public void getFileList(String strPath) {
        Log.d("Scan", "Ready to scan!!!");
        File dir = new File(strPath);
        File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
        if (files != null) {
            ++scanned_files_num;
            for (File file : files) {
                String fileName = file.getName();
                Log.d("Scanning", fileName);
                if (file.isDirectory()) { // 判断是文件还是文件夹
                    getFileList(file.getAbsolutePath()); // 获取文件绝对路径
                } else if (fileName.endsWith("mp3") || fileName.endsWith("wav") || fileName.endsWith("flac")) { // 判断文件名是否以.mp3结尾
                    Log.d("Scan_Target_Found", file.getAbsolutePath());
                    if (!scandata.contains(file.getPath())) scandata.add(file.getPath());
                }
            }

        }
        Log.d("Scanned", String.valueOf(scandata.size()));
    }


    public void music_player(String path, int model) {
        final String TAG = "Player";
        //如果上一首播放歌曲与当前将要播放的歌曲一致，则暂停/继续播放，并更改按钮文字。
        if (path.equals(current_music_name)) {
            Log.d(TAG, path + "\n" + current_music_name);
            if (mediaPlayer.isPlaying()) {
                kill_timer();
                mediaPlayer.pause();
                music_play.setText("播放");
            } else {
                mediaPlayer.start();
                if (seek_timer == null) getProgress();
                music_play.setText("暂停");
            }
            return;
        }
        //如果与上一首歌曲不一致且还在播放则强制释放
        if (isplaying) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            isplaying = false;
            mediaPlayer = null;
            Log.d("Player", "已释放资源。");
        }

        try {
            //将上次播放的字符串变量定义为现在将要播放的字符串
            current_music_name = path;
            Toast.makeText(getApplicationContext(), path, Toast.LENGTH_SHORT).show();
            //创建一个新的媒体播放器实例
            if (mediaPlayer == null) mediaPlayer = new MediaPlayer();
            //为新的媒体播放器实例设置数据源
            mediaPlayer.setDataSource(path);
            //异步加载数据源(一般来说异步加载是用在网络资源上的。)
            mediaPlayer.prepareAsync();
            //定义一个加载监听器
            mediaPlayer.setOnPreparedListener(mp -> {
                //获取媒体的持续时间
                int duration = mediaPlayer.getDuration();
                //为进度条设定最大值
                music_bar.setMax(duration);
                music_ed.setText(formatTime(duration));
                //开始播放媒体音乐
                mediaPlayer.start();
                music_play.setText("暂停");
                //开始播放变量设为真
                isplaying = true;
                //持续更新进度条
                getProgress();
                get_current_music_info(path);
            });
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    private void getProgress() {
        String TAG = "Update_Seek";
        //创建一个定时器实例
        seek_timer = new Timer();
        //定义执行的任务
        seek_timer.schedule(new TimerTask() {

            @Override
            public void run() {
                //if(!mediaPlayer.isPlaying())kill_timer();
                //获取歌曲的进度
                int p = mediaPlayer.getCurrentPosition();
                Log.d(TAG, String.valueOf(p));
                Log.i(TAG, "CurrentThread ID:" + Thread.currentThread().getId());
                //将获取歌曲的进度赋值给seekbar
                //music_op.setText(TimerFormatter);
                music_bar.setProgress(p);
                //正在播放的标签
                runOnUiThread(() -> music_op.setText(formatTime(p)));
                //如果达到歌曲最大值，则开始休眠
                if (mediaPlayer.getDuration() == p) {
                    runOnUiThread(() -> music_play.setText("播放"));
                    kill_timer();
                    if (play_mode == 1) {
                        runOnUiThread(() -> {
                            Log.d("Next_song", "Index OF: " + current_list.indexOf(current_music_name) + "\nNext_name: " + current_list.get(current_list.indexOf(current_music_name) + 1));
                            music_player(current_list.get(current_list.indexOf(current_music_name) + 1), 0);
                        });
                    }
                }
            }
        }, 0, 300);
    }

    private void kill_timer() {
        if (seek_timer != null) {
            seek_timer.cancel();
            seek_timer.purge();
            seek_timer = null;
        }
    }

    private void init_wdts() {
        current_musics = findViewById(R.id.music_play_list);
        music_title = findViewById(R.id.music_title);
        music_artist = findViewById(R.id.music_artist);
        music_album = findViewById(R.id.music_album);
        music_op = findViewById(R.id.music_op);
        music_ed = findViewById(R.id.music_ed);
        music_pic = findViewById(R.id.music_image);
        music_bar = findViewById(R.id.music_seekBar);
        music_play = findViewById(R.id.music_play);
        music_bitrate = findViewById(R.id.music_bitrate);
        music_album_artist = findViewById(R.id.music_album_artist);
    }

    @SuppressLint("SetTextI18n")
    private void get_current_music_info(String path) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        //标题
        music_title.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE).trim());
        //歌手
        music_artist.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST).trim());
        //专辑
        music_album.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM).trim());
        int music_birate_format = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)) / 1000;
        music_bitrate.setText(music_birate_format + getResources().getString(R.string.k));
        music_album_artist.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST).trim());

        //专辑图
        byte[] embeddedPicture = mmr.getEmbeddedPicture();
        if (embeddedPicture != null && embeddedPicture.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(embeddedPicture, 0, embeddedPicture.length);
            music_pic.setImageBitmap(bitmap);
        } else {
            music_pic.setImageDrawable(getDrawable(R.mipmap.music_icon));
        }


    }

    private String formatTime(int length) {
        Date date = new Date(length);
        int ss = length / 1000;
        String min = String.valueOf(ss / 60);
        String sec = String.valueOf(ss % 60);
        if (Integer.parseInt(min) / 10 <= 0) {
            min = "0" + min;
        }
        if (Integer.parseInt(sec) / 10 <= 0) {
            sec = "0" + sec;
        }

//时间格式化工具
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss:SSSSS", Locale.getDefault());
        //return sdf.format(date);
        return min + ":" + sec;
    }


}
