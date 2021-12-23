package com.example.music_player;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_player.Classes.Album;
import com.example.music_player.Classes.Playlist;
import com.example.music_player.Classes.PlaylistSong;
import com.example.music_player.Classes.Song;
import com.example.music_player.enums.AdapterType;
import com.example.music_player.interfaces.AlbumClickListener;
import com.example.music_player.interfaces.BasicClickListener;
import com.example.music_player.ui.AddToPlaylistAdapter;
import com.sun.syndication.feed.rss.Guid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.music_player.Classes.Singleton.getPlayInstance;

public class AddToPlaylistActivity extends Activity implements BasicClickListener {
    RelativeLayout transparentBackground;
    RelativeLayout PlaylistPanel;

    LinearLayoutManager layoutManager;
    AddToPlaylistAdapter addToPlaylistAdapter;
    RecyclerView PlaylistView;
    Button CancelButton;
    Button NewPlaylistButton;

    Playlist selectedPlaylist;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_to_playlist_layout);

        transparentBackground = findViewById(R.id.transparentBackground);
        PlaylistPanel = findViewById(R.id.PlaylistsPanel);

        PlaylistView = findViewById(R.id.PlaylistView);
        CancelButton = findViewById(R.id.CancelButton);
        NewPlaylistButton = findViewById(R.id.NewPlaylistButton);

        layoutManager = new LinearLayoutManager(this);
        PlaylistView.setLayoutManager(layoutManager);
        addToPlaylistAdapter = new AddToPlaylistAdapter(getPlayInstance().playlists, this);
        PlaylistView.setAdapter(addToPlaylistAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(PlaylistView.getContext(), layoutManager.getOrientation());
        PlaylistView.addItemDecoration(dividerItemDecoration);

        transparentBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        CancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        NewPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), NewPlaylistActivity.class);
                v.getContext().startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onItemViewClicked(int position) {
        selectedPlaylist = getPlayInstance().playlists.get(position);
        List<Song> tempSongs = new ArrayList<>();

        if(getPlayInstance().itemToAdd.getClass().equals(Album.class)){
            Album album = (Album) getPlayInstance().itemToAdd;
            int playlistSort = selectedPlaylist.PlaylistSongs.size();
            List<Song> selectedSongs = getPlayInstance().songs.stream().filter(x -> x.GetAlbum().equals(album.GetName())).collect(Collectors.toList());
            Collections.sort(selectedSongs, Comparator.comparingInt(p -> p.GetSongNumberInt()));
            Collections.sort(selectedSongs, Comparator.comparingInt(p -> p.GetDiscNumberInt()));
            for(Song song : selectedSongs){
                PlaylistSong playlistSong = new PlaylistSong(song.SongID, playlistSort + 1, selectedPlaylist.ID, UUID.randomUUID().toString(), song.Art);
                getPlayInstance().myRef.child("PlaylistSongs").child(playlistSong.PlaylistSongID).setValue(playlistSong);
                playlistSort++;
            }
        }
        else if (getPlayInstance().itemToAdd.getClass().equals(Song.class)){
            Song song = (Song) getPlayInstance().itemToAdd;
            PlaylistSong playlistSong = new PlaylistSong(song.SongID, selectedPlaylist.PlaylistSongs.size() + 1, selectedPlaylist.ID, UUID.randomUUID().toString(), song.Art);
            getPlayInstance().myRef.child("PlaylistSongs").child(playlistSong.PlaylistSongID).setValue(playlistSong);
        }
        else if (getPlayInstance().itemToAdd.getClass().equals(tempSongs.getClass())){
            tempSongs = (List<Song>) getPlayInstance().itemToAdd;
            int playlistSort = selectedPlaylist.PlaylistSongs.size();
            for(Song song : tempSongs){
                PlaylistSong playlistSong = new PlaylistSong(song.SongID, playlistSort + 1, selectedPlaylist.ID, UUID.randomUUID().toString(), song.Art);
                getPlayInstance().myRef.child("PlaylistSongs").child(playlistSong.PlaylistSongID).setValue(playlistSong);
                playlistSort++;
            }
        }
        getPlayInstance().itemToAdd = null;

        Toast.makeText(this, "Songs added to playlist", Toast.LENGTH_SHORT).show();
        onBackPressed();
    }
}
