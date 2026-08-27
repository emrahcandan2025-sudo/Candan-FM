package com.candan.radyo;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
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
    private TextView liveText;
    private LinearLayout radioList;

    private SharedPreferences prefs;
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

        prefs = getSharedPreferences(
                "candan_radyo",
                MODE_PRIVATE
        );

        favorites.addAll(
                prefs.getStringSet(
                        "favorites",
                        new HashSet<>()
                )
        );

        requestNotificationPermission();
        createController();
        createInterface();
    }

    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(
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

                if (controller.getCurrentMediaItem() != null) {

                    MediaMetadata metadata =
                            controller
                                    .getCurrentMediaItem()
                                    .mediaMetadata;

                    if (metadata.title != null) {

                        stationText.setText(
                                metadata.title.toString()
                        );

                        liveText.setText(
                                "●  CANLI YAYIN"
                        );
                    }
                }

            } catch (Exception ignored) {
            }

        }, executor);
    }

    private void createInterface() {

        LinearLayout main =
                new LinearLayout(this);

        main.setOrientation(
                LinearLayout.VERTICAL
        );

        main.setBackgroundColor(
                Color.rgb(244, 246, 249)
        );

        main.setPadding(
                dp(16),
                dp(18),
                dp(16),
                dp(18)
        );


        TextView title =
                new TextView(this);

        title.setText("📻  Candan Radyo");

        title.setTextSize(28);

        title.setTypeface(
                null,
                Typeface.BOLD
        );

        title.setTextColor(
                Color.rgb(17, 24, 39)
        );

        title.setGravity(
                Gravity.CENTER
        );

        main.addView(title);


        LinearLayout playerCard =
                new LinearLayout(this);

        playerCard.setOrientation(
                LinearLayout.VERTICAL
        );

        playerCard.setGravity(
                Gravity.CENTER
        );

        playerCard.setPadding(
                dp(16),
                dp(14),
                dp(16),
                dp(14)
        );

        GradientDrawable playerBg =
                new GradientDrawable();

        playerBg.setColor(
                Color.WHITE
        );

        playerBg.setCornerRadius(
                dp(18)
        );

        playerCard.setBackground(
                playerBg
        );


        LinearLayout.LayoutParams playerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        playerParams.setMargins(
                0,
                dp(14),
                0,
                dp(14)
        );

        playerCard.setLayoutParams(
                playerParams
        );


        stationText =
                new TextView(this);

        stationText.setText(
                "Bir radyo seç"
        );

        stationText.setTextSize(
                21
        );

        stationText.setTypeface(
                null,
                Typeface.BOLD
        );

        stationText.setTextColor(
                Color.rgb(31, 41, 55)
        );

        stationText.setGravity(
                Gravity.CENTER
        );

        playerCard.addView(
                stationText
        );


        liveText =
                new TextView(this);

        liveText.setText(
                "Hazır"
        );

        liveText.setTextSize(
                14
        );

        liveText.setTextColor(
                Color.rgb(220, 38, 38)
        );

        liveText.setGravity(
                Gravity.CENTER
        );

        liveText.setPadding(
                0,
                dp(5),
                0,
                0
        );

        playerCard.addView(
                liveText
        );

        main.addView(
                playerCard
        );


        EditText search =
                new EditText(this);

        search.setHint(
                "Radyo ara..."
        );

        search.setTextSize(
                16
        );

        search.setSingleLine(
                true
        );

        search.setPadding(
                dp(18),
                0,
                dp(18),
                0
        );

        GradientDrawable searchBg =
                new GradientDrawable();

        searchBg.setColor(
                Color.WHITE
        );

        searchBg.setCornerRadius(
                dp(16)
        );

        searchBg.setStroke(
                dp(1),
                Color.rgb(225, 229, 235)
        );

        search.setBackground(
                searchBg
        );


        LinearLayout.LayoutParams searchParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(52)
                );

        searchParams.setMargins(
                0,
                0,
                0,
                dp(10)
        );

        search.setLayoutParams(
                searchParams
        );

        main.addView(
                search
        );


        ScrollView scroll =
                new ScrollView(this);

        scroll.setFillViewport(
                true
        );

        radioList =
                new LinearLayout(this);

        radioList.setOrientation(
                LinearLayout.VERTICAL
        );

        scroll.addView(
                radioList
        );


        LinearLayout.LayoutParams scrollParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                );

        main.addView(
                scroll,
                scrollParams
        );


        Button stopButton =
                new Button(this);

        stopButton.setText(
                "■   YAYINI DURDUR"
        );

        stopButton.setTextSize(
                15
        );

        stopButton.setTypeface(
                null,
                Typeface.BOLD
        );

        stopButton.setTextColor(
                Color.WHITE
        );

        stopButton.setAllCaps(
                false
        );

        GradientDrawable stopBg =
                new GradientDrawable();

        stopBg.setColor(
                Color.rgb(31, 41, 55)
        );

        stopBg.setCornerRadius(
                dp(16)
        );

        stopButton.setBackground(
                stopBg
        );


        LinearLayout.LayoutParams stopParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(54)
                );

        stopParams.setMargins(
                0,
                dp(10),
                0,
                dp(14)
        );

        stopButton.setLayoutParams(
                stopParams
        );


        stopButton.setOnClickListener(v -> {

            if (controller != null) {

                controller.stop();

                controller.clearMediaItems();

                stationText.setText(
                        "Bir radyo seç"
                );

                liveText.setText(
                        "Hazır"
                );
            }
        });

        main.addView(
                stopButton
        );

        showRadios("");

        search.addTextChangedListener(
                new TextWatcher() {

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
                                s.toString()
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );

        setContentView(
                main
        );
    }

    private void showRadios(
            String filter) {

        radioList.removeAllViews();

        String f =
                filter
                        .toLowerCase()
                        .replace("ı", "i");

        for (String[] radio : radios) {

            String name =
                    radio[0];

            String searchable =
                    name
                            .toLowerCase()
                            .replace("ı", "i");

            if (!searchable.contains(f)) {
                continue;
            }

            createRadioRow(
                    radio[0],
                    radio[1],
                    radio[2]
            );
        }
    }

    private void createRadioRow(
            String name,
            String url,
            String letter) {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        row.setPadding(
                dp(12),
                dp(8),
                dp(8),
                dp(8)
        );


        GradientDrawable rowBg =
                new GradientDrawable();

        rowBg.setColor(
                Color.WHITE
        );

        rowBg.setCornerRadius(
                dp(15)
        );

        row.setBackground(
                rowBg
        );


        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(68)
                );

        rowParams.setMargins(
                0,
                dp(4),
                0,
                dp(4)
        );

        row.setLayoutParams(
                rowParams
        );


        TextView logo =
                new TextView(this);

        logo.setText(
                letter
        );

        logo.setTextSize(
                19
        );

        logo.setTypeface(
                null,
                Typeface.BOLD
        );

        logo.setTextColor(
                Color.WHITE
        );

        logo.setGravity(
                Gravity.CENTER
        );


        GradientDrawable logoBg =
                new GradientDrawable();

        logoBg.setColor(
                Color.rgb(220, 38, 38)
        );

        logoBg.setCornerRadius(
                dp(12)
        );

        logo.setBackground(
                logoBg
        );


        LinearLayout.LayoutParams logoParams =
                new LinearLayout.LayoutParams(
                        dp(46),
                        dp(46)
                );

        logo.setLayoutParams(
                logoParams
        );


        TextView nameText =
                new TextView(this);

        nameText.setText(
                name
        );

        nameText.setTextSize(
                17
        );

        nameText.setTextColor(
                Color.rgb(31, 41, 55)
        );

        nameText.setGravity(
                Gravity.CENTER_VERTICAL
        );

        nameText.setPadding(
                dp(14),
                0,
                dp(5),
                0
        );


        LinearLayout.LayoutParams nameParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                );

        nameText.setLayoutParams(
                nameParams
        );


        TextView star =
                new TextView(this);

        star.setText(
                favorites.contains(name)
                        ? "★"
                        : "☆"
        );

        star.setTextSize(
                30
        );

        star.setGravity(
                Gravity.CENTER
        );

        star.setTextColor(
                favorites.contains(name)
                        ? Color.rgb(245, 158, 11)
                        : Color.rgb(156, 163, 175)
        );


        LinearLayout.LayoutParams starParams =
                new LinearLayout.LayoutParams(
                        dp(52),
                        dp(52)
                );

        star.setLayoutParams(
                starParams
        );


        star.setOnClickListener(v -> {

            if (favorites.contains(name)) {

                favorites.remove(name);

                star.setText("☆");

                star.setTextColor(
                        Color.rgb(
                                156,
                                163,
                                175
                        )
                );

            } else {

                favorites.add(name);

                star.setText("★");

                star.setTextColor(
                        Color.rgb(
                                245,
                                158,
                                11
                        )
                );
            }

            prefs.edit()
                    .putStringSet(
                            "favorites",
                            new HashSet<>(
                                    favorites
                            )
                    )
                    .apply();
        });


        View.OnClickListener play =
                v -> playStation(
                        name,
                        url
                );

        row.setOnClickListener(
                play
        );

        logo.setOnClickListener(
                play
        );

        nameText.setOnClickListener(
                play
        );


        row.addView(
                logo
        );

        row.addView(
                nameText
        );

        row.addView(
                star
        );

        radioList.addView(
                row
        );
    }

    private void playStation(
            String name,
            String url) {

        if (controller == null) {

            liveText.setText(
                    "Oynatıcı hazırlanıyor..."
            );

            return;
        }

        MediaMetadata metadata =
                new MediaMetadata.Builder()
                        .setTitle(name)
                        .setArtist(
                                "Candan Radyo"
                        )
                        .build();

        MediaItem item =
                new MediaItem.Builder()
                        .setUri(url)
                        .setMediaMetadata(
                                metadata
                        )
                        .build();

        controller.stop();

        controller.clearMediaItems();

        controller.setMediaItem(
                item
        );

        controller.prepare();

        controller.play();

        stationText.setText(
                name
        );

        liveText.setText(
                "●  CANLI YAYIN"
        );
    }

    private int dp(int value) {

        return (int) (
                value *
                getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    @Override
    protected void onDestroy() {

        if (controllerFuture != null) {

            MediaController.releaseFuture(
                    controllerFuture
            );
        }

        super.onDestroy();
    }
    }
