package com.candan.radyo;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    private LinearLayout radioList;

    private String currentName = "";
    private String currentUrl = "";
    private int retryCount = 0;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Set<String> favorites = new HashSet<>();

    private final String[][] radios = {

            {"Süper FM",
             "https://playerservices.streamtheworld.com/api/livestream-redirect/SUPER_FM128AAC_SC",
             "S"},

            {"Metro FM",
             "https://playerservices.streamtheworld.com/api/livestream-redirect/METRO_FM128AAC_SC",
             "M"},

            {"JoyTürk",
             "https://playerservices.streamtheworld.com/api/livestream-redirect/JOY_TURKAAC_SC",
             "J"},

            {"Joy FM",
             "https://playerservices.streamtheworld.com/api/livestream-redirect/JOY_FM128AAC_SC",
             "J"},

            {"Virgin Radio Türkiye",
             "https://playerservices.streamtheworld.com/api/livestream-redirect/VIRGIN_RADIOAAC_SC",
             "V"},

            {"Kral Pop",
             "https://dygedge.radyotvonline.net/kralpop/playlist.m3u8",
             "K"},

            {"Kral FM",
             "https://dygedge.radyotvonline.net/kralfm/playlist.m3u8",
             "K"},

            {"PowerTürk",
             "https://listen.powerapp.com.tr/powerturk/mpeg/icecast.audio",
             "P"},

            {"Power FM",
             "https://listen.powerapp.com.tr/powerfm/mpeg/icecast.audio",
             "P"},

            {"Radyo Fenomen",
             "https://listen.radyofenomen.com/fenomen/128/icecast.audio",
             "F"},

            {"Fenomen Türk",
             "https://listen.radyofenomen.com/fenomenturk/128/icecast.audio",
             "F"},

            {"Number1 FM",
             "https://n10101m.mediatriple.net/numberone",
             "N"},

            {"Number1 Türk",
             "https://n10101m.mediatriple.net/numberoneturk",
             "N"},

            {"Number1 Türk Slow",
             "https://n10101m.mediatriple.net/numberoneturkslow",
             "N"},

            {"Number1 Türk 90'lar",
             "https://n10101m.mediatriple.net/numberoneturk90",
             "N"},

            {"Best FM",
             "https://bestfm.turkhosted.com/stream",
             "B"},

            {"Alem FM",
             "https://playerservices.streamtheworld.com/api/livestream-redirect/ALEM_FM128AAC_SC",
             "A"},

            {"Radyo D",
             "https://moondigitaledge.radyotvonline.net/radyod/playlist.m3u8",
             "D"},

            {"SlowTürk",
             "https://radyo.duhnet.tv/slowturk",
             "S"},

            {"Pal Nostalji",
             "https://shoutcast.radyogrup.com:1020/stream",
             "P"},

            {"Pal Station",
             "https://shoutcast.radyogrup.com:1010/stream",
             "P"},

            {"TRT FM",
             "https://radio-trtfm.live.trt.com.tr/master.m3u8",
             "T"},

            {"TRT Radyo 1",
             "https://radio-trtradyo1.live.trt.com.tr/master.m3u8",
             "T"},

            {"TRT Türkü",
             "https://radio-trtturku.live.trt.com.tr/master.m3u8",
             "T"},

            {"TRT Nağme",
             "https://radio-trtnagme.live.trt.com.tr/master.m3u8",
             "T"}
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
            public void onPlaybackStateChanged(int state) {

                if (state == Player.STATE_BUFFERING) {

                    statusText.setText("Bağlanıyor...");

                } else if (state == Player.STATE_READY &&
                           player.getPlayWhenReady()) {

                    statusText.setText("🔴 CANLI");
                    currentStationText.setText(currentName);
                    stopButton.setEnabled(true);

                    retryCount = 0;
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {

                if (retryCount < 2 && !currentUrl.isEmpty()) {

                    retryCount++;

                    statusText.setText("Bağlantı yenileniyor...");

                    handler.postDelayed(
                            () -> reconnect(),
                            1500
                    );

                } else {

                    statusText.setText("Yayın açılamadı");

                    stopButton.setEnabled(false);

                    Toast.makeText(
                            MainActivity.this,
                            currentName + " yayını açılamadı.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
    }

    private void createInterface() {

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setBackgroundColor(Color.rgb(245, 246, 248));
        main.setPadding(20, 30, 20, 35);

        TextView title = new TextView(this);
        title.setText("📻 Candan Radyo");
        title.setTextSize(27);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.rgb(17, 24, 39));
        title.setPadding(0, 10, 0, 12);

        main.addView(title);

        currentStationText = new TextView(this);
        currentStationText.setText("Radyo seç");
        currentStationText.setTextSize(18);
        currentStationText.setTypeface(null, Typeface.BOLD);
        currentStationText.setGravity(Gravity.CENTER);
        currentStationText.setTextColor(Color.rgb(55, 65, 81));

        main.addView(currentStationText);

        statusText = new TextView(this);
        statusText.setText("Hazır");
        statusText.setTextSize(14);
        statusText.setGravity(Gravity.CENTER);
        statusText.setTextColor(Color.rgb(107, 114, 128));
        statusText.setPadding(0, 4, 0, 12);

        main.addView(statusText);

        EditText search = new EditText(this);
        search.setHint("🔎 Radyo ara...");
        search.setSingleLine(true);
        search.setTextSize(16);
        search.setPadding(20, 5, 20, 5);

        LinearLayout.LayoutParams searchParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        100
                );

        searchParams.setMargins(0, 0, 0, 10);
        search.setLayoutParams(searchParams);

        main.addView(search);

        ScrollView scrollView = new ScrollView(this);

        radioList = new LinearLayout(this);
        radioList.setOrientation(LinearLayout.VERTICAL);

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
                        115
                );

        stopParams.setMargins(0, 12, 0, 25);
        stopButton.setLayoutParams(stopParams);

        stopButton.setOnClickListener(v -> stopRadio());

        main.addView(stopButton);

        showRadios("");

        search.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                showRadios(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        setContentView(main);
    }

    private void showRadios(String filter) {

        radioList.removeAllViews();

        String search =
                filter.toLowerCase()
                      .replace("ı", "i");

        for (String[] radio : radios) {

            String name = radio[0];
            String url = radio[1];
            String letter = radio[2];

            String searchable =
                    name.toLowerCase()
                        .replace("ı", "i");

            if (!searchable.contains(search)) {
                continue;
            }

            createRadioRow(name, url, letter);
        }
    }

    private void createRadioRow(
            String name,
            String url,
            String letter) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(15, 8, 8, 8);
        row.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        105
                );

        rowParams.setMargins(0, 4, 0, 4);
        row.setLayoutParams(rowParams);

        TextView logo = new TextView(this);
        logo.setText(letter);
        logo.setTextSize(21);
        logo.setTypeface(null, Typeface.BOLD);
        logo.setTextColor(Color.WHITE);
        logo.setGravity(Gravity.CENTER);
        logo.setBackgroundColor(Color.rgb(220, 38, 38));

        LinearLayout.LayoutParams logoParams =
                new LinearLayout.LayoutParams(72, 72);

        logo.setLayoutParams(logoParams);

        TextView nameText = new TextView(this);
        nameText.setText(name);
        nameText.setTextSize(18);
        nameText.setTextColor(Color.rgb(31, 41, 55));
        nameText.setGravity(Gravity.CENTER_VERTICAL);
        nameText.setPadding(20, 0, 5, 0);

        LinearLayout.LayoutParams nameParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                );

        nameText.setLayoutParams(nameParams);

        Button star = new Button(this);

        star.setText(
                favorites.contains(name) ? "★" : "☆"
        );

        star.setTextSize(27);
        star.setBackgroundColor(Color.TRANSPARENT);

        LinearLayout.LayoutParams starParams =
                new LinearLayout.LayoutParams(85, 85);

        star.setLayoutParams(starParams);

        star.setOnClickListener(v -> {

            if (favorites.contains(name)) {

                favorites.remove(name);
                star.setText("☆");

            } else {

                favorites.add(name);
                star.setText("★");
            }
        });

        View.OnClickListener play =
                v -> playRadio(name, url);

        row.setOnClickListener(play);
        logo.setOnClickListener(play);
        nameText.setOnClickListener(play);

        row.addView(logo);
        row.addView(nameText);
        row.addView(star);

        radioList.addView(row);
    }

    private void playRadio(
            String name,
            String url) {

        currentName = name;
        currentUrl = url;
        retryCount = 0;

        currentStationText.setText(name);
        statusText.setText("Bağlanıyor...");
        stopButton.setEnabled(false);

        player.stop();
        player.clearMediaItems();

        MediaItem item =
                MediaItem.fromUri(url);

        player.setMediaItem(item);
        player.prepare();
        player.play();
    }

    private void reconnect() {

        if (currentUrl.isEmpty()) {
            return;
        }

        player.stop();
        player.clearMediaItems();

        player.setMediaItem(
                MediaItem.fromUri(currentUrl)
        );

        player.prepare();
        player.play();
    }

    private void stopRadio() {

        player.stop();
        player.clearMediaItems();

        currentName = "";
        currentUrl = "";
        retryCount = 0;

        currentStationText.setText("Radyo seç");
        statusText.setText("Hazır");
        stopButton.setEnabled(false);
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
