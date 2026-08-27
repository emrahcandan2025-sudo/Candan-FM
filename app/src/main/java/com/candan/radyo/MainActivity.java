package com.candan.radyo;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

public class MainActivity extends Activity {

    private ListenableFuture<MediaController> controllerFuture;
    private MediaController controller;

    private TextView stationText;
    private TextView statusText;
    private LinearLayout radioList;

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

        requestNotificationPermission();

        createController();
        createInterface();
    }

    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= 33) {

            if (checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        100
                );
            }
        }
    }

    private void createController() {

        SessionToken token =
                new SessionToken(
                        this,
                        new ComponentName(
                                this,
                                RadioService.class
                        )
                );

        controllerFuture =
                new MediaController.Builder(
                        this,
                        token
                ).buildAsync();

        Executor executor =
                command -> runOnUiThread(command);

        controllerFuture.addListener(() -> {

            try {

                controller = controllerFuture.get();

            } catch (Exception ignored) {
            }

        }, executor);
    }

    private void createInterface() {

        LinearLayout main =
                new LinearLayout(this);

        main.setOrientation(
                LinearLayout.VERTICAL);

        main.setBackgroundColor(
                Color.rgb(245, 246, 248));

        main.setPadding(
                20, 30, 20, 40);


        TextView title =
                new TextView(this);

        title.setText("📻 Candan Radyo");
        title.setTextSize(28);
        title.setTypeface(
                null,
                Typeface.BOLD);

        title.setGravity(Gravity.CENTER);

        title.setTextColor(
                Color.rgb(17, 24, 39));

        title.setPadding(
                0, 10, 0, 10);

        main.addView(title);


        stationText =
                new TextView(this);

        stationText.setText("Radyo seç");
        stationText.setTextSize(18);

        stationText.setTypeface(
                null,
                Typeface.BOLD);

        stationText.setGravity(
                Gravity.CENTER);

        main.addView(stationText);


        statusText =
                new TextView(this);

        statusText.setText("Hazır");
        statusText.setTextSize(14);

        statusText.setGravity(
                Gravity.CENTER);

        statusText.setPadding(
                0, 5, 0, 15);

        main.addView(statusText);


        EditText search =
                new EditText(this);

        search.setHint("🔎 Radyo ara...");
        search.setSingleLine(true);

        main.addView(search);


        ScrollView scroll =
                new ScrollView(this);

        radioList =
                new LinearLayout(this);

        radioList.setOrientation(
                LinearLayout.VERTICAL);

        scroll.addView(radioList);


        LinearLayout.LayoutParams sp =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                );

        main.addView(scroll, sp);


        Button stop =
                new Button(this);

        stop.setText("■ Yayını Durdur");
        stop.setTextSize(16);
        stop.setAllCaps(false);

        stop.setOnClickListener(v -> {

            if (controller != null) {

                controller.stop();
                controller.clearMediaItems();

                stationText.setText(
                        "Radyo seç");

                statusText.setText(
                        "Hazır");
            }
        });

        main.addView(stop);

        showRadios("");

        search.addTextChangedListener(
                new android.text.TextWatcher() {

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

                        showRadios(
                                s.toString());
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s) {
                    }
                }
        );

        setContentView(main);
    }

    private void showRadios(String filter) {

        radioList.removeAllViews();

        String f =
                filter
                        .toLowerCase()
                        .replace("ı", "i");

        for (String[] radio : radios) {

            String name = radio[0];

            if (!name
                    .toLowerCase()
                    .replace("ı", "i")
                    .contains(f)) {

                continue;
            }

            createRow(
                    radio[0],
                    radio[1],
                    radio[2]);
        }
    }

    private void createRow(
            String name,
            String url,
            String letter) {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL);

        row.setGravity(
                Gravity.CENTER_VERTICAL);

        row.setPadding(
                15, 10, 10, 10);

        row.setBackgroundColor(
                Color.WHITE);


        LinearLayout.LayoutParams rp =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        110
                );

        rp.setMargins(
                0, 5, 0, 5);

        row.setLayoutParams(rp);


        TextView logo =
                new TextView(this);

        logo.setText(letter);
        logo.setTextSize(21);

        logo.setTypeface(
                null,
                Typeface.BOLD);

        logo.setTextColor(
                Color.WHITE);

        logo.setGravity(
                Gravity.CENTER);

        logo.setBackgroundColor(
                Color.rgb(220, 38, 38));


        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        72, 72);

        logo.setLayoutParams(lp);


        TextView nameText =
                new TextView(this);

        nameText.setText(name);
        nameText.setTextSize(18);

        nameText.setPadding(
                20, 0, 5, 0);


        LinearLayout.LayoutParams np =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                );

        nameText.setLayoutParams(np);

        nameText.setGravity(
                Gravity.CENTER_VERTICAL);


        Button star =
                new Button(this);

        star.setText(
                favorites.contains(name)
                        ? "★"
                        : "☆");

        star.setTextSize(27);

        star.setBackgroundColor(
                Color.TRANSPARENT);


        star.setOnClickListener(v -> {

            if (favorites.contains(name)) {

                favorites.remove(name);

                star.setText("☆");

            } else {

                favorites.add(name);

                star.setText("★");
            }
        });


        row.setOnClickListener(v ->
                playStation(
                        name,
                        url));


        logo.setOnClickListener(v ->
                playStation(
                        name,
                        url));


        nameText.setOnClickListener(v ->
                playStation(
                        name,
                        url));


        row.addView(logo);
        row.addView(nameText);
        row.addView(star);

        radioList.addView(row);
    }

    private void playStation(
            String name,
            String url) {

        if (controller == null) {

            statusText.setText(
                    "Oynatıcı hazırlanıyor...");

            return;
        }

        MediaMetadata metadata =
                new MediaMetadata.Builder()
                        .setTitle(name)
                        .setArtist("Candan Radyo")
                        .build();

        MediaItem item =
                new MediaItem.Builder()
                        .setUri(url)
                        .setMediaMetadata(metadata)
                        .build();

        controller.stop();
        controller.clearMediaItems();

        controller.setMediaItem(item);

        controller.prepare();
        controller.play();

        stationText.setText(name);

        statusText.setText(
                "🔴 CANLI");
    }

    @Override
    protected void onDestroy() {

        /*
         * ÖNEMLİ:
         * Burada radyoyu durdurmuyoruz.
         *
         * Activity kapansa bile RadioService
         * yayını sürdürmeye devam edecek.
         */

        if (controllerFuture != null) {

            MediaController.releaseFuture(
                    controllerFuture);
        }

        super.onDestroy();
    }
}
