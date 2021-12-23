package com.example.music_player.MusicService;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;

import androidx.annotation.NonNull;

import com.example.music_player.Classes.PlayMusicValues;
import com.example.music_player.Classes.Song;
import com.example.music_player.DriveHelpers.MyAsyncTask;
import com.example.music_player.MainActivity;
import com.example.music_player.R;
import com.example.music_player.enums.RepeatType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.example.music_player.Classes.Singleton.getPlayInstance;
import static com.example.music_player.MainActivity.createMediaPlayer;
import static com.example.music_player.MainActivity.googleDriveService;

public class PlayMusicService extends Service {
    public static final String ActionPlay = "com.example.music_player.action.PLAY";
    public static final String ActionPause = "com.example.music_player.action.PAUSE";
    public static final String ActionStop = "com.example.music_player.action.STOP";
    public static final String ActionPlayNextTrack = "com.example.music_player.action.ONPLAYNEXTTRACK";
    public static final String ActionBack = "com.example.music_player.action.BACK";
    public static final String ActionForward = "com.example.music_player.action.FORWARD";

    public PlayMusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        getPlayInstance().getMusicValues().mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Play();
            }
        });
        getPlayInstance().getMusicValues().mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(getPlayInstance().getMusicValues().mediaPlayer.getDuration() != 0) {
                    if(getPlayInstance().getMusicValues().repeatStatus.equals(RepeatType.REPEAT) && getPlayInstance().getMusicValues().currentSongIndex == getPlayInstance().getMusicValues().SongQueue.size() - 1){
                        getPlayInstance().getMusicValues().currentSongIndex = 0;
                        getPlayInstance().getMusicValues().queueFinished = true;
                        DeleteFile();
                        onPlayNextTrack();
                    }
                    else if(getPlayInstance().getMusicValues().repeatStatus.equals(RepeatType.REPEAT_ONCE)){
                        DeleteFile();
                        onPlayNextTrack();
                    }
                    else if(getPlayInstance().getMusicValues().currentSongIndex < getPlayInstance().getMusicValues().SongQueue.size() - 1) {
                        getPlayInstance().getMusicValues().currentSongIndex++;
                        DeleteFile();
                        onPlayNextTrack();
                    }
                    else{
                        DeleteFile();
                        Pause();
                        stopForeground(true);
                    }
                }
            }
        });
        getPlayInstance().getMusicValues().tempMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                getPlayInstance().getMusicValues().tempMPPrepared = true;
            }
        });

        super.onCreate();
    }

    public void DeleteFile(){
        java.io.File temp = new File(getPlayInstance().tempFilePath);
        if(temp.exists()){
            temp.delete();
        }
        else{
            String ss = "cant find it";
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case ActionPlay: Play(); break;
                case ActionPause: Pause(); break;
                case ActionPlayNextTrack: onPlayNextTrack(); break;
                case ActionBack: Back(); break;
                case ActionForward: Forward(); break;
            }
        }
        return START_STICKY;
    }

    private boolean isServiceRunning(String serviceName){
        boolean serviceRunning = false;
        ActivityManager am = (ActivityManager)this.getSystemService(Service.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> l = am.getRunningServices(50);

        for (ActivityManager.RunningServiceInfo item : l){
            if(item.service.getClassName().contains(serviceName)){
                serviceRunning = true;

                if(item.foreground){

                }
            }
        }
        return serviceRunning;
    }

    public void Play(){
        if(getPlayInstance().getMusicValues().queueFinished && getPlayInstance().getMusicValues().currentSongIndex == getPlayInstance().getMusicValues().SongQueue.size() - 1){
            getPlayInstance().getMusicValues().currentSongIndex = 0;
            onPlayNextTrack();
            return;
        }
        if (getPlayInstance().getMusicValues().mediaPlayer.getDuration() == 0){
            onPlayNextTrack();
            return;
        }
        getPlayInstance().getMusicValues().queueFinished = false;
        getPlayInstance().getMusicValues().mediaPlayer.start();
        DeleteFile();

        getPlayInstance().getMusicValues().mediaSession.setPlaybackState(getPlayInstance().getMusicValues().playbackStateBuilder.setState(PlaybackState.STATE_PLAYING, getPlayInstance().getMusicValues().mediaPlayer.getCurrentPosition(), 1).build());
        MainActivity.mDriveServiceHelper.GetGlideImageTask(getPlayInstance().selectedSong).addOnCompleteListener(new OnCompleteListener<Bitmap>() {
            @Override
            public void onComplete(@NonNull Task<Bitmap> task) {
                getPlayInstance().getMusicValues().mediaSession.setMetadata(new MediaMetadata.Builder()
                        .putLong(MediaMetadata.METADATA_KEY_DURATION, getPlayInstance().getMusicValues().mediaPlayer.getDuration())
                        .putString(MediaMetadata.METADATA_KEY_TITLE, getPlayInstance().selectedSong.Title)
                        .putString(MediaMetadata.METADATA_KEY_ALBUM, getPlayInstance().selectedSong.Album)
                        .putString(MediaMetadata.METADATA_KEY_ARTIST, getPlayInstance().selectedSong.Artist)
                        .putString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST, getPlayInstance().selectedSong.AlbumArtist)
                        .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, task.getResult()).build());
            }
        });


        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] notifs = notificationManager.getActiveNotifications();
        List<StatusBarNotification> notifList = Arrays.asList(notifs);

        if (isServiceRunning("PlayMusicService") && notifList.stream().anyMatch((x) -> x.getNotification().getChannelId().equals("20XX_Player"))){
            for (StatusBarNotification notif : notifList){
                if(notif.getNotification().extras.getString("android.title").equals(getPlayInstance().getMusicValues().SongQueue.get(getPlayInstance().getMusicValues().currentSongIndex).Title)){
                    notif.getNotification().actions[1] = MainActivity.mDriveServiceHelper.GenerateAction(R.drawable.pause_black_48dp, "Pause", ActionPause, getApplicationContext());
                    notificationManager.notify(notif.getId(), notif.getNotification());
                    startForeground(notif.getId(), notif.getNotification());
                }
                else{
                    startForeground(getPlayInstance().getMusicValues().SongQueue.get(getPlayInstance().getMusicValues().currentSongIndex));
                }
            }
        }
        else{
            if(getPlayInstance().getMusicValues().SongQueue.size() > 0){
                startForeground(getPlayInstance().getMusicValues().SongQueue.get(getPlayInstance().getMusicValues().currentSongIndex));
            }
        }
    }

    public void Pause(){
        stopForeground(false);
        getPlayInstance().getMusicValues().mediaPlayer.pause();
        getPlayInstance().getMusicValues().mediaSession.setPlaybackState(getPlayInstance().getMusicValues().playbackStateBuilder.setState(PlaybackState.STATE_PAUSED, getPlayInstance().getMusicValues().mediaPlayer.getCurrentPosition(), 1).build());

        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] notifs = notificationManager.getActiveNotifications();
        List<StatusBarNotification> notifList = Arrays.asList(notifs);

        for (StatusBarNotification notif : notifList){
            if(notif.getNotification().getChannelId().equals("20XX_Player")){
                notif.getNotification().actions[1] = MainActivity.mDriveServiceHelper.GenerateAction(R.drawable.play_arrow_black_48dp, "Play", ActionPlay, getApplicationContext());
                notificationManager.notify(notif.getId(), notif.getNotification());
            }
        }
    }

    public void Back(){
        if(getPlayInstance().getMusicValues().currentSongIndex > 0){
            getPlayInstance().getMusicValues().currentSongIndex--;
            onPlayNextTrack();
        }
    }

    public void Forward(){
        if(getPlayInstance().getMusicValues().currentSongIndex < getPlayInstance().getMusicValues().SongQueue.size() - 1){
            getPlayInstance().getMusicValues().currentSongIndex++;
            onPlayNextTrack();
        }
    }

    private void startForeground(Song song){
        MainActivity.mDriveServiceHelper.BuildNotification(getApplicationContext(), song, ActionBack, ActionPlay, ActionPause, ActionForward).addOnCompleteListener(new OnCompleteListener<Notification.Builder>() {
            @Override
            public void onComplete(@NonNull Task<Notification.Builder> task) {
                NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                Notification notification = task.getResult().build();
                notificationManager.notify(1, notification);
                startForeground(1, notification);
            }
        });
    }

    private void onPlayNextTrack() {
        if (getPlayInstance().getMusicValues().currentSongIndex < getPlayInstance().getMusicValues().SongQueue.size()) {
            getPlayInstance().selectedSong = getPlayInstance().getMusicValues().SongQueue.get(getPlayInstance().getMusicValues().currentSongIndex);
            getPlayInstance().editor.putString("SelectedSongID", getPlayInstance().selectedSong.SongID);
            getPlayInstance().editor.commit();

            getPlayInstance().getMusicValues().mediaPlayer.reset();

            try {
                MainActivity.mDriveServiceHelper.getInputStream(googleDriveService, getPlayInstance().selectedSong.Link).addOnCompleteListener(new OnCompleteListener<InputStream>() {
                    @Override
                    public void onComplete(@NonNull Task<InputStream> task) {
                        try {
                            MyAsyncTask myTask =  new MyAsyncTask(task.getResult());
                            getPlayInstance().tempFilePath = myTask.execute().get();
                            myTask.cancel(true);
                            getPlayInstance().getMusicValues().mediaPlayer.setDataSource(getPlayInstance().tempFilePath);
                            getPlayInstance().getMusicValues().mediaPlayer.prepareAsync();
                        } catch (Exception e) {
                            String i = e.getMessage();
                        }
                    }
                });
            } catch (Exception exception) {
                String e = exception.getMessage();
            }
        }
    }
}