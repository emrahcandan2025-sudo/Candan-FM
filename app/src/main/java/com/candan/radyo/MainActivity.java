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

    private final int RED = Color.rgb(218, 30, 40);
    private final int DARK_RED = Color.rgb(180, 20, 30);
    private final int SOFT_BG = Color.rgb(248, 248, 248);

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
                    "http://20043.live.streamtheworld.com/NUMBER1FMAAC.aac",
                    "N"},

            {"Number1 Türk",
                    "http://19643.live.streamtheworld.com/NUMBER1TURK_FMAAC.aac",
                    "N"},

            {"Number1 Türk Slow",
                    "http://playerservices.streamtheworld.com/api/livestream-redirect/NUMBER1TURK_SLOWAAC.aac",
                    "N"},

            {"Number1 Türk 90'lar",
                    "http://playerservices.streamtheworld.com/api/livestream-redirect/NUMBER1TURK_90LARAAC.aac",
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

            {"TRT Radyo 3",
                    "https://radio-trtradyo3.live.trt.com.tr/master.m3u8",
                    "T"},

            {"TRT Türkü",
                    "https://radio-trtturku.live.trt.com.tr/master.m3u8",
                    "T"},

            {"TRT Nağme",
                    "https://radio-trtnagme.live.trt.com.tr/master.m3u8",
                    "T"},

            {"TRT Radyo Haber",
                    "https://radio-trtradyohaber.live.trt.com.tr/master.m3u8",
                    "T"},

            {"TRT Antalya",
                    "https://radio-trtantalya.live.trt.com.tr/master.m3u8",
                    "T"},

            {"TRT Çukurova",
                    "https://radio-trtcukurova.live.trt.com.tr/master.m3u8",
                    "T"},

            {"TRT GAP Diyarbakır",
                    "https://radio-trtgap.live.trt.com.tr/master.m3u8",
                    "T"},

            {"TRT Erzurum",
                    "https://radio-trterzurum.live.trt.com.tr/master.m3u8",
                    "T"},

            {"TRT Trabzon",
                    "https://radio-trttrabzon.live.trt.com.tr/master.m3u8",
                    "T"},

            {"Memleketim FM",
                    "https://radio-memleketimfm.live.trt.com.tr/master.m3u8",
                    "M"}
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

                controller =
                        controllerFuture.get();

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
                                "● CANLI YAYIN"
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
                SOFT_BG
        );

        main.setPadding(
                dp(12),
                dp(12),
                dp(12),
                dp(16)
        );


        TextView title =
                new TextView(this);

        title.setText(
                "📻  CANDAN RADYO"
        );

        title.setTextSize(
                27
        );

        title.setTypeface(
                null,
                Typeface.BOLD
        );

        title.setTextColor(
                Color.WHITE
        );

        title.setGravity(
                Gravity.CENTER
        );

        title.setPadding(
                dp(10),
                dp(14),
                dp(10),
                dp(14)
        );


        GradientDrawable titleBg =
                new GradientDrawable();

        titleBg.setColor(
                RED
        );

        titleBg.setCornerRadius(
                dp(18)
        );

        title.setBackground(
                titleBg
        );

        main.addView(
                title
        );


        LinearLayout nowPlaying =
                new LinearLayout(this);

        nowPlaying.setOrientation(
                LinearLayout.VERTICAL
        );

        nowPlaying.setGravity(
                Gravity.CENTER
        );

        nowPlaying.setPadding(
                dp(10),
                dp(9),
                dp(10),
                dp(9)
        );


        GradientDrawable nowBg =
                new GradientDrawable();

        nowBg.setColor(
                Color.WHITE
        );

        nowBg.setStroke(
                dp(1),
                RED
        );

        nowBg.setCornerRadius(
                dp(14)
        );

        nowPlaying.setBackground(
                nowBg
        );


        LinearLayout.LayoutParams nowParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        nowParams.setMargins(
                0,
                dp(8),
                0,
                dp(8)
        );

        nowPlaying.setLayoutParams(
                nowParams
        );


        stationText =
                new TextView(this);

        stationText.setText(
                "Bir radyo seç"
        );

        stationText.setTextSize(
                19
        );

        stationText.setTypeface(
                null,
                Typeface.BOLD
        );

        stationText.setTextColor(
                Color.rgb(35, 35, 35)
        );

        stationText.setGravity(
                Gravity.CENTER
        );

        nowPlaying.addView(
                stationText
        );


        liveText =
                new TextView(this);

        liveText.setText(
                "Hazır"
        );

        liveText.setTextSize(
                13
        );

        liveText.setTextColor(
                RED
        );

        liveText.setGravity(
                Gravity.CENTER
        );

        liveText.setPadding(
                0,
                dp(3),
                0,
                0
        );

        nowPlaying.addView(
                liveText
        );

        main.addView(
                nowPlaying
        );


        EditText search =
                new EditText(this);

        search.setHint(
                "🔎 Radyo ara..."
        );

        search.setTextSize(
                16
        );

        search.setSingleLine(
                true
        );

        search.setPadding(
                dp(15),
                0,
                dp(15),
                0
        );


        GradientDrawable searchBg =
                new GradientDrawable();

        searchBg.setColor(
                Color.WHITE
        );

        searchBg.setStroke(
                dp(1),
                Color.rgb(220, 220, 220)
        );

        searchBg.setCornerRadius(
                dp(12)
        );

        search.setBackground(
                searchBg
        );


        LinearLayout.LayoutParams searchParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(48)
                );

        searchParams.setMargins(
                0,
                0,
                0,
                dp(7)
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


        TextView stop =
                new TextView(this);

        stop.setText(
                "■  YAYINI DURDUR"
        );

        stop.setTextSize(
                15
        );

        stop.setTypeface(
                null,
                Typeface.BOLD
        );

        stop.setTextColor(
                Color.WHITE
        );

        stop.setGravity(
                Gravity.CENTER
        );


        GradientDrawable stopBg =
                new GradientDrawable();

        stopBg.setColor(
                DARK_RED
        );

        stopBg.setCornerRadius(
                dp(14)
        );

        stop.setBackground(
                stopBg
        );


        LinearLayout.LayoutParams stopParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(50)
                );

        stopParams.setMargins(
                0,
                dp(7),
                0,
                dp(30)
        );

        stop.setLayoutParams(
                stopParams
        );


        stop.setOnClickListener(v -> {

            if (controller != null) {

                controller.stop();
                controller.clearMediaItems();

                stationText.setText(
                        "Bir radyo seç"
                );

                liveText.setText(
                        "Hazır"
                );

                showRadios(
                        search.getText().toString()
                );
            }
        });

        main.addView(
                stop
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

        boolean isCurrent = false;

        if (controller != null &&
                controller.getCurrentMediaItem() != null &&
                controller
                        .getCurrentMediaItem()
                        .mediaMetadata
                        .title != null) {

            isCurrent =
                    name.equals(
                            controller
                                    .getCurrentMediaItem()
                                    .mediaMetadata
                                    .title
                                    .toString()
                    );
        }


        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        row.setPadding(
                dp(7),
                dp(5),
                dp(5),
                dp(5)
        );


        GradientDrawable rowBg =
                new GradientDrawable();

        rowBg.setColor(
                Color.WHITE
        );

        rowBg.setStroke(
                dp(isCurrent ? 2 : 1),
                isCurrent
                        ? RED
                        : Color.rgb(
                                225,
                                225,
                                225
                        )
        );

        rowBg.setCornerRadius(
                dp(8)
        );

        row.setBackground(
                rowBg
        );


        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(58)
                );

        rowParams.setMargins(
                0,
                dp(2),
                0,
                dp(2)
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
                18
        );

        logo.setTypeface(
                null,
                Typeface.BOLD
        );

        logo.setTextColor(
                RED
        );

        logo.setGravity(
                Gravity.CENTER
        );


        GradientDrawable logoBg =
                new GradientDrawable();

        logoBg.setColor(
                Color.WHITE
        );

        logoBg.setStroke(
                dp(2),
                RED
        );

        logoBg.setCornerRadius(
                dp(7)
        );

        logo.setBackground(
                logoBg
        );


        LinearLayout.LayoutParams logoParams =
                new LinearLayout.LayoutParams(
                        dp(43),
                        dp(43)
                );

        logo.setLayoutParams(
                logoParams
        );


        LinearLayout center =
                new LinearLayout(this);

        center.setOrientation(
                LinearLayout.VERTICAL
        );

        center.setGravity(
                Gravity.CENTER_VERTICAL
        );


        LinearLayout.LayoutParams centerParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                );

        centerParams.setMargins(
                dp(12),
                0,
                dp(5),
                0
        );

        center.setLayoutParams(
                centerParams
        );


        TextView nameText =
                new TextView(this);

        nameText.setText(
                name
        );

        nameText.setTextSize(
                17
        );

        nameText.setTypeface(
                null,
                isCurrent
                        ? Typeface.BOLD
                        : Typeface.NORMAL
        );

        nameText.setTextColor(
                isCurrent
                        ? RED
                        : Color.rgb(
                                45,
                                45,
                                45
                        )
        );

        center.addView(
                nameText
        );


        if (isCurrent) {

            TextView playing =
                    new TextView(this);

            playing.setText(
                    "● CANLI"
            );

            playing.setTextSize(
                    10
            );

            playing.setTextColor(
                    RED
            );

            center.addView(
                    playing
            );
        }


        TextView star =
                new TextView(this);

        star.setText(
                favorites.contains(name)
                        ? "★"
                        : "☆"
        );

        star.setTextSize(
                26
        );

        star.setTextColor(
                Color.WHITE
        );

        star.setGravity(
                Gravity.CENTER
        );


        GradientDrawable starBg =
                new GradientDrawable();

        starBg.setColor(
                favorites.contains(name)
                        ? DARK_RED
                        : RED
        );

        starBg.setCornerRadius(
                dp(6)
        );

        star.setBackground(
                starBg
        );


        LinearLayout.LayoutParams starParams =
                new LinearLayout.LayoutParams(
                        dp(49),
                        dp(46)
                );

        star.setLayoutParams(
                starParams
        );


        star.setOnClickListener(v -> {

            if (favorites.contains(name)) {

                favorites.remove(name);
                star.setText("☆");

            } else {

                favorites.add(name);
                star.setText("★");
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

        center.setOnClickListener(
                play
        );


        row.addView(
                logo
        );

        row.addView(
                center
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
                        .setTitle(
                                name
                        )
                        .setArtist(
                                "Candan Radyo"
                        )
                        .build();

        MediaItem item =
                new MediaItem.Builder()
                        .setUri(
                                url
                        )
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
                "● CANLI YAYIN"
        );

        showRadios("");
    }

    private int dp(
            int value) {

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
