package com.example.music_player.MusicService;

import android.media.MediaPlayer;

public class BetterMediaPlayer extends MediaPlayer {
    public IsPlayingListener isPlayingListener;

    @Override
    public void start() throws IllegalStateException {
        if (isPlayingListener != null) {
            isPlayingListener.onPlayChanged(true);
        }
        super.start();
    }

    @Override
    public void stop() throws IllegalStateException {
        if (isPlayingListener != null) {
            isPlayingListener.onPlayChanged(false);
        }
        super.stop();
    }

    @Override
    public void pause() throws IllegalStateException {
        if (isPlayingListener != null) {
            isPlayingListener.onPlayChanged(false);
        }
        super.pause();
    }

    public void setIsPlayingListener(IsPlayingListener isPlayingListener){
        this.isPlayingListener = isPlayingListener;
    }
}
