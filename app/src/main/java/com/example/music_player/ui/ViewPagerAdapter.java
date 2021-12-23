package com.example.music_player.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.RequestManager;
import com.example.music_player.AddToPlaylistActivity;
import com.example.music_player.AlbumGroupActivity;
import com.example.music_player.AlbumViewActivity;
import com.example.music_player.Classes.PlaylistSong;
import com.example.music_player.Classes.Song;
import com.example.music_player.EditActivity;
import com.example.music_player.MusicService.PlayMusicService;
import com.example.music_player.PlaylistViewActivity;
import com.example.music_player.R;
import com.example.music_player.SearchActivity;
import com.example.music_player.enums.AdapterType;
import com.example.music_player.interfaces.UpdateQueueListener;
import com.example.music_player.interfaces.AlbumClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.music_player.Classes.Singleton.getPlayInstance;

public class ViewPagerAdapter extends PagerAdapter implements AlbumClickListener, AllSongsClickListener {
    Context context;
    List<String> titles;
    RequestManager requestManager;

    RecyclerView.LayoutManager recentsLayoutManager;
    public AlbumAdapter recentsAdapter;

    RecyclerView.LayoutManager albumLayoutManager;
    public AlbumAdapter albumAdapter;

    LinearLayoutManager songLayoutManager;
    public AllSongsAdapter songAdapter;

    RecyclerView.LayoutManager playlistLayoutManager;
    public PlaylistAdapter playlistAdapter;

    RecyclerView.LayoutManager genreLayoutManager;
    public GroupAdapter genreAdapter;

    RecyclerView.LayoutManager artistLayoutManager;
    public GroupAdapter artistAdapter;

    UpdateQueueListener updateQueueListener;

    public ViewPagerAdapter(Context context, List<String> titles, RequestManager requestManager) {
        super();
        this.context = context;
        this.titles = titles;
        this.requestManager = requestManager;
    }

    public void setUpdateQueueListener(UpdateQueueListener updateQueueListener) {
        this.updateQueueListener = updateQueueListener;
    }

    @Override
    public int getCount() {
        return titles.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        //return super.getPageTitle(position);
        return new String(titles.get(position));
    }

    @Override
    public void destroyItem(@NonNull View container, int position, @NonNull Object object) {
        //super.destroyItem(container, position, object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        //RecyclerView recyclerView = new RecyclerView(context);
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(container.getContext()).inflate(R.layout.recycler_with_fast_scroll, container, false);
        RecyclerView recyclerView = linearLayout.findViewById(R.id.recyclerView);

        LinearLayout.LayoutParams layoutParams =  (LinearLayout.LayoutParams) recyclerView.getLayoutParams();

        switch (position){
            case 0:
                recyclerView.setAdapter(null);

                recentsLayoutManager = new GridLayoutManager(context, 2);
                recyclerView.setLayoutManager(recentsLayoutManager);
                recentsAdapter = new AlbumAdapter(getPlayInstance().recents, requestManager, context, this, AdapterType.RECENT);
                recyclerView.setAdapter(recentsAdapter);

                recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 30, true));

                layoutParams.rightMargin = 0;
                layoutParams.leftMargin = 0;
                break;
            case 1:
                recyclerView.setAdapter(null);

                playlistLayoutManager = new GridLayoutManager(context, 2);
                recyclerView.setLayoutManager(playlistLayoutManager);
                playlistAdapter = new PlaylistAdapter(getPlayInstance().playlists, context, this);
                recyclerView.setAdapter(playlistAdapter);

                recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 30, true));

                layoutParams.rightMargin = 0;
                layoutParams.leftMargin = 0;
                break;
            case 2:
                recyclerView.setAdapter(null);

                artistLayoutManager = new GridLayoutManager(context, 2);
                recyclerView.setLayoutManager(artistLayoutManager);
                artistAdapter = new GroupAdapter(getPlayInstance().artists, context, this, AdapterType.ARTIST);
                recyclerView.setAdapter(artistAdapter);

                recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 30, true));

                layoutParams.rightMargin = 0;
                layoutParams.leftMargin = 0;
                break;
            case 3:
                recyclerView.setAdapter(null);

                albumLayoutManager = new GridLayoutManager(context, 2);
                recyclerView.setLayoutManager(albumLayoutManager);
                albumAdapter = new AlbumAdapter(getPlayInstance().albums, requestManager, context, this, AdapterType.ALBUM);
                recyclerView.setAdapter(albumAdapter);

                recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 30, true));

                layoutParams.rightMargin = 0;
                layoutParams.leftMargin = 0;
                break;
            case 4:
                recyclerView.setAdapter(null);

                songLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(songLayoutManager);
                //songAdapter = new AllSongsAdapter(MainActivity.songs, context, this);
                songAdapter = new AllSongsAdapter(getPlayInstance().songs, context, this);
                recyclerView.setAdapter(songAdapter);

                layoutParams.rightMargin = 30;
                layoutParams.leftMargin = 30;
                break;
            case 5:
                recyclerView.setAdapter(null);

                genreLayoutManager = new GridLayoutManager(context, 2);
                recyclerView.setLayoutManager(genreLayoutManager);
                genreAdapter = new GroupAdapter(getPlayInstance().genres, context, this, AdapterType.GENRE);
                recyclerView.setAdapter(genreAdapter);

                recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 30, true));

                layoutParams.rightMargin = 0;
                layoutParams.leftMargin = 0;
                break;
        }

        ViewPager viewPager = (ViewPager) container;
        viewPager.addView(linearLayout);
        return linearLayout;
    }

    @Override
    public void onOverflowClicked(int position, View view, AdapterType type) {
        PopupMenu popupMenu = new PopupMenu(context, view.findViewById(R.id.OverflowMenu));
        MenuInflater inflater = popupMenu.getMenuInflater();

        switch (type) {
            case RECENT:
                getPlayInstance().selectedAlbum = getPlayInstance().recents.get(position);
                inflater.inflate(R.menu.album_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        List<Song> selectedSongs = new ArrayList<Song>();
                        selectedSongs = getPlayInstance().songs.stream().filter((x) -> x.Album.equals(getPlayInstance().selectedAlbum.GetName())).collect(Collectors.toList());
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
                                getPlayInstance().itemToAdd = getPlayInstance().recents.get(position);
                                Intent intent = new Intent(context, AddToPlaylistActivity.class);
                                context.startActivity(intent);
                                break;
                            case R.id.action_GotoArtist:
                                getPlayInstance().selectedGroup = getPlayInstance().artists.stream().filter(x -> x.GetName().equals(getPlayInstance().albums.get(position).GetAlbumArtist())).findFirst().get();
                                Intent artist = new Intent(context, AlbumGroupActivity.class);
                                context.startActivity(artist);
                                break;
                            case R.id.action_Edit:
                                getPlayInstance().itemToEdit = getPlayInstance().recents.get(position);
                                Intent edit = new Intent(context, EditActivity.class);
                                context.startActivity(edit);
                                break;
                            case R.id.action_Delete:
                                AlertDialog alertDialog = new AlertDialog.Builder(context)
                                        .setTitle("Delete")
                                        .setMessage("Delete " + getPlayInstance().recents.get(position).GetName())
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
            case ALBUM:
                getPlayInstance().selectedAlbum = getPlayInstance().albums.get(position);
                inflater.inflate(R.menu.album_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        List<Song> selectedSongs = new ArrayList<Song>();
                        selectedSongs = getPlayInstance().songs.stream().filter((x) -> x.Album.equals(getPlayInstance().selectedAlbum.GetName())).collect(Collectors.toList());
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
                                getPlayInstance().itemToAdd = getPlayInstance().albums.get(position);
                                Intent intent = new Intent(context, AddToPlaylistActivity.class);
                                context.startActivity(intent);
                                break;
                            case R.id.action_GotoArtist:
                                getPlayInstance().selectedGroup = getPlayInstance().artists.stream().filter(x -> x.GetName().equals(getPlayInstance().albums.get(position).GetAlbumArtist())).findFirst().get();
                                Intent artist = new Intent(context, AlbumGroupActivity.class);
                                context.startActivity(artist);
                                break;
                            case R.id.action_Edit:
                                getPlayInstance().itemToEdit = getPlayInstance().albums.get(position);
                                Intent edit = new Intent(context, EditActivity.class);
                                context.startActivity(edit);
                                break;
                            case R.id.action_Delete:
                                AlertDialog alertDialog = new AlertDialog.Builder(context)
                                        .setTitle("Delete")
                                        .setMessage("Delete " + getPlayInstance().albums.get(position).GetName())
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
                getPlayInstance().selectedGroup = getPlayInstance().artists.get(position);
                inflater.inflate(R.menu.group_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        List<Song> selectedSongs = new ArrayList<Song>();
                        selectedSongs = getPlayInstance().songs.stream().filter((x) -> x.Artist.equals(getPlayInstance().selectedGroup.GetName())).collect(Collectors.toList());
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
            case GENRE:
                getPlayInstance().selectedGroup = getPlayInstance().genres.get(position);
                inflater.inflate(R.menu.group_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        List<Song> selectedSongs = new ArrayList<Song>();
                        selectedSongs = getPlayInstance().songs.stream().filter((x) -> x.Genre.equals(getPlayInstance().selectedGroup.GetName())).collect(Collectors.toList());
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
                getPlayInstance().selectedPlaylist = getPlayInstance().playlists.get(position);
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
            case RECENT:
                getPlayInstance().selectedAlbum = getPlayInstance().recents.get(position);
                selectedSongs = getPlayInstance().songs.stream().filter((x) -> x.Album.equals(getPlayInstance().selectedAlbum.GetName())).collect(Collectors.toList());
                Collections.sort(selectedSongs, Comparator.comparingInt(p -> p.GetSongNumberInt()));
                Collections.sort(selectedSongs, Comparator.comparingInt(p -> p.GetDiscNumberInt()));
                for (Song song : selectedSongs){
                    song.SetNowPlayingSource(AdapterType.ALBUM);
                }
                break;
            case ALBUM:
                getPlayInstance().selectedAlbum = getPlayInstance().albums.get(position);
                selectedSongs = getPlayInstance().songs.stream().filter((x) -> x.Album.equals(getPlayInstance().selectedAlbum.GetName())).collect(Collectors.toList());
                Collections.sort(selectedSongs, Comparator.comparingInt(p -> p.GetSongNumberInt()));
                Collections.sort(selectedSongs, Comparator.comparingInt(p -> p.GetDiscNumberInt()));
                for (Song song : selectedSongs){
                    song.SetNowPlayingSource(AdapterType.ALBUM);
                }
                break;
            case ARTIST:
                getPlayInstance().selectedGroup = getPlayInstance().artists.get(position);
                selectedSongs = getPlayInstance().songs.stream().filter((x) -> x.Artist.equals(getPlayInstance().selectedGroup.GetName())).collect(Collectors.toList());
                Collections.sort(selectedSongs, Comparator.comparing(p -> p.Title.toLowerCase()));
                for (Song song : selectedSongs){
                    song.SetNowPlayingSource(AdapterType.SONG);
                }
                break;
            case GENRE:
                getPlayInstance().selectedGroup = getPlayInstance().genres.get(position);
                selectedSongs = getPlayInstance().songs.stream().filter((x) -> x.Genre.equals(getPlayInstance().selectedGroup.GetName())).collect(Collectors.toList());
                Collections.sort(selectedSongs, Comparator.comparing(p -> p.Title.toLowerCase()));
                for (Song song : selectedSongs){
                    song.SetNowPlayingSource(AdapterType.SONG);
                }
                break;
            case PLAYLIST:
                getPlayInstance().selectedPlaylist = getPlayInstance().playlists.get(position);
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
            case RECENT:
                getPlayInstance().selectedAlbum = getPlayInstance().recents.get(position);
                Intent recentAlbum = new Intent(context, AlbumViewActivity.class);
                context.startActivity(recentAlbum);
                break;
            case ALBUM:
                getPlayInstance().selectedAlbum = getPlayInstance().albums.get(position);
                Intent album = new Intent(context, AlbumViewActivity.class);
                context.startActivity(album);
                break;
            case ARTIST:
                getPlayInstance().selectedGroup = getPlayInstance().artists.get(position);
                Intent artist = new Intent(context, AlbumGroupActivity.class);
                context.startActivity(artist);
                break;
            case GENRE:
                getPlayInstance().selectedGroup = getPlayInstance().genres.get(position);
                Intent genre = new Intent(context, AlbumGroupActivity.class);
                context.startActivity(genre);
                break;
            case PLAYLIST:
                getPlayInstance().selectedPlaylist = getPlayInstance().playlists.get(position);
                Intent playlist = new Intent(context, PlaylistViewActivity.class);
                context.startActivity(playlist);
                break;
        }
    }

    void shuffled(List<Song> selectedSongs, boolean keepFirst){
        getPlayInstance().shuffled(selectedSongs, keepFirst);
    }

    void playNext(List<Song> selectedSongs){
        if (getPlayInstance().playNext(selectedSongs)) {
            updateQueueListener.onUpdated(true);
        }
    }

    void addToQueue(List<Song> selectedSongs){
        if (getPlayInstance().addToQueue(selectedSongs)){
            updateQueueListener.onUpdated(true);
        }
    }

    void playMusic(List<Song> selectedSongs, boolean keepFirst){
        getPlayInstance().playMusic(selectedSongs, keepFirst);
    }


    @Override
    public void onOverflowClicked(int position, View view) {
        PopupMenu popupMenu = new PopupMenu(context, view.findViewById(R.id.OverflowMenu));

        MenuInflater inflater = popupMenu.getMenuInflater();

        inflater.inflate(R.menu.song_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                List<Song> selectedSongs = new ArrayList<>();
                selectedSongs =  getPlayInstance().songs.subList(position, getPlayInstance().songs.size());
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
                        getPlayInstance().itemToAdd = getPlayInstance().songs.get(position);
                        Intent intent = new Intent(context, AddToPlaylistActivity.class);
                        context.startActivity(intent);
                        break;
                    case R.id.action_GotoArtist:
                        getPlayInstance().selectedGroup = getPlayInstance().artists.stream().filter(x -> x.GetName().equals(getPlayInstance().songs.get(position).GetArtist())).findFirst().get();
                        Intent artist = new Intent(context, AlbumGroupActivity.class);
                        context.startActivity(artist);
                        break;
                    case R.id.action_GotoAlbum:
                        getPlayInstance().selectedAlbum = getPlayInstance().albums.stream().filter(x -> x.GetName().equals(getPlayInstance().songs.get(position).GetAlbum())).findFirst().get();
                        Intent album = new Intent(context, AlbumViewActivity.class);
                        context.startActivity(album);
                        break;
                    case R.id.action_Edit:
                        getPlayInstance().itemToEdit = getPlayInstance().songs.get(position);
                        Intent edit = new Intent(context, EditActivity.class);
                        context.startActivity(edit);
                        break;
                    case R.id.action_Delete:
                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                .setTitle("Delete")
                                .setMessage("Delete " + getPlayInstance().songs.get(position).GetTitle())
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        getPlayInstance().deleteSong(getPlayInstance().songs.get(position));
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
        List<Song> selectedSongs = getPlayInstance().songs.subList(position, getPlayInstance().songs.size());
        for (Song song : selectedSongs){
            song.SetNowPlayingSource(AdapterType.SONG);
        }
        playMusic(selectedSongs, true);
    }
}
