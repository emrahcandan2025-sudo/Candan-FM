package com.candan.radyo;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
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

        player.addListener(
                new Player.Listener() {

                    @Override
                    public void onPlaybackStateChanged(
                            int state) {

                        if (state == Player.STATE_READY) {
                            retryCount = 0;
                        }
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
