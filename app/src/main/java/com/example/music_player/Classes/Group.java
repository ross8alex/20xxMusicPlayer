package com.example.music_player.Classes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.music_player.enums.GroupType;

import org.apache.poi.ss.formula.functions.Na;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class Group {
    public String Name;
    public List<Album> NumberOfAlbums = new ArrayList<Album>();
    public List<Song> Songs = new ArrayList<Song>();
    public GroupType type;

    public String GetName(){
        return (Name == null) ? "" : Name;
    }
}
