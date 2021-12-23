package com.example.music_player.Classes;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    public String Name;
    public String ID;
    public String Art1;
    public String Art2;
    public String Art3;
    public String Art4;
    public List<Song> RealSongs = new ArrayList<>();
    public List<PlaylistSong> PlaylistSongs = new ArrayList<>();

    public Playlist(){

    }

    public String GetName(){
        return (Name == null) ? "" : Name;
    }
}
