package org.starx_software_lab.ntp_server_changer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class SoundCTRL extends AppCompatActivity {
    TextView music_vol_percent,call_vol_percent,ring_vol_percent,notification_vol_percent,alarm_vol_percent,accessibility_vol_percent,dtmf_vol_percent,system_vol_percent;
    SeekBar music_vol_seekbar,call_vol_seekbar,ring_vol_seekbar,notification_vol_seekbar,alarm_vol_seekbar,accessibility_vol_seekbar,dtmf_vol_seekbar,system_vol_seekbar;
    AudioManager audioManager;
    //music
    int max_music_vol;
    int current_music_vol;
    //call
    int max_call_vol;
    int current_call_vol;
    //ring
    int max_ring_vol;
    int current_ring_vol;
    //notification
    int max_notification_vol;
    int current_notification_vol;
    //alarm
    int max_alarm_vol;
    int current_alarm_vol;
    //accessibility
    int max_accessibility_vol;
    int current_accessibility_vol;
    //dtmf
    int max_dtmf_vol;
    int current_dtmf_vol;
    //system
    int max_system_vol;
    int current_system_vol;




    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_c_t_r_l);
        findview();
        getaudiom();
        setup_vars();
        set_cvol();

        music_vol_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int start = 0;
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);
                current_music_vol = progress;
                music_vol_percent.setText(vol_percent(max_music_vol, progress) + getString(R.string.percent));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                start = seekBar.getProgress();
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getApplicationContext(),"Music VOL From " + start + " Changed to " + seekBar.getProgress(),Toast.LENGTH_SHORT).show();
            }
        });

        call_vol_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int start = 0;
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,progress,0);
                current_call_vol = progress;
                call_vol_percent.setText(vol_percent(max_call_vol, progress) + getString(R.string.percent));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                start = seekBar.getProgress();
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getApplicationContext(),"Call VOL From " + start + " Changed to " + seekBar.getProgress(),Toast.LENGTH_SHORT).show();
            }
        });

        ring_vol_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int start = 0;
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_RING,progress,0);
                current_ring_vol = progress;
                ring_vol_percent.setText(vol_percent(max_ring_vol, progress) + getString(R.string.percent));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                start = seekBar.getProgress();
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getApplicationContext(),"Ring VOL From " + start + " Changed to " + seekBar.getProgress(),Toast.LENGTH_SHORT).show();
            }
        });

        notification_vol_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int start = 0;
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,progress,0);
                current_notification_vol = progress;
                notification_vol_percent.setText(vol_percent(max_notification_vol, progress) + getString(R.string.percent));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                start = seekBar.getProgress();
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getApplicationContext(),"Notification VOL From " + start + " Changed to " + seekBar.getProgress(),Toast.LENGTH_SHORT).show();
            }
        });

        alarm_vol_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int start = 0;
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM,progress,0);
                current_alarm_vol = progress;
                alarm_vol_percent.setText(vol_percent(max_alarm_vol, progress) + getString(R.string.percent));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                start = seekBar.getProgress();
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getApplicationContext(),"Alarm VOL From " + start + " Changed to " + seekBar.getProgress(),Toast.LENGTH_SHORT).show();
            }
        });

        accessibility_vol_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int start = 0;
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_ACCESSIBILITY,progress,0);
                current_accessibility_vol = progress;
                accessibility_vol_percent.setText(vol_percent(max_accessibility_vol, progress) + getString(R.string.percent));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                start = seekBar.getProgress();
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getApplicationContext(),"Accessibility VOL From " + start + " Changed to " + seekBar.getProgress(),Toast.LENGTH_SHORT).show();
            }
        });

        dtmf_vol_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int start = 0;
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_DTMF,progress,0);
                current_dtmf_vol = progress;
                dtmf_vol_percent.setText(vol_percent(max_dtmf_vol, progress) + getString(R.string.percent));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                start = seekBar.getProgress();
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getApplicationContext(),"DTMF VOL From " + start + " Changed to " + seekBar.getProgress(),Toast.LENGTH_SHORT).show();
            }
        });

        system_vol_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int start = 0;
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,progress,0);
                current_system_vol = progress;
                system_vol_percent.setText(vol_percent(max_system_vol, progress) + getString(R.string.percent));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                start = seekBar.getProgress();
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getApplicationContext(),"System VOL From " + start + " Changed to " + seekBar.getProgress(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void findview() {
        //
        music_vol_percent = findViewById(R.id.music_vol_percent);
        music_vol_seekbar = findViewById(R.id.music_vol_seekbar);
        //
        call_vol_percent = findViewById(R.id.call_vol_percent);
        call_vol_seekbar = findViewById(R.id.call_vol_seekbar);
        //
        ring_vol_percent = findViewById(R.id.ring_vol_percent);
        ring_vol_seekbar = findViewById(R.id.ring_vol_seekbar);
        //
        notification_vol_percent = findViewById(R.id.notification_vol_percent);
        notification_vol_seekbar = findViewById(R.id.notification_vol_seekbar);
        //
        alarm_vol_percent = findViewById(R.id.alarm_vol_percent);
        alarm_vol_seekbar = findViewById(R.id.alarm_vol_seekbar);
        //
        accessibility_vol_percent = findViewById(R.id.accessibility_vol_percent);
        accessibility_vol_seekbar = findViewById(R.id.accessibility_vol_seekbar);
        //
        dtmf_vol_percent = findViewById(R.id.dtmf_vol_percent);
        dtmf_vol_seekbar = findViewById(R.id.dtmf_vol_seekbar);
        //
        system_vol_percent = findViewById(R.id.system_vol_percent);
        system_vol_seekbar = findViewById(R.id.system_vol_seekbar);
    }

    public void getaudiom() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        assert audioManager != null;
    }

    public int vol_percent(int max,int current) {
        return (int)((float)current/(float)max*100);
    }

    public void setup_vars() {
        //
        max_music_vol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        current_music_vol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        //
        max_call_vol = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        current_call_vol = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        //
        max_ring_vol = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        current_ring_vol = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        //
        max_notification_vol = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        current_notification_vol = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        //
        max_alarm_vol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        current_alarm_vol = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        //
        max_accessibility_vol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ACCESSIBILITY);
        current_accessibility_vol = audioManager.getStreamVolume(AudioManager.STREAM_ACCESSIBILITY);
        //
        max_dtmf_vol = audioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF);
        current_dtmf_vol = audioManager.getStreamVolume(AudioManager.STREAM_DTMF);
        //
        max_system_vol = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        current_system_vol = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
    }

    @SuppressLint("SetTextI18n")
    public void set_cvol() {
        //
        music_vol_percent.setText(vol_percent(max_music_vol, current_music_vol) + getString(R.string.percent));
        music_vol_seekbar.setMax(max_music_vol);
        music_vol_seekbar.setProgress(current_music_vol);
        //
        call_vol_percent.setText(vol_percent(max_call_vol, current_call_vol) + getString(R.string.percent));
        call_vol_seekbar.setMax(max_call_vol);
        call_vol_seekbar.setProgress(current_call_vol);
        //
        ring_vol_percent.setText(vol_percent(max_ring_vol, current_ring_vol) + getString(R.string.percent));
        ring_vol_seekbar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_RING));
        ring_vol_seekbar.setProgress(current_ring_vol);
        //
        notification_vol_percent.setText(vol_percent(max_notification_vol, current_notification_vol) + getString(R.string.percent));
        notification_vol_seekbar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
        notification_vol_seekbar.setProgress(current_notification_vol);
        //
        alarm_vol_percent.setText(vol_percent(max_alarm_vol, current_alarm_vol) + getString(R.string.percent));
        alarm_vol_seekbar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
        alarm_vol_seekbar.setProgress(current_alarm_vol);
        //
        accessibility_vol_percent.setText(vol_percent(max_accessibility_vol, current_accessibility_vol) + getString(R.string.percent));
        accessibility_vol_seekbar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_ACCESSIBILITY));
        accessibility_vol_seekbar.setProgress(current_accessibility_vol);
        //
        dtmf_vol_percent.setText(vol_percent(max_dtmf_vol, current_dtmf_vol) + getString(R.string.percent));
        dtmf_vol_seekbar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF));
        dtmf_vol_seekbar.setProgress(current_dtmf_vol);
        //
        system_vol_percent.setText(vol_percent(max_system_vol, current_system_vol) + getString(R.string.percent));
        system_vol_seekbar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
        system_vol_seekbar.setProgress(current_system_vol);
    }
}
