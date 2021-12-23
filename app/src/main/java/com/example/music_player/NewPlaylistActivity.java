package com.example.music_player;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.music_player.Classes.Album;
import com.example.music_player.Classes.Playlist;
import com.example.music_player.Classes.PlaylistSong;
import com.example.music_player.Classes.Song;
import com.sun.syndication.feed.rss.Guid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.music_player.Classes.Singleton.getPlayInstance;

public class NewPlaylistActivity extends Activity {

    RelativeLayout transparentBackground;
    RelativeLayout PlaylistPanel;

    Button Cancel;
    Button NewPlaylist;

    EditText Name;
    EditText Description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_playlist_layout);

        transparentBackground = findViewById(R.id.transparentBackground);
        PlaylistPanel = findViewById(R.id.PlaylistPanel);

        Cancel = findViewById(R.id.CancelButton);
        NewPlaylist = findViewById(R.id.CreatePlaylistButton);

        Name = findViewById(R.id.Name);
        Description = findViewById(R.id.Description);

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        transparentBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        NewPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Playlist playlist = new Playlist();
                playlist.Name = Name.getText().toString();
                playlist.ID = UUID.randomUUID().toString();

                getPlayInstance().myRef.child("Playlists").child(playlist.ID).setValue(playlist);

                List<Song> tempSongs = new ArrayList<>();

                if(getPlayInstance().itemToAdd != null) {
                    if (getPlayInstance().itemToAdd.getClass().equals(Album.class)) {
                        Album album = (Album) getPlayInstance().itemToAdd;
                        int playlistSort = playlist.PlaylistSongs.size();
                        List<Song> selectedSongs = getPlayInstance().songs.stream().filter(x -> x.GetAlbum().equals(album.GetName())).collect(Collectors.toList());
                        Collections.sort(selectedSongs, Comparator.comparingInt(p -> p.GetSongNumberInt()));
                        Collections.sort(selectedSongs, Comparator.comparingInt(p -> p.GetDiscNumberInt()));
                        for (Song song : selectedSongs) {
                            PlaylistSong playlistSong = new PlaylistSong(song.SongID, playlistSort + 1, playlist.ID, UUID.randomUUID().toString(), song.Art);
                            getPlayInstance().myRef.child("PlaylistSongs").child(playlistSong.PlaylistSongID).setValue(playlistSong);
                            playlistSort++;
                        }
                    } else if (getPlayInstance().itemToAdd.getClass().equals(Song.class)) {
                        Song song = (Song) getPlayInstance().itemToAdd;
                        PlaylistSong playlistSong = new PlaylistSong(song.SongID, playlist.PlaylistSongs.size() + 1, playlist.ID, UUID.randomUUID().toString(), song.Art);
                        getPlayInstance().myRef.child("PlaylistSongs").child(playlistSong.PlaylistSongID).setValue(playlistSong);
                    } else if (getPlayInstance().itemToAdd.getClass().equals(tempSongs.getClass())) {
                        tempSongs = (List<Song>) getPlayInstance().itemToAdd;
                        int playlistSort = playlist.PlaylistSongs.size();
                        for (Song song : tempSongs) {
                            PlaylistSong playlistSong = new PlaylistSong(song.SongID, playlistSort + 1, playlist.ID, UUID.randomUUID().toString(), song.Art);
                            getPlayInstance().myRef.child("PlaylistSongs").child(playlistSong.PlaylistSongID).setValue(playlistSong);
                            playlistSort++;
                        }
                    }
                }

                Toast.makeText(v.getContext(), playlist.Name + " added", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }

        });
    }
}