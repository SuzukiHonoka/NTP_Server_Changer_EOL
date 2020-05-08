package org.starx_software_lab.ntp_server_changer;

import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Ringtone_Changer extends AppCompatActivity {

    EditText ringtone_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main10);
        ringtone_uri = findViewById(R.id.set_ringtone_uri);
    }

    public void set_ringtone(View view) {
        if (Settings.System.canWrite(getApplicationContext())) {
            String TAG = "RINGTONE";
            String ab_music_path = ringtone_uri.getText().toString().trim();
            if (!ab_music_path.isEmpty()) {
                Uri uri = MediaStore.Audio.Media.getContentUriForPath(ab_music_path);
                assert uri != null;
                getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + ab_music_path + "\"", null);
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(ab_music_path);
                //标题
                String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                //歌手
                String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String mine_type = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DATA, ab_music_path);
                values.put(MediaStore.MediaColumns.TITLE, title);
                values.put(MediaStore.Audio.Media.ARTIST, artist);
                values.put(MediaStore.MediaColumns.MIME_TYPE, mine_type);
                values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
                values.put(MediaStore.Audio.Media.IS_ALARM, false);
                values.put(MediaStore.Audio.Media.IS_MUSIC, false);

                Uri newUri = getContentResolver().insert(uri, values);
                Log.d(TAG, "ab:" + ab_music_path + "\nURI:" + uri.toString() + "\nvalues:" + values.toString() + "\nNEWURI:" + newUri);
                RingtoneManager.setActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE, newUri);
                Toast.makeText(getApplicationContext(), "已设为铃声。", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "路径不能为空。", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "无法修改系统设置，\n正在打开权限设置界面..", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName())));
        }
    }
}
