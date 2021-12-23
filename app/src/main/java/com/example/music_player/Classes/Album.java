package com.example.music_player.Classes;

public class Album {
    public String Name;
    public String AlbumArtist;
    public String Genre = "";
    public String Art;
    public String AlbumID;

    public Album(){

    }

    public Album(String Name, String AlbumArtist, String Genre, String Art, String AlbumID){
        SetName(Name);
        SetAlbumArtist(AlbumArtist);
        SetGenre(Genre);
        SetArt(Art);
        SetAlbumID(AlbumID);
    }

    public void SetName(String _Name){ Name = _Name;}
    public void SetAlbumArtist(String _AlbumArtist){AlbumArtist = _AlbumArtist;}
    public void SetGenre(String _Genre){Genre = _Genre;}
    public void SetArt(String _Art){Art = _Art;}
    public void SetAlbumID(String _AlbumID){AlbumID = _AlbumID;}

    public String GetName(){
        return (Name == null) ? "" : Name;
    }

    public String GetAlbumArtist(){
        return AlbumArtist == null ? "" : AlbumArtist;
    }

    public String GetGenre(){
        return Genre == null ? "" : Genre;
    }

    public String GetArt(){
        return Art == null ? "" : Art;
    }

    public String AlbumID(){
        return AlbumID == null ? "" : AlbumID;
    }
}
