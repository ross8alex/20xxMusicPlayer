package com.example.music_player.Classes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.music_player.DriveHelpers.DriveServiceHelper;
import com.example.music_player.MusicService.PlayMusicService;
import com.example.music_player.enums.AdapterType;
import com.example.music_player.ui.ViewPagerAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Singleton {
    private static Singleton instance;

    private Singleton(){}

    public static Singleton getPlayInstance(){
        if(instance == null){
            instance = new Singleton();
        }
        return instance;
    }

    public Context context;

    public final String version = "0.0.0";

    //public ViewPagerAdapter viewPagerAdapter;
    public DriveServiceHelper mDriveServiceHelper;

    public List<String> recentSearches;
    public List<Song> songs = new ArrayList<Song>();
    public List<Album> albums = new ArrayList<Album>();
    public List<Playlist> playlists = new ArrayList<Playlist>();
    public List<PlaylistSong> playlistSongs = new ArrayList<>();
    public List<Group> genres = new ArrayList<Group>();
    public List<Group> artists = new ArrayList<Group>();
    public List<Album> recents = new ArrayList<Album>();
    public List<String> recentAlbumIDs = new ArrayList<String>();

    public String Search;
    public String itemMedia;
    public Object itemToAdd;
    public Object itemToEdit;
    public AdapterType selectedMedia;
    public Album selectedAlbum;
    public Song selectedSong;
    public Playlist selectedPlaylist;
    public Group selectedGroup;
    public DatabaseReference myRef;

    public SharedPreferences prefs;
    public SharedPreferences.Editor editor;

    public String tempFilePath;
    public String tempFilePath1;

    public GoogleSignInAccount account;

    private PlayMusicValues musicValues;
    public PlayMusicValues getMusicValues(){
        if(musicValues == null){
            musicValues = new PlayMusicValues(context);
        }
        return musicValues;
    }

    public void deleteAlbum(Album album, List<Song> selectedSongs){
        myRef.child("Albums").child(album.AlbumID).removeValue();
        for (Song song : selectedSongs){
            deleteSong(song);
        }
    }

    public void deleteSongKeepDrive(Song song){
        myRef.child("Songs").child(song.SongID).removeValue();

        List<PlaylistSong> playlistSongs = new ArrayList<>();
        for(Playlist playlist : playlists){
            if(playlist.PlaylistSongs.stream().anyMatch(x -> x.SongID.equals(song.SongID))){
                playlistSongs.addAll(playlist.PlaylistSongs.stream().filter(x -> x.SongID.equals(song.SongID)).collect(Collectors.toList()));
            }
        }

        for(PlaylistSong playlistSong : playlistSongs){
            deletePlaylistSong(playlistSong);
        }
    }

    public void deleteSong(Song song){
        myRef.child("Songs").child(song.SongID).removeValue();
        mDriveServiceHelper.deleteFolderFile(song.Link);

        List<PlaylistSong> playlistSongs = new ArrayList<>();
        for(Playlist playlist : playlists){
            if(playlist.PlaylistSongs.stream().anyMatch(x -> x.SongID.equals(song.SongID))){
                playlistSongs.addAll(playlist.PlaylistSongs.stream().filter(x -> x.SongID.equals(song.SongID)).collect(Collectors.toList()));
            }
        }

        for(PlaylistSong playlistSong : playlistSongs){
            deletePlaylistSong(playlistSong);
        }
    }

    public void deletePlaylist(Playlist playlist){
        myRef.child("Playlists").child(playlist.ID).removeValue();
    }

    public void deletePlaylistSong(PlaylistSong playlistSong){
        myRef.child("PlaylistSongs").child(playlistSong.PlaylistSongID).removeValue();
    }

    public void shuffled(List<Song> selectedSongs, boolean keepFirst){
        getPlayInstance().getMusicValues().SongQueue.clear();
        getPlayInstance().getMusicValues().mediaPlayer.setWakeMode(context, 1);
        getPlayInstance().getMusicValues().SongQueue.addAll(selectedSongs);
        getPlayInstance().selectedSong = selectedSongs.get(0);
        getPlayInstance().getMusicValues().shuffled = true;
        getPlayInstance().getMusicValues().SongQueueTemp.clear();
        getPlayInstance().getMusicValues().SongQueueTemp.addAll(getPlayInstance().getMusicValues().SongQueue);
        Collections.shuffle(getPlayInstance().getMusicValues().SongQueue);
        if(keepFirst) {
            getPlayInstance().getMusicValues().SongQueue.remove(getPlayInstance().getMusicValues().SongQueue.indexOf(getPlayInstance().selectedSong));
            getPlayInstance().getMusicValues().SongQueue.add(0, getPlayInstance().selectedSong);
        }
        getPlayInstance().getMusicValues().currentSongIndex = 0;
        Intent intent = new Intent(context, PlayMusicService.class);
        intent.setAction(PlayMusicService.ActionPlayNextTrack);
        context.startService(intent);
    }

    public boolean playNext(List<Song> selectedSongs){
        getPlayInstance().getMusicValues().mediaPlayer.setWakeMode(context, 1);
        if(getPlayInstance().getMusicValues().shuffled){
            getPlayInstance().getMusicValues().SongQueueTemp.addAll(getPlayInstance().getMusicValues().currentSongIndex + 1, selectedSongs);
            Collections.shuffle(selectedSongs);
        }
        if(getPlayInstance().getMusicValues().SongQueue.size() > 0) {
            getPlayInstance().getMusicValues().SongQueue.addAll(getPlayInstance().getMusicValues().currentSongIndex + 1, selectedSongs);
        }
        else{
            getPlayInstance().getMusicValues().SongQueue.addAll(getPlayInstance().getMusicValues().currentSongIndex, selectedSongs);
        }

        if(getPlayInstance().selectedSong == null){
            getPlayInstance().selectedSong = selectedSongs.get(0);
            getPlayInstance().getMusicValues().currentSongIndex = 0;
            Intent intent = new Intent(context, PlayMusicService.class);
            intent.setAction(PlayMusicService.ActionPlayNextTrack);
            context.startService(intent);
            return false;
        }
        else {
            return true;
        }
    }

    public boolean addToQueue(List<Song> selectedSongs){
        getPlayInstance().getMusicValues().mediaPlayer.setWakeMode(context, 1);
        if(getPlayInstance().getMusicValues().shuffled){
            getPlayInstance().getMusicValues().SongQueueTemp.addAll(selectedSongs);
            Collections.shuffle(selectedSongs);
        }
        getPlayInstance().getMusicValues().SongQueue.addAll(selectedSongs);
        if(getPlayInstance().selectedSong == null){
            getPlayInstance().selectedSong = selectedSongs.get(0);
            getPlayInstance().getMusicValues().currentSongIndex = 0;
            Intent intent = new Intent(context, PlayMusicService.class);
            intent.setAction(PlayMusicService.ActionPlayNextTrack);
            context.startService(intent);
            return false;
        }
        else {
            return true;
        }
    }

    public void playMusic(List<Song> selectedSongs, boolean keepFirst){
        if(getPlayInstance().getMusicValues().shuffled){
            shuffled(selectedSongs, keepFirst);
            return;
        }
        getPlayInstance().selectedSong = selectedSongs.get(0);
        getPlayInstance().getMusicValues().SongQueue.clear();
        getPlayInstance().getMusicValues().mediaPlayer.setWakeMode(context, 1);
        getPlayInstance().getMusicValues().currentSongIndex = 0;
        getPlayInstance().getMusicValues().SongQueue.addAll(selectedSongs);
        Intent intent = new Intent(context, PlayMusicService.class);
        intent.setAction(PlayMusicService.ActionPlayNextTrack);
        context.startService(intent);
    }

    public void playFromQueue(int newIndex){
        getPlayInstance().getMusicValues().mediaPlayer.setWakeMode(context, 1);
        getPlayInstance().getMusicValues().currentSongIndex = newIndex;
        getPlayInstance().selectedSong = getPlayInstance().getMusicValues().SongQueue.get(newIndex);
        Intent intent = new Intent(context, PlayMusicService.class);
        intent.setAction(PlayMusicService.ActionPlayNextTrack);
        context.startService(intent);
    }
}
