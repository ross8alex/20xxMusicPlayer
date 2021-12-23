package com.example.music_player;

import androidx.appcompat.app.AppCompatActivity;

import com.example.music_player.MusicService.IsPlayingListener;

import static com.example.music_player.Classes.Singleton.getPlayInstance;

public abstract class BaseActivity extends AppCompatActivity {
    abstract IsPlayingListener getIsPlayingListener();

    @Override
    protected void onResume() {
        getPlayInstance().getMusicValues().mediaPlayer.setIsPlayingListener(getIsPlayingListener());

        super.onResume();
    }

    @Override
    protected void onPause() {
        getPlayInstance().getMusicValues().mediaPlayer.setIsPlayingListener(null);

        super.onPause();
    }
}
