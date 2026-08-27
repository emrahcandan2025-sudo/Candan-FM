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
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
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
import androidx.media3.common.Player;
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
    private TextView playPauseButton;
    private TextView sessionTimeText;
    private LinearLayout radioList;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private long sessionStartedAt = 0L;
    private long accumulatedSessionMs = 0L;
    private boolean sessionRunning = false;

    private final Runnable sessionTicker = new Runnable() {
        @Override
        public void run() {
            updateSessionTime();
            uiHandler.postDelayed(this, 1000);
        }
    };

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
                    "https://dygedge2.radyotvonline.net/kralfm/playlist.m3u8",
                    "K"},

            {"PowerTürk",
                    "https://listen.powerapp.com.tr/powerturk/mpeg/icecast.audio",
                    "P"},

            {"Power FM",
                    "https://listen.powerapp.com.tr/powerfm/256/chunks.m3u8",
                    "P"},

            {"Radyo Fenomen",
                    "https://live.radyofenomen.com/fenomen/256/icecast.audio",
                    "F"},

            {"Best FM",
                    "https://ssldyg.radyotvonline.com/best/bestfm.stream/playlist.m3u8",
                    "B"},

            {"Alem FM",
                    "https://turkmedya.radyotvonline.net/alemfmaac",
                    "A"},

            {"Radyo D",
                    "https://moondigitaledge2.radyotvonline.net/radyod/playlist.m3u8",
                    "D"},

            {"SlowTürk",
                    "https://radyo.duhnet.tv/slowturk",
                    "S"},

            {"PAL Station",
                    "http://shoutcast.radyogrup.com:1020/",
                    "P"},

            {"PAL FM",
                    "http://shoutcast.radyogrup.com:1030/",
                    "P"},

            {"Kafa Radyo",
                    "https://moondigitalmaster.radyotvonline.net/kafaradyo/playlist.m3u8",
                    "K"},

            {"Show Radyo",
                    "http://46.20.3.229/",
                    "S"},

            {"Radyo Viva",
                    "http://46.20.3.231/",
                    "V"},

            {"Radyo 45lik",
                    "https://stream.radyo45lik.com:4545/",
                    "4"}
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

        SessionToken token = new SessionToken(
                this,
                new ComponentName(this, RadioService.class)
        );

        controllerFuture = new MediaController.Builder(this, token).buildAsync();
        Executor executor = command -> runOnUiThread(command);

        controllerFuture.addListener(() -> {
            try {
                controller = controllerFuture.get();

                controller.addListener(new Player.Listener() {
                    @Override
                    public void onIsPlayingChanged(boolean isPlaying) {
                        if (isPlaying) {
                            startSessionTimerIfNeeded();
                        } else {
                            pauseSessionTimer();
                        }
                        updatePlayerUi();
                    }

                    @Override
                    public void onMediaItemTransition(MediaItem mediaItem, int reason) {
                        updatePlayerUi();
                        showRadios("");
                    }
                });

                updatePlayerUi();
            } catch (Exception ignored) {
            }
        }, executor);
    }

    private void createInterface() {

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setBackgroundColor(SOFT_BG);
        main.setPadding(dp(12), dp(12), dp(12), dp(10));

        TextView title = new TextView(this);
        title.setText("📻  CANDAN RADYO");
        title.setTextSize(27);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        title.setPadding(dp(10), dp(14), dp(10), dp(14));

        GradientDrawable titleBg = new GradientDrawable();
        titleBg.setColor(RED);
        titleBg.setCornerRadius(dp(18));
        title.setBackground(titleBg);
        main.addView(title);

        EditText search = new EditText(this);
        search.setHint("🔎 Radyo ara...");
        search.setTextSize(16);
        search.setSingleLine(true);
        search.setPadding(dp(15), 0, dp(15), 0);

        GradientDrawable searchBg = new GradientDrawable();
        searchBg.setColor(Color.WHITE);
        searchBg.setStroke(dp(1), Color.rgb(220, 220, 220));
        searchBg.setCornerRadius(dp(12));
        search.setBackground(searchBg);

        LinearLayout.LayoutParams searchParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        searchParams.setMargins(0, dp(8), 0, dp(7));
        search.setLayoutParams(searchParams);
        main.addView(search);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        radioList = new LinearLayout(this);
        radioList.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(radioList);

        main.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        LinearLayout miniPlayer = new LinearLayout(this);
        miniPlayer.setOrientation(LinearLayout.VERTICAL);
        miniPlayer.setGravity(Gravity.CENTER);
        miniPlayer.setPadding(dp(12), dp(8), dp(12), dp(8));

        GradientDrawable miniBg = new GradientDrawable();
        miniBg.setColor(Color.WHITE);
        miniBg.setStroke(dp(1), RED);
        miniBg.setCornerRadius(dp(16));
        miniPlayer.setBackground(miniBg);

        LinearLayout.LayoutParams miniParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        miniParams.setMargins(0, dp(7), 0, dp(18));
        miniPlayer.setLayoutParams(miniParams);

        stationText = new TextView(this);
        stationText.setText("Bir radyo seç");
        stationText.setTextSize(17);
        stationText.setTypeface(null, Typeface.BOLD);
        stationText.setTextColor(Color.rgb(35, 35, 35));
        stationText.setGravity(Gravity.CENTER);
        miniPlayer.addView(stationText);

        LinearLayout infoRow = new LinearLayout(this);
        infoRow.setOrientation(LinearLayout.HORIZONTAL);
        infoRow.setGravity(Gravity.CENTER);

        liveText = new TextView(this);
        liveText.setText("Hazır");
        liveText.setTextSize(11);
        liveText.setTextColor(RED);

        sessionTimeText = new TextView(this);
        sessionTimeText.setText("  •  00:00:00");
        sessionTimeText.setTextSize(11);
        sessionTimeText.setTextColor(Color.rgb(110, 110, 110));

        infoRow.addView(liveText);
        infoRow.addView(sessionTimeText);
        miniPlayer.addView(infoRow);

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.CENTER);
        controls.setPadding(0, dp(5), 0, 0);

        TextView previous = createControlButton("⏮", false);
        playPauseButton = createControlButton("▶", true);
        TextView next = createControlButton("⏭", false);

        LinearLayout.LayoutParams side =
                new LinearLayout.LayoutParams(dp(56), dp(42));
        side.setMargins(dp(10), 0, dp(10), 0);
        previous.setLayoutParams(side);
        next.setLayoutParams(new LinearLayout.LayoutParams(side));

        LinearLayout.LayoutParams middle =
                new LinearLayout.LayoutParams(dp(64), dp(50));
        middle.setMargins(dp(12), 0, dp(12), 0);
        playPauseButton.setLayoutParams(middle);

        previous.setOnClickListener(v -> playRelative(-1));
        next.setOnClickListener(v -> playRelative(1));

        playPauseButton.setOnClickListener(v -> {
            if (controller == null) return;

            if (controller.getCurrentMediaItem() == null) {
                playStation(radios[0][0], radios[0][1]);
            } else if (controller.isPlaying()) {
                controller.pause();
            } else {
                controller.play();
            }
        });

        controls.addView(previous);
        controls.addView(playPauseButton);
        controls.addView(next);
        miniPlayer.addView(controls);
        main.addView(miniPlayer);

        showRadios("");

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showRadios(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        setContentView(main);
        uiHandler.post(sessionTicker);
    }

    private TextView createControlButton(String symbol, boolean primary) {
        TextView button = new TextView(this);
        button.setText(symbol);
        button.setTextSize(primary ? 24 : 22);
        button.setTypeface(null, Typeface.BOLD);
        button.setGravity(Gravity.CENTER);
        button.setTextColor(primary ? Color.WHITE : RED);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(primary ? RED : Color.WHITE);
        bg.setStroke(dp(1), RED);
        bg.setCornerRadius(dp(14));
        button.setBackground(bg);
        return button;
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

    private void playStation(String name, String url) {

        if (controller == null) {
            if (liveText != null) liveText.setText("Oynatıcı hazırlanıyor...");
            return;
        }

        int index = findRadioIndex(name);
        if (index < 0) return;

        java.util.ArrayList<MediaItem> items = new java.util.ArrayList<>();

        for (String[] radio : radios) {
            MediaMetadata metadata = new MediaMetadata.Builder()
                    .setTitle(radio[0])
                    .setArtist("Candan Radyo")
                    .build();

            items.add(new MediaItem.Builder()
                    .setUri(radio[1])
                    .setMediaMetadata(metadata)
                    .build());
        }

        controller.setMediaItems(items, index, 0);
        controller.prepare();
        controller.play();

        startSessionTimerIfNeeded();
        updatePlayerUi();
        showRadios("");
    }

    private int findRadioIndex(String name) {
        for (int i = 0; i < radios.length; i++) {
            if (radios[i][0].equals(name)) return i;
        }
        return -1;
    }

    private int getCurrentRadioIndex() {
        if (controller == null ||
                controller.getCurrentMediaItem() == null ||
                controller.getCurrentMediaItem().mediaMetadata.title == null) {
            return -1;
        }
        return findRadioIndex(
                controller.getCurrentMediaItem().mediaMetadata.title.toString());
    }

    private void playRelative(int direction) {
        int current = getCurrentRadioIndex();

        if (current < 0) {
            playStation(radios[0][0], radios[0][1]);
            return;
        }

        int target = (current + direction + radios.length) % radios.length;
        playStation(radios[target][0], radios[target][1]);
    }

    private void updatePlayerUi() {
        if (controller == null || stationText == null ||
                liveText == null || playPauseButton == null) return;

        MediaItem current = controller.getCurrentMediaItem();

        if (current != null && current.mediaMetadata.title != null) {
            stationText.setText(current.mediaMetadata.title.toString());

            if (controller.isPlaying()) {
                liveText.setText("● CANLI");
                playPauseButton.setText("Ⅱ");
            } else {
                liveText.setText("DURAKLATILDI");
                playPauseButton.setText("▶");
            }
        } else {
            stationText.setText("Bir radyo seç");
            liveText.setText("Hazır");
            playPauseButton.setText("▶");
        }
    }

    private void startSessionTimerIfNeeded() {
        if (!sessionRunning) {
            sessionStartedAt = SystemClock.elapsedRealtime();
            sessionRunning = true;
        }
    }

    private void pauseSessionTimer() {
        if (sessionRunning) {
            accumulatedSessionMs += SystemClock.elapsedRealtime() - sessionStartedAt;
            sessionRunning = false;
        }
    }

    private void updateSessionTime() {
        if (sessionTimeText == null) return;

        long total = accumulatedSessionMs;
        if (sessionRunning) {
            total += SystemClock.elapsedRealtime() - sessionStartedAt;
        }

        long seconds = total / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        sessionTimeText.setText(String.format(
                java.util.Locale.getDefault(),
                "  •  %02d:%02d:%02d",
                hours, minutes, secs));
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

        uiHandler.removeCallbacksAndMessages(null);

        if (controllerFuture != null) {

            MediaController.releaseFuture(
                    controllerFuture
            );
        }

        super.onDestroy();
    }
}
