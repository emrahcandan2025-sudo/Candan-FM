package com.candan.radyo;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends Activity {

    private MediaPlayer player;
    private TextView statusText;
    private Button stopButton;

    private final String[][] radios = {
            {"PowerTürk", "https://listen.powerapp.com.tr/powerturk/mpeg/icecast.audio"},
            {"Süper FM", "https://playerservices.streamtheworld.com/api/livestream-redirect/SUPER_FM.mp3"},
            {"Metro FM", "https://playerservices.streamtheworld.com/api/livestream-redirect/METRO_FM.mp3"},
            {"JoyTürk", "https://playerservices.streamtheworld.com/api/livestream-redirect/JOY_TURK.mp3"},
            {"Alem FM", "https://playerservices.streamtheworld.com/api/livestream-redirect/ALEM_FM.mp3"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(30, 40, 30, 30);
        main.setBackgroundColor(0xFFF3F4F6);

        TextView title = new TextView(this);
        title.setText("📻 Candan Radyo");
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(0xFF111827);
        title.setPadding(0, 20, 0, 20);
        main.addView(title);

        statusText = new TextView(this);
        statusText.setText("Bir radyo seç");
        statusText.setTextSize(17);
        statusText.setGravity(Gravity.CENTER);
        statusText.setTextColor(0xFF4B5563);
        statusText.setPadding(0, 10, 0, 25);
        main.addView(statusText);

        ScrollView scroll = new ScrollView(this);

        LinearLayout radioList = new LinearLayout(this);
        radioList.setOrientation(LinearLayout.VERTICAL);

        for (String[] radio : radios) {

            Button button = new Button(this);
            button.setText("▶  " + radio[0]);
            button.setTextSize(17);
            button.setAllCaps(false);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            params.setMargins(0, 8, 0, 8);
            button.setLayoutParams(params);

            button.setOnClickListener(v ->
                    playRadio(radio[0], radio[1]));

            radioList.addView(button);
        }

        scroll.addView(radioList);

        LinearLayout.LayoutParams scrollParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                );

        main.addView(scroll, scrollParams);

        stopButton = new Button(this);
        stopButton.setText("■  Yayını Durdur");
        stopButton.setTextSize(16);
        stopButton.setAllCaps(false);
        stopButton.setEnabled(false);

        stopButton.setOnClickListener(v -> stopRadio());

        main.addView(stopButton);

        setContentView(main);
    }

    private void playRadio(String name, String url) {

        stopRadio();

        statusText.setText(name + " bağlanıyor...");

        player = new MediaPlayer();

        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {

            player.setDataSource(url);

            player.setOnPreparedListener(mp -> {

                mp.start();

                statusText.setText("🔴 Canlı • " + name);

                stopButton.setEnabled(true);
            });

            player.setOnErrorListener((mp, what, extra) -> {

                statusText.setText("Yayın açılamadı");

                Toast.makeText(
                        this,
                        name + " yayınına bağlanılamadı.",
                        Toast.LENGTH_SHORT
                ).show();

                return true;
            });

            player.prepareAsync();

        } catch (IOException e) {

            statusText.setText("Yayın açılamadı");

            Toast.makeText(
                    this,
                    "Bağlantı hatası",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void stopRadio() {

        if (player != null) {

            try {

                if (player.isPlaying()) {
                    player.stop();
                }

            } catch (Exception ignored) {
            }

            player.release();
            player = null;
        }

        if (statusText != null) {
            statusText.setText("Bir radyo seç");
        }

        if (stopButton != null) {
            stopButton.setEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {

        stopRadio();

        super.onDestroy();
    }
          }
