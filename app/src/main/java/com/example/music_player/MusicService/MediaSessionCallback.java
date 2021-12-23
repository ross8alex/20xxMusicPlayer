package com.example.music_player.MusicService;

import android.content.Intent;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.music_player.MainActivity;

import java.util.function.Function;

import static com.example.music_player.Classes.Singleton.getPlayInstance;

public class MediaSessionCallback extends MediaSession.Callback {
    public Function<Intent, Boolean> MediaButtonEvent;

    public void actionPlay(){
        Intent intent = new Intent(getPlayInstance().getMusicValues().context, PlayMusicService.class);
        intent.setAction(PlayMusicService.ActionPlay);
        getPlayInstance().getMusicValues().context.startService(intent);
    }

    public void actionPause(){
        Intent intent = new Intent(getPlayInstance().getMusicValues().context, PlayMusicService.class);
        intent.setAction(PlayMusicService.ActionPause);
        getPlayInstance().getMusicValues().context.startService(intent);
    }

    public void actionNext(){
        Intent intent = new Intent(getPlayInstance().getMusicValues().context, PlayMusicService.class);
        intent.setAction(PlayMusicService.ActionForward);
        getPlayInstance().getMusicValues().context.startService(intent);
    }

    public void actionBack(){
        Intent intent = new Intent(getPlayInstance().getMusicValues().context, PlayMusicService.class);
        intent.setAction(PlayMusicService.ActionBack);
        getPlayInstance().getMusicValues().context.startService(intent);
    }

    @Override
    public void onPlay() {
        //super.onPlay();
        actionPlay();
    }

    @Override
    public void onPause() {
        //super.onPause();
        actionPause();
    }

    @Override
    public void onSkipToNext() {
        //super.onSkipToNext();
        actionNext();
    }

    @Override
    public void onSkipToPrevious() {
        //super.onSkipToPrevious();
        actionBack();
    }

    @Override
    public void onSeekTo(long pos) {
        //super.onSeekTo(pos);
        if(getPlayInstance().getMusicValues().mediaPlayer.getDuration() != 0){
            getPlayInstance().getMusicValues().mediaPlayer.seekTo((int)pos);
            getPlayInstance().getMusicValues().mediaSession.setPlaybackState(getPlayInstance().getMusicValues().playbackStateBuilder.setState(getPlayInstance().getMusicValues().mediaPlayer.isPlaying() ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED, getPlayInstance().getMusicValues().mediaPlayer.getCurrentPosition(), 1).build());
        }
    }
}
