package com.example.music_player.interfaces;

import android.view.View;

import com.example.music_player.enums.AdapterType;

public interface AlbumClickListener{
    void onOverflowClicked(int position, View view, AdapterType type);
    void onPlayButtonClicked(int position, AdapterType type);
    void onItemViewClicked(int position, AdapterType type);
}