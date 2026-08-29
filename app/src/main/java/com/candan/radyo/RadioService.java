package com.candan.radyo;

import android.content.Intent;
import android.net.Uri;
import androidx.annotation.Nullable;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.okhttp.OkHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.session.MediaLibraryService;
import androidx.media3.session.MediaSession;

import okhttp3.OkHttpClient;

public class RadioService extends MediaLibraryService {

    private ExoPlayer player;
    private MediaLibrarySession mediaLibrarySession;

    @Override
    public void onCreate() {
        super.onCreate();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build();

        OkHttpDataSource.Factory okHttpDataSourceFactory = new OkHttpDataSource.Factory(okHttpClient)
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this, okHttpDataSourceFactory);
        DefaultMediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(dataSourceFactory);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build();

        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory)
                .setAudioAttributes(audioAttributes, true)
                .setHandleAudioBecomingNoisy(true)
                .build();

        mediaLibrarySession = new MediaLibrarySession.Builder(this, player, new MediaLibrarySession.Callback() {
            @Override
            public MediaSession.MediaItemsWithLocalCommandResult onAddMediaItems(
                    MediaSession mediaSession,
                    MediaSession.ControllerInfo controllerInfo,
                    java.util.List<MediaItem> mediaItems) {

                java.util.List<MediaItem> updatedItems = new java.util.ArrayList<>();
                for (MediaItem item : mediaItems) {
                    if (item.localConfiguration != null) {
                        String uriString = item.localConfiguration.uri.toString();
                        MediaItem.Builder builder = item.buildUpon();

                        if (uriString.contains(".m3u8")) {
                            builder.setMimeType(MimeTypes.APPLICATION_M3U8);
                        } else if (uriString.contains(".aac")) {
                            builder.setMimeType(MimeTypes.AUDIO_AAC);
                        }
                        updatedItems.add(builder.build());
                    } else {
                        updatedItems.add(item);
                    }
                }
                return new MediaSession.MediaItemsWithLocalCommandResult(updatedItems, 0);
            }
        }).build();
    }

    @Nullable
    @Override
    public MediaLibrarySession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaLibrarySession;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mediaLibrarySession != null) {
            mediaLibrarySession.release();
            mediaLibrarySession = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
        super.onDestroy();
    }
}
