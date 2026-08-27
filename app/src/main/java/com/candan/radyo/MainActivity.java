package com.candan.radyo;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity {

    private ExoPlayer player;
    private TextView statusText;
    private TextView currentStationText;
    private Button stopButton;

    private String currentName = "";
    private String currentUrl = "";

    private int retryCount = 0;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Set<String> favorites = new HashSet<>();

    private final String[][] radios = {

            {
                    "PowerTürk",
                    "https://listen.powerapp.com.tr/powerturk/mpeg/icecast.audio",
                    "P"
            },

            {
                    "Süper FM",
                    "https://playerservices.streamtheworld.com/api/livestream-redirect/SUPER_FM.mp3",
                    "S"
            },

            {
                    "Metro FM",
                    "https://playerservices.streamtheworld.com/api/livestream-redirect/METRO_FM.mp3",
                    "M"
            },

            {
                    "JoyTürk",
                    "https://playerservices.streamtheworld.com/api/livestream-redirect/JOY_TURK.mp3",
                    "J"
            },

            {
                    "Alem FM",
                    "https://playerservices.streamtheworld.com/api/livestream-redirect/ALEM_FM.mp3",
                    "A"
            }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createPlayer();
        createInterface();
    }

    private void createPlayer() {

        player = new ExoPlayer.Builder(this).build();

        player.addListener(new Player.Listener() {

            @Override
            public void onPlaybackStateChanged(int playbackState) {

                if (playbackState == Player.STATE_BUFFERING) {

                    statusText.setText("Bağlanıyor...");

                } else if (playbackState == Player.STATE_READY) {

                    if (player.getPlayWhenReady()) {

                        statusText.setText("🔴 CANLI");

                        currentStationText.setText(currentName);

                        stopButton.setEnabled(true);

                        retryCount = 0;
                    }

                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {

                if (retryCount < 2 && !currentUrl.isEmpty()) {

                    retryCount++;

                    statusText.setText("Bağlantı yenileniyor...");

                    handler.postDelayed(() -> {

                        reconnect();

                    }, 2000);

                } else {

                    statusText.setText("Yayın açılamadı");

                    Toast.makeText(
                            MainActivity.this,
                            currentName + " yayınına bağlanılamadı.",
                            Toast.LENGTH_SHORT
                    ).show();

                    stopButton.setEnabled(false);
                }
            }
        });
    }

    private void createInterface() {

        LinearLayout main = new LinearLayout(this);

        main.setOrientation(LinearLayout.VERTICAL);

        main.setBackgroundColor(Color.rgb(245, 246, 248));

        main.setPadding(20, 35, 20, 25);


        TextView title = new TextView(this);

        title.setText("📻  Candan Radyo");

        title.setTextSize(29);

        title.setTypeface(null, Typeface.BOLD);

        title.setTextColor(Color.rgb(17, 24, 39));

        title.setGravity(Gravity.CENTER);

        title.setPadding(0, 10, 0, 15);

        main.addView(title);


        currentStationText = new TextView(this);

        currentStationText.setText("Radyo seç");

        currentStationText.setTextSize(18);

        currentStationText.setTypeface(null, Typeface.BOLD);

        currentStationText.setTextColor(Color.rgb(55, 65, 81));

        currentStationText.setGravity(Gravity.CENTER);

        main.addView(currentStationText);


        statusText = new TextView(this);

        statusText.setText("Hazır");

        statusText.setTextSize(15);

        statusText.setTextColor(Color.rgb(107, 114, 128));

        statusText.setGravity(Gravity.CENTER);

        statusText.setPadding(0, 5, 0, 20);

        main.addView(statusText);


        ScrollView scrollView = new ScrollView(this);

        LinearLayout radioList = new LinearLayout(this);

        radioList.setOrientation(LinearLayout.VERTICAL);


        for (String[] radio : radios) {

            String name = radio[0];
            String url = radio[1];
            String logoLetter = radio[2];

            LinearLayout row = new LinearLayout(this);

            row.setOrientation(LinearLayout.HORIZONTAL);

            row.setGravity(Gravity.CENTER_VERTICAL);

            row.setPadding(15, 12, 10, 12);

            row.setBackgroundColor(Color.WHITE);


            LinearLayout.LayoutParams rowParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            105
                    );

            rowParams.setMargins(0, 5, 0, 5);

            row.setLayoutParams(rowParams);


            TextView logo = new TextView(this);

            logo.setText(logoLetter);

            logo.setTextSize(22);

            logo.setTypeface(null, Typeface.BOLD);

            logo.setTextColor(Color.WHITE);

            logo.setGravity(Gravity.CENTER);

            logo.setBackgroundColor(Color.rgb(220, 38, 38));


            LinearLayout.LayoutParams logoParams =
                    new LinearLayout.LayoutParams(72, 72);

            logo.setLayoutParams(logoParams);


            TextView nameText = new TextView(this);

            nameText.setText(name);

            nameText.setTextSize(19);

            nameText.setTextColor(Color.rgb(31, 41, 55));

            nameText.setGravity(Gravity.CENTER_VERTICAL);

            nameText.setPadding(20, 0, 10, 0);


            LinearLayout.LayoutParams nameParams =
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1
                    );

            nameText.setLayoutParams(nameParams);


            Button favorite = new Button(this);

            favorite.setText("☆");

            favorite.setTextSize(28);

            favorite.setAllCaps(false);

            favorite.setBackgroundColor(Color.TRANSPARENT);


            LinearLayout.LayoutParams starParams =
                    new LinearLayout.LayoutParams(85, 85);

            favorite.setLayoutParams(starParams);


            favorite.setOnClickListener(v -> {

                if (favorites.contains(name)) {

                    favorites.remove(name);

                    favorite.setText("☆");

                } else {

                    favorites.add(name);

                    favorite.setText("★");
                }
            });


            row.setOnClickListener(v -> {

                playRadio(name, url);

            });


            nameText.setOnClickListener(v -> {

                playRadio(name, url);

            });


            logo.setOnClickListener(v -> {

                playRadio(name, url);

            });


            row.addView(logo);

            row.addView(nameText);

            row.addView(favorite);

            radioList.addView(row);
        }


        scrollView.addView(radioList);


        LinearLayout.LayoutParams scrollParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                );

        main.addView(scrollView, scrollParams);


        stopButton = new Button(this);

        stopButton.setText("■  Yayını Durdur");

        stopButton.setTextSize(16);

        stopButton.setAllCaps(false);

        stopButton.setEnabled(false);


        LinearLayout.LayoutParams stopParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        120
                );

        stopParams.setMargins(0, 15, 0, 25);

        stopButton.setLayoutParams(stopParams);


        stopButton.setOnClickListener(v -> {

            stopRadio();

        });


        main.addView(stopButton);

        setContentView(main);
    }

    private void playRadio(String name, String url) {

        currentName = name;

        currentUrl = url;

        retryCount = 0;

        currentStationText.setText(name);

        statusText.setText("Bağlanıyor...");

        stopButton.setEnabled(false);

        player.stop();

        player.clearMediaItems();

        MediaItem mediaItem = MediaItem.fromUri(url);

        player.setMediaItem(mediaItem);

        player.prepare();

        player.play();
    }

    private void reconnect() {

        if (currentUrl.isEmpty()) {
            return;
        }

        player.stop();

        player.clearMediaItems();

        MediaItem mediaItem =
                MediaItem.fromUri(currentUrl);

        player.setMediaItem(mediaItem);

        player.prepare();

        player.play();
    }

    private void stopRadio() {

        if (player != null) {

            player.stop();

            player.clearMediaItems();
        }

        currentStationText.setText("Radyo seç");

        statusText.setText("Hazır");

        stopButton.setEnabled(false);

        currentName = "";

        currentUrl = "";

        retryCount = 0;
    }

    @Override
    protected void onDestroy() {

        handler.removeCallbacksAndMessages(null);

        if (player != null) {

            player.release();

            player = null;
        }

        super.onDestroy();
    }
}
