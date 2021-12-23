package com.example.music_player.ui;

import android.view.View;

public interface AllSongsClickListener{
    void onOverflowClicked(int position, View view);
    void onItemViewClicked(int position, View view);
}
