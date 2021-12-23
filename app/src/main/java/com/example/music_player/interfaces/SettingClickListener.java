package com.example.music_player.interfaces;

import com.example.music_player.enums.SettingType;

public interface SettingClickListener{
    void onSwitchClicked(int position, SettingType type);
    void onItemViewClicked(int position, SettingType type);
}
