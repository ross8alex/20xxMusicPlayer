package com.example.music_player.Classes;

public class Setting {
    public String PrimarySetting;
    public String SecondarySetting;
    public boolean SettingValue;
    public boolean HasSwitch;

    public Setting(String PrimarySetting, String SecondarySetting, boolean SettingValue, boolean HasSwitch){
        this.PrimarySetting = PrimarySetting;
        this.SecondarySetting = SecondarySetting;
        this.SettingValue = SettingValue;
        this.HasSwitch = HasSwitch;
    }
}
