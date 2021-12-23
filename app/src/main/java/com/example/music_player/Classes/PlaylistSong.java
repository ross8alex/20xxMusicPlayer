package com.example.music_player.Classes;

public class PlaylistSong {
    public String SongID;
    public int PlaylistSort;
    public String PlaylistID;
    public String PlaylistSongID;
    public String Art;

    public PlaylistSong(){

    }

    public PlaylistSong(String _songID,  int _PlaylistSort, String _PlaylistID, String _PlaylistSongID, String _Art){
        SetSongID(_songID);
        SetPlaylistSort(_PlaylistSort);
        SetPlaylistID(_PlaylistID);
        SetPlaylistSongID(_PlaylistSongID);
        SetArt(_Art);
    }

    public void SetSongID(String _songID){
        SongID = _songID;
    }
    public void SetPlaylistSort(int _PlaylistSort) {PlaylistSort = _PlaylistSort; }
    public void SetPlaylistID(String _PlaylistID) {PlaylistID = _PlaylistID; }
    public void SetPlaylistSongID(String _PlaylistSongID) {PlaylistSongID = _PlaylistSongID; }
    public void SetArt(String _Art) {Art = _Art;}

    public String GetSongID(){
        return SongID;
    }

    public int GetPlaylistSort(){
        return PlaylistSort;
    }

    public String GetPlaylistID(){
        return PlaylistID;
    }

    public String GetPlaylistSongID(){
        return PlaylistSongID;
    }

    public String GetArt(){ return (Art == null) ? "" : Art; }
}
