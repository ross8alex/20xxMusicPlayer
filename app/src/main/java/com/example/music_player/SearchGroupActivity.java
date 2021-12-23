package com.example.music_player;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.music_player.Classes.Album;
import com.example.music_player.Classes.PlaylistSong;
import com.example.music_player.Classes.Song;
import com.example.music_player.MusicService.IsPlayingListener;
import com.example.music_player.enums.AdapterType;
import com.example.music_player.interfaces.AlbumClickListener;
import com.example.music_player.ui.AlbumAdapter;
import com.example.music_player.ui.AllSongsAdapter;
import com.example.music_player.ui.AllSongsClickListener;
import com.example.music_player.ui.GridSpacingItemDecoration;
import com.example.music_player.ui.GroupAdapter;
import com.example.music_player.ui.PlaylistAdapter;
import com.example.music_player.ui.SongFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.music_player.Classes.Singleton.getPlayInstance;

public class SearchGroupActivity extends BaseActivity implements View.OnTouchListener, AlbumClickListener, AllSongsClickListener, IsPlayingListener {
    List<Album> selectedAlbums = new ArrayList<Album>();

    RecyclerView SearchGroup;
    GridLayoutManager GridGroupManager;
    LinearLayoutManager SongGroupManager;
    GroupAdapter ArtistAdapter;
    AlbumAdapter AlbumGroupAdapter;
    AllSongsAdapter SongAdapter;
    PlaylistAdapter PlaylistAdapter;

    public RelativeLayout songLayout;
    OvershootInterpolator interpolator;
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    SongFragment fragment;

    float width;
    float height;
    float orientation;
    float lastPositionY;
    float lastTransformY;

    RelativeLayout.LayoutParams pagerParams;

    RequestManager requestManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_Music_Player_NoActionBar2);
        setContentView(R.layout.album_group_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        switch (SearchActivity.selectedMedia){
            case ARTIST:
                toolbar.setTitle(getPlayInstance().Search + " - Artists");
                break;
            case ALBUM:
                toolbar.setTitle(getPlayInstance().Search + " - Albums");
                break;
            case SONG:
                toolbar.setTitle(getPlayInstance().Search + " - Songs");
                break;
            case PLAYLIST:
                toolbar.setTitle(getPlayInstance().Search + " - Playlists");
                break;
        }
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark, getTheme()));

        requestManager = Glide.with(this);

        width = getResources().getDisplayMetrics().widthPixels;
        height = getResources().getDisplayMetrics().heightPixels;

        SearchGroup = findViewById(R.id.AlbumGroups);
        songLayout = findViewById(R.id.SongLayoutView);

        switch (SearchActivity.selectedMedia){
            case ARTIST:
                GridGroupManager = new GridLayoutManager(this, 2);
                SearchGroup.setLayoutManager(GridGroupManager);
                ArtistAdapter = new GroupAdapter(SearchActivity.selectedArtists, this, this, AdapterType.ARTIST);
                SearchGroup.setAdapter(ArtistAdapter);
                SearchGroup.addItemDecoration(new GridSpacingItemDecoration(2, 30, true));
                break;
            case ALBUM:
                GridGroupManager = new GridLayoutManager(this, 2);
                SearchGroup.setLayoutManager(GridGroupManager);
                AlbumGroupAdapter = new AlbumAdapter(SearchActivity.selectedAlbums, requestManager, this,this, AdapterType.ALBUM);
                SearchGroup.setAdapter(AlbumGroupAdapter);
                SearchGroup.addItemDecoration(new GridSpacingItemDecoration(2, 30, true));
                break;
            case SONG:
                SongGroupManager = new LinearLayoutManager(this);
                SearchGroup.setLayoutManager(SongGroupManager);
                SongAdapter = new AllSongsAdapter(SearchActivity.selectedSongs, this, this);
                SearchGroup.setAdapter(SongAdapter);
                break;
            case PLAYLIST:
                GridGroupManager = new GridLayoutManager(this, 2);
                SearchGroup.setLayoutManager(GridGroupManager);
                PlaylistAdapter = new PlaylistAdapter(SearchActivity.selectedPlaylists, this, this);
                SearchGroup.setAdapter(PlaylistAdapter);
                SearchGroup.addItemDecoration(new GridSpacingItemDecoration(2, 30, true));
                break;
        }

        interpolator = new OvershootInterpolator(0);
        songLayout.setTranslationY(height);
        songLayout.setOnTouchListener(this);
        pagerParams = (RelativeLayout.LayoutParams)SearchGroup.getLayoutParams();

        fragment = new SongFragment(this, requestManager);
        fragmentTransaction.add(R.id.SongFragmentContainer, fragment);
        fragmentTransaction.commit();

    }

    @Override
    IsPlayingListener getIsPlayingListener() {
        return this;
    }

    @Override
    public void onBackPressed() {
        if(songLayout.getTranslationY() < height - PixelFromDP(75)){
            songLayout.animate().setInterpolator(interpolator).translationY(height - PixelFromDP(75)).setDuration(500);
            fragment.OpenFullLayout(false);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        if(getPlayInstance().selectedSong != null){
            songLayout.setTranslationY(height - PixelFromDP(75));

            pagerParams.bottomMargin = PixelFromDP(75);

            fragment.SetSelectedSong(getPlayInstance().getMusicValues().mediaPlayer.isPlaying());
            fragment.OpenFullLayout(false);
        }

        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_BUTTON_PRESS:
                return true;
            case MotionEvent.ACTION_DOWN:
                lastPositionY = event.getY();
                lastTransformY = v.getTranslationY();
                return true;
            case MotionEvent.ACTION_UP:
                if (lastTransformY >= v.getTranslationY() - 10 && lastTransformY <= v.getTranslationY() + 10) {
                    if (lastTransformY == 0) {
                        //full down
                        songLayout.animate().setInterpolator(interpolator).translationY(height - PixelFromDP(75)).setDuration(500);
                        fragment.OpenFullLayout(false);
                    } else {
                        //full up
                        songLayout.animate().setInterpolator(interpolator).translationY(PixelFromDP(0)).setDuration(500);
                        fragment.OpenFullLayout(true);
                    }

                    return true;
                } else if (lastTransformY > v.getTranslationY()) {
                    orientation = 0;
                } else {
                    orientation = 1;
                }

                lastTransformY = v.getTranslationY();

                if (orientation == 0) {
                    //up
                    float deltaY = lastPositionY - event.getY();
                    float translationY = v.getTranslationY();
                    translationY -= deltaY;

                    if (translationY < 0) {
                        translationY = 0;
                    }

                    if (translationY > height - PixelFromDP(75)) {
                        translationY = height - PixelFromDP(75);
                    }

                    if (translationY < height - PixelFromDP(150)) {
                        //full up
                        songLayout.animate().setInterpolator(interpolator).translationY(PixelFromDP(0)).setDuration(500);
                        translationY = 0;
                        fragment.OpenFullLayout(true);
                    } else {
                        //full down
                        songLayout.animate().setInterpolator(interpolator).translationY(height - PixelFromDP(75)).setDuration(500);
                        fragment.OpenFullLayout(false);
                    }
                } else {
                    //down
                    float deltaY = lastPositionY - event.getY();
                    float translationY = v.getTranslationY();
                    translationY -= deltaY;

                    if (translationY < 0) {
                        translationY = 0;
                    }

                    if (translationY > height - PixelFromDP(75)) {
                        translationY = height - PixelFromDP(75);
                    }

                    if (translationY < PixelFromDP(75)) {
                        //full up
                        songLayout.animate().setInterpolator(interpolator).translationY(PixelFromDP(0)).setDuration(500);
                        fragment.OpenFullLayout(true);
                    } else {
                        //full down
                        songLayout.animate().setInterpolator(interpolator).translationY(height - PixelFromDP(75)).setDuration(500);
                        fragment.OpenFullLayout(false);
                    }
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                float deltaY1 = lastPositionY - event.getY();
                float translationY1 = v.getTranslationY();
                translationY1 -= deltaY1;

                if (translationY1 < 0) {
                    translationY1 = 0;
                }

                if (translationY1 > height - PixelFromDP(75)) {
                    translationY1 = height - PixelFromDP(75);
                }

                v.setTranslationY(translationY1);
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    int PixelFromDP(int dp)
    {
        int pixel = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
        return pixel;
    }

    void shuffled(List<Song> selectedSongs, boolean keepFirst){
        getPlayInstance().shuffled(selectedSongs, keepFirst);
    }

    void playNext(List<Song> selectedSongs){
        if (getPlayInstance().playNext(selectedSongs)) {
            fragment.SetSelectedSong(getPlayInstance().getMusicValues().mediaPlayer.isPlaying());
        }
    }

    void addToQueue(List<Song> selectedSongs){
        if (getPlayInstance().addToQueue(selectedSongs)){
            fragment.SetSelectedSong(getPlayInstance().getMusicValues().mediaPlayer.isPlaying());
        }
    }

    void playMusic(List<Song> selectedSongs, boolean keepFirst){
        getPlayInstance().playMusic(selectedSongs, keepFirst);
    }

    @Override
    public void onOverflowClicked(int position, View view, AdapterType type) {
        PopupMenu popupMenu = new PopupMenu(this, view.findViewById(R.id.OverflowMenu));
        MenuInflater inflater = popupMenu.getMenuInflater();
        Context context = this;

        switch (type) {
            case ALBUM:
                getPlayInstance().selectedAlbum = SearchActivity.selectedAlbums.get(position);
                inflater.inflate(R.menu.album_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        List<Song> selectedSongs = new ArrayList<Song>();
                        selectedSongs = SearchActivity.selectedSongs.stream().filter((x) -> x.Album.equals(getPlayInstance().selectedAlbum.GetName())).collect(Collectors.toList());
                        Collections.sort(selectedSongs, Comparator.comparingInt(p -> p.GetSongNumberInt()));
                        Collections.sort(selectedSongs, Comparator.comparingInt(p -> p.GetDiscNumberInt()));
                        for (Song song : selectedSongs){
                            song.SetNowPlayingSource(AdapterType.ALBUM);
                        }
                        switch (item.getItemId()) {
                            case R.id.action_Shuffle:
                                shuffled(selectedSongs, false);
                                break;
                            case R.id.action_PlayNext:
                                playNext(selectedSongs);
                                break;
                            case R.id.action_AddtoQueue:
                                addToQueue(selectedSongs);
                                break;
                            case R.id.action_AddtoPlaylist:
                                getPlayInstance().itemToAdd = getPlayInstance().selectedAlbum;
                                Intent intent = new Intent(getBaseContext(), AddToPlaylistActivity.class);
                                startActivity(intent);
                                break;
                            case R.id.action_GotoArtist:
                                getPlayInstance().selectedGroup = getPlayInstance().artists.stream().filter(x -> x.GetName().equals(SearchActivity.selectedAlbums.get(position).GetAlbumArtist())).findFirst().get();
                                Intent artist = new Intent(context, AlbumGroupActivity.class);
                                context.startActivity(artist);
                                break;
                            case R.id.action_Edit:
                                getPlayInstance().itemToEdit = SearchActivity.selectedAlbums.get(position);
                                Intent edit = new Intent(getBaseContext(), EditActivity.class);
                                startActivity(edit);
                                break;
                            case R.id.action_Delete:
                                AlertDialog alertDialog = new AlertDialog.Builder(context)
                                        .setTitle("Delete")
                                        .setMessage("Delete " + getPlayInstance().selectedAlbum.GetName())
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                getPlayInstance().deleteAlbum(getPlayInstance().albums.get(position), getPlayInstance().songs.stream().filter((x) -> x.Album.equals(getPlayInstance().albums.get(position).Name)).collect(Collectors.toList()));
                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        })
                                        .create();
                                alertDialog.show();
                                break;
                        }

                        return true;
                    }
                });
                popupMenu.show();
                break;
            case ARTIST:
                getPlayInstance().selectedGroup = SearchActivity.selectedArtists.get(position);
                inflater.inflate(R.menu.group_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        List<Song> selectedSongs = new ArrayList<Song>();
                        selectedSongs = SearchActivity.selectedSongs.stream().filter((x) -> x.Artist.equals(getPlayInstance().selectedGroup.GetName())).collect(Collectors.toList());
                        Collections.sort(selectedSongs, Comparator.comparing(p -> p.Title.toLowerCase()));
                        for (Song song : selectedSongs){
                            song.SetNowPlayingSource(AdapterType.SONG);
                        }
                        switch (item.getItemId()) {
                            case R.id.action_Shuffle:
                                shuffled(selectedSongs, false);
                                break;
                            case R.id.action_PlayNext:
                                playNext(selectedSongs);
                                break;
                            case R.id.action_AddtoQueue:
                                addToQueue(selectedSongs);
                                break;
                        }

                        return true;
                    }
                });
                popupMenu.show();
                break;
            case PLAYLIST:
                getPlayInstance().selectedPlaylist = SearchActivity.selectedPlaylists.get(position);
                inflater.inflate(R.menu.playlist_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        List<Song> selectedSongs = new ArrayList<Song>();
                        Collections.sort(getPlayInstance().selectedPlaylist.PlaylistSongs, Comparator.comparingInt(p -> p.PlaylistSort));
                        for (PlaylistSong playlistSong : getPlayInstance().selectedPlaylist.PlaylistSongs){
                            Song song = getPlayInstance().songs.stream().filter(x -> x.SongID.equals(playlistSong.SongID)).findFirst().get();
                            song.SetNowPlayingSource(AdapterType.PLAYLIST);
                            selectedSongs.add(song);
                        }
                        switch (item.getItemId()) {
                            case R.id.action_Shuffle:
                                shuffled(selectedSongs, false);
                                break;
                            case R.id.action_PlayNext:
                                playNext(selectedSongs);
                                break;
                            case R.id.action_AddtoQueue:
                                addToQueue(selectedSongs);
                                break;
                            case R.id.action_Edit:
//                                getPlayInstance().itemToEdit = getPlayInstance().playlists.get(Position);
//                                Intent edit = new Intent(context, EditActivity.class);
//                                context.startActivity(edit);
                                break;
                            case R.id.action_Delete:
                                AlertDialog alertDialog = new AlertDialog.Builder(context)
                                        .setTitle("Delete")
                                        .setMessage("Delete " + getPlayInstance().playlists.get(position).GetName())
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                getPlayInstance().deletePlaylist(getPlayInstance().playlists.get(position));
                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        })
                                        .create();
                                alertDialog.show();
                                break;
                        }

                        return true;
                    }
                });
                popupMenu.show();
                break;
        }
    }

    @Override
    public void onPlayButtonClicked(int position, AdapterType type) {
        List<Song> selectedSongs = new ArrayList<Song>();
        switch(type){
            case ALBUM:
                getPlayInstance().selectedAlbum = SearchActivity.selectedAlbums.get(position);
                selectedSongs = getPlayInstance().songs.stream().filter((x) -> x.Album.equals(getPlayInstance().selectedAlbum.GetName())).collect(Collectors.toList());
                Collections.sort(selectedSongs, Comparator.comparingInt(p -> p.GetSongNumberInt()));
                Collections.sort(selectedSongs, Comparator.comparingInt(p -> p.GetDiscNumberInt()));
                for (Song song : selectedSongs){
                    song.SetNowPlayingSource(AdapterType.ALBUM);
                }
                break;
            case ARTIST:
                getPlayInstance().selectedGroup = SearchActivity.selectedArtists.get(position);
                selectedSongs = getPlayInstance().songs.stream().filter((x) -> x.Artist.equals(getPlayInstance().selectedGroup.GetName())).collect(Collectors.toList());
                Collections.sort(selectedSongs, Comparator.comparing(p -> p.Title.toLowerCase()));
                for (Song song : selectedSongs){
                    song.SetNowPlayingSource(AdapterType.SONG);
                }
                break;
            case PLAYLIST:
                getPlayInstance().selectedPlaylist = SearchActivity.selectedPlaylists.get(position);
                Collections.sort(getPlayInstance().selectedPlaylist.PlaylistSongs, Comparator.comparingInt(p -> p.PlaylistSort));
                for (PlaylistSong playlistSong : getPlayInstance().selectedPlaylist.PlaylistSongs){
                    Song song = getPlayInstance().songs.stream().filter(x -> x.SongID.equals(playlistSong.SongID)).findFirst().get();
                    song.SetNowPlayingSource(AdapterType.PLAYLIST);
                    selectedSongs.add(song);
                }
                break;
        }

        if(selectedSongs.size() != 0){
            playMusic(selectedSongs, false);
        }
    }

    @Override
    public void onItemViewClicked(int position, AdapterType type) {
        switch(type){
            case ALBUM:
                getPlayInstance().selectedAlbum = SearchActivity.selectedAlbums.get(position);
                Intent album = new Intent(this, AlbumViewActivity.class);
                startActivity(album);
                break;
            case ARTIST:
                getPlayInstance().selectedGroup = SearchActivity.selectedArtists.get(position);
                Intent artist = new Intent(this, AlbumGroupActivity.class);
                startActivity(artist);
                break;
            case PLAYLIST:
                getPlayInstance().selectedPlaylist = SearchActivity.selectedPlaylists.get(position);
                Intent playlist = new Intent(this, PlaylistViewActivity.class);
                startActivity(playlist);
                break;
        }
    }

    @Override
    public void onOverflowClicked(int position, View view) {
        PopupMenu popupMenu = new PopupMenu(this, view.findViewById(R.id.OverflowMenu));
        Context context = this;
        MenuInflater inflater = popupMenu.getMenuInflater();

        inflater.inflate(R.menu.song_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                List<Song> selectedSongs = new ArrayList<>();
                selectedSongs =  SearchActivity.selectedSongs.subList(position, SearchActivity.selectedSongs.size());
                Collections.sort(selectedSongs, Comparator.comparing(p -> p.Title.toLowerCase()));
                for (Song song : selectedSongs){
                    song.SetNowPlayingSource(AdapterType.SONG);
                }
                switch (item.getItemId()) {
                    case R.id.action_Shuffle:
                        shuffled(selectedSongs, true);
                        break;
                    case R.id.action_PlayNext:
                        playNext(selectedSongs);
                        break;
                    case R.id.action_AddtoQueue:
                        addToQueue(selectedSongs);
                        break;
                    case R.id.action_AddtoPlaylist:
                        getPlayInstance().itemToAdd = SearchActivity.selectedSongs.get(position);
                        Intent intent = new Intent(getBaseContext(), AddToPlaylistActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_GotoArtist:
                        getPlayInstance().selectedGroup = SearchActivity.selectedArtists.stream().filter(x -> x.GetName().equals(SearchActivity.selectedSongs.get(position).GetArtist())).findFirst().get();
                        Intent artist = new Intent(context, AlbumGroupActivity.class);
                        startActivity(artist);
                        break;
                    case R.id.action_GotoAlbum:
                        getPlayInstance().selectedAlbum = SearchActivity.selectedAlbums.stream().filter(x -> x.GetName().equals(SearchActivity.selectedSongs.get(position).GetAlbum())).findFirst().get();
                        Intent album = new Intent(context, AlbumViewActivity.class);
                        startActivity(album);
                        break;
                    case R.id.action_Edit:
                        getPlayInstance().itemToEdit = SearchActivity.selectedSongs.get(position);
                        Intent edit = new Intent(getBaseContext(), EditActivity.class);
                        startActivity(edit);
                        break;
                    case R.id.action_Delete:
                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                .setTitle("Delete")
                                .setMessage("Delete " + SearchActivity.selectedSongs.get(position).GetTitle())
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        getPlayInstance().deleteSong(SearchActivity.selectedSongs.get(position));
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .create();
                        alertDialog.show();
                        break;
                }

                return true;
            }
        });
        popupMenu.show();
    }

    @Override
    public void onItemViewClicked(int position, View view) {
        List<Song> selectedSongs = SearchActivity.selectedSongs.subList(position, SearchActivity.selectedSongs.size());
        for (Song song : selectedSongs){
            song.SetNowPlayingSource(AdapterType.SONG);
        }
        playMusic(selectedSongs, true);
    }

    @Override
    public void onPlayChanged(boolean playing) {
        if(fragment != null) {
            fragment.SetSelectedSong(playing);
        }

        if(pagerParams.bottomMargin != PixelFromDP(75)) {
            //songLayout.setTranslationY(height - PixelFromDP(75));
            songLayout.animate().setInterpolator(interpolator).translationY(height - PixelFromDP(75)).setDuration(500);

            pagerParams.bottomMargin = PixelFromDP(75);
        }
    }
}
