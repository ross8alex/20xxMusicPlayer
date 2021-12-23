package com.example.music_player.Classes;

import com.example.music_player.enums.AdapterType;

import java.util.Set;

public class Song {
    public String Album;
    public String Artist;
    public String AlbumArtist;
    public String Title;
    public String Link;
    public String SongID;
    public String Art;
    public String SongNumber;
    private int SongNumberInt;
    public String DiscNumber;
    private int DiscNumberInt;
    public String Genre;
    public String SongLength;

//    public int PlaylistSort;
//    public String PlaylistSongID;
//    public bool AddToRecents;
    private AdapterType NowPlayingSource;

    public Song(){

    }

    public Song(String Album, String Artist, String AlbumArtist, String Title, String Link, String SongID, String Art, String SongNumber, String DiscNumber, String Genre, String SongLength){
        SetAlbum(Album);
        SetArtist(Artist);
        SetAlbumArtist(AlbumArtist);
        SetTitle(Title);
        SetLink(Link);
        SetSongID(SongID);
        SetArt(Art);
        SetSongNumber(SongNumber);
        SetDiscNumber(DiscNumber);
        SetGenre(Genre);
        SetSongLength(SongLength);
    }

    public void setNumbers(){
        try {
            DiscNumberInt = Integer.parseInt(DiscNumber);
            SongNumberInt = Integer.parseInt(SongNumber);
        }
        catch(Exception e){

        }
    }

    public void SetAlbum(String _Album){
        Album = _Album;
    }
    public void SetArtist(String _Artist){
        Artist = _Artist;
    }
    public void SetAlbumArtist(String _AlbumArtist){
        AlbumArtist = _AlbumArtist;
    }
    public void SetTitle(String _Title){
        Title = _Title;
    }
    public void SetLink(String _Link){
        Link = _Link;
    }
    public void SetSongID(String _SongID){
        SongID = _SongID;
    }
    public void SetArt(String _Art){
        Art = _Art;
    }
    public void SetSongNumber(String _SongNumber){
        SongNumber = _SongNumber;
    }
    public void SetDiscNumber(String _DiscNumber){ DiscNumber = _DiscNumber; }
    public void SetGenre(String _Genre){ Genre = _Genre; }
    public void SetSongLength(String _SongLength){ SongLength = _SongLength; }
    public void SetNowPlayingSource(AdapterType _NowPlayingSource){ NowPlayingSource = _NowPlayingSource; }

    public int GetDiscNumberInt(){
        return Integer.parseInt(DiscNumber);
    }

    public int GetSongNumberInt(){
        return Integer.parseInt(SongNumber);
    }

    public String GetAlbum(){
        return (Album == null) ? "" : Album;
    }

    public String GetArtist(){
        return (Artist == null) ? "" : Artist;
    }

    public String GetAlbumArtist(){
        return (AlbumArtist == null) ? "" : AlbumArtist;
    }

    public String GetTitle(){
        return (Title == null) ? "" : Title;
    }

    public String GetLink(){ return (Link == null) ? "" : Link; }

    public String GetSongID(){
        return SongID;
    }

    public String GetArt(){
        return (Art == null) ? "" : Art;
    }

    public String GetSongNumber(){
        return SongNumber;
    }

    public String GetDiscNumber(){
        return DiscNumber;
    }

    public String GetGenre(){ return (Genre == null) ? "" : Genre; }

    public String GetSongLenth(){
        return SongLength;
    }

    public AdapterType GetNowPlayingSource() { return NowPlayingSource; }
}
