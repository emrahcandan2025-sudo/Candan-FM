package com.candan.radyo;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
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

        player = new ExoPlayer.Builder(this).build();

        mediaSession =
                new MediaSession.Builder(this, player)
                        .build();

        player.addListener(new Player.Listener() {

            @Override
            public void onPlaybackStateChanged(int state) {

                if (state == Player.STATE_READY) {
                    retryCount = 0;
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {

                if (player.getCurrentMediaItem() == null) {
                    return;
                }

                if (retryCount < 5) {

                    retryCount++;

                    handler.postDelayed(() -> {

                        if (player != null &&
                                player.getCurrentMediaItem() != null) {

                            player.prepare();
                            player.play();
                        }

                    }, 2000);
                }
            }
        });
    }

    @Nullable
    @Override
    public MediaSession onGetSession(
            MediaSession.ControllerInfo controllerInfo) {

        return mediaSession;
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
