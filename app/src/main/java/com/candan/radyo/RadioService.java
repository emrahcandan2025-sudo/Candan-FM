package com.candan.radyo;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

public class RadioService extends MediaSessionService {

    private ExoPlayer player;
    private MediaSession mediaSession;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private int retryCount = 0;

    private long sessionStartedAt = 0L;
    private long accumulatedSessionMs = 0L;
    private boolean sessionRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();

        AudioAttributes audioAttributes =
                new AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build();

        player = new ExoPlayer.Builder(this).build();

        player.setAudioAttributes(
                audioAttributes,
                true
        );

        player.setWakeMode(
                C.WAKE_MODE_NETWORK
        );

        mediaSession =
                new MediaSession.Builder(
                        this,
                        player
                ).build();

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (sessionRunning) {
                    updateSessionMetadata();
                }
                handler.postDelayed(this, 1000);
            }
        });

        player.addListener(
                new Player.Listener() {

                    @Override
                    public void onPlaybackStateChanged(
                            int state) {

                        if (state == Player.STATE_READY) {
                            retryCount = 0;
                        }
                        updateSessionTimerState();
                    }

                    @Override
                    public void onIsPlayingChanged(boolean isPlaying) {
                        updateSessionTimerState();
                    }

                    @Override
                    public void onMediaItemTransition(
                            MediaItem mediaItem,
                            int reason) {
                        updateSessionMetadata();
                    }

                    @Override
                    public void onPlayerError(
                            PlaybackException error) {

                        if (player == null ||
                                player.getCurrentMediaItem() == null) {
                            return;
                        }

                        if (retryCount < 10) {

                            retryCount++;

                            handler.postDelayed(
                                    () -> reconnect(),
                                    2000
                            );
                        }
                    }
                }
        );
    }

    private void reconnect() {

        if (player == null ||
                player.getCurrentMediaItem() == null) {
            return;
        }

        try {
            player.prepare();
            player.play();

        } catch (Exception ignored) {
        }
    }

    private void updateSessionTimerState() {
        if (player == null) return;

        if (player.isPlaying()) {
            if (!sessionRunning) {
                sessionStartedAt = android.os.SystemClock.elapsedRealtime();
                sessionRunning = true;
            }
        } else if (sessionRunning) {
            accumulatedSessionMs +=
                    android.os.SystemClock.elapsedRealtime() - sessionStartedAt;
            sessionRunning = false;
        }

        updateSessionMetadata();
    }

    private long getSessionElapsedMs() {
        long total = accumulatedSessionMs;
        if (sessionRunning) {
            total += android.os.SystemClock.elapsedRealtime() - sessionStartedAt;
        }
        return total;
    }

    private void updateSessionMetadata() {
        if (player == null || player.getCurrentMediaItem() == null) return;

        MediaItem current = player.getCurrentMediaItem();
        MediaMetadata old = current.mediaMetadata;

        long totalSeconds = getSessionElapsedMs() / 1000L;
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;

        String station = old.title == null
                ? "Candan Radyo"
                : old.title.toString();

        String elapsed = String.format(
                java.util.Locale.getDefault(),
                "%02d:%02d:%02d",
                hours, minutes, seconds);

        MediaMetadata metadata = old.buildUpon()
                .setTitle(station)
                .setArtist("Candan Radyo • " + elapsed)
                .build();

        MediaItem updated = current.buildUpon()
                .setMediaMetadata(metadata)
                .build();

        int index = player.getCurrentMediaItemIndex();
        if (index >= 0) {
            player.replaceMediaItem(index, updated);
        }
    }

    @Nullable
    @Override
    public MediaSession onGetSession(
            MediaSession.ControllerInfo controllerInfo) {

        return mediaSession;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        if (player != null &&
                player.isPlaying()) {
            return;
        }

        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {

        handler.removeCallbacksAndMessages(null);

        if (mediaSession != null) {
            mediaSession.release();
            mediaSession = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }

        super.onDestroy();
    }
}
