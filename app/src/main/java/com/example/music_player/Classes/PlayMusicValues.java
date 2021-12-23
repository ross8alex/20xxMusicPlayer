package com.example.music_player.Classes;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.ResultReceiver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.music_player.MainActivity;
import com.example.music_player.MusicService.BetterMediaPlayer;
import com.example.music_player.MusicService.MediaSessionCallback;
import com.example.music_player.enums.RepeatType;

import java.util.ArrayList;
import java.util.List;

public class PlayMusicValues {
    public BetterMediaPlayer mediaPlayer;
    public BetterMediaPlayer tempMediaPlayer;
    public boolean tempMPPrepared;
    public List<Song> SongQueue = new ArrayList<>();
    public List<Song> SongQueueTemp = new ArrayList<>();
    public boolean queueFinished;
    public int currentSongIndex = 0;
    public boolean shuffled = false;
    public RepeatType repeatStatus = RepeatType.SINGLE;
    public MediaSession mediaSession;
    public MediaSessionCallback Callback;
    public PlaybackState.Builder playbackStateBuilder;
    public Context context;
    public boolean seeking;

    public PlayMusicValues(Context context){
        this.context = context;

        Callback = new MediaSessionCallback();
        playbackStateBuilder = new PlaybackState.Builder();
        mediaSession = new MediaSession(context, "MusicService");
        mediaSession.setCallback(Callback);
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setPlaybackState(playbackStateBuilder.setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_SEEK_TO | PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS).build());
        mediaSession.setActive(true);

        mediaPlayer = new BetterMediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());

        tempMediaPlayer = new BetterMediaPlayer();
        tempMediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());

        SongQueue = new ArrayList<>();
    }

    public void ResetMediaPlayer(Context context){
        this.context = context;

        Callback = new MediaSessionCallback();
        playbackStateBuilder = new PlaybackState.Builder();
        mediaSession = new MediaSession(context, "MusicService");
        mediaSession.setCallback(Callback);
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setPlaybackState(playbackStateBuilder.setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_SEEK_TO | PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS).build());
        mediaSession.setActive(true);

        mediaPlayer = new BetterMediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());

        tempMediaPlayer = new BetterMediaPlayer();
        tempMediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
    }
}
