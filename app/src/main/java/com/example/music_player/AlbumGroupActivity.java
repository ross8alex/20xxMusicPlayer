package com.example.music_player;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.music_player.Classes.Album;
import com.example.music_player.Classes.Song;
import com.example.music_player.MusicService.IsPlayingListener;
import com.example.music_player.MusicService.PlayMusicService;
import com.example.music_player.enums.AdapterType;
import com.example.music_player.enums.GroupType;
import com.example.music_player.interfaces.AlbumClickListener;
import com.example.music_player.ui.AlbumAdapter;
import com.example.music_player.ui.GridSpacingItemDecoration;
import com.example.music_player.ui.SongFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import org.apache.http.HttpRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.music_player.Classes.Singleton.getPlayInstance;

public class AlbumGroupActivity extends BaseActivity implements AlbumClickListener, View.OnTouchListener, IsPlayingListener {
    List<Album> selectedAlbums = new ArrayList<Album>();

    RecyclerView AlbumGroup;
    GridLayoutManager AlbumGroupManager;
    AlbumAdapter AlbumGroupAdapter;

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

    int Position;
    Context context = this;

    RelativeLayout.LayoutParams pagerParams;
    RequestManager requestManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_Music_Player_NoActionBar2);
        setContentView(R.layout.album_group_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getPlayInstance().selectedGroup.Name);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark, getTheme()));

        requestManager = Glide.with(this);

        width = getResources().getDisplayMetrics().widthPixels;
        height = getResources().getDisplayMetrics().heightPixels;

        AlbumGroup = findViewById(R.id.AlbumGroups);
        songLayout = findViewById(R.id.SongLayoutView);

        if(getPlayInstance().selectedGroup.type == GroupType.GENRE){
            List<Song> songs = getPlayInstance().songs.stream().filter((x) -> x.GetGenre().equals(getPlayInstance().selectedGroup.Name)).collect(Collectors.toList());
            for(Song song : songs){
                selectedAlbums.addAll(getPlayInstance().albums.stream().filter((x) -> x.Name.equals(song.Album) && !selectedAlbums.stream().anyMatch((y) -> y.AlbumID.equals(x.AlbumID))).collect(Collectors.toList()));
            }
        }
        else if(getPlayInstance().selectedGroup.type == GroupType.ARTIST){
            List<Song> songs = getPlayInstance().songs.stream().filter((x) -> x.Artist.equals(getPlayInstance().selectedGroup.Name)).collect(Collectors.toList());
            for (Song song : songs){
                selectedAlbums.addAll(getPlayInstance().albums.stream().filter((x) -> x.Name.equals(song.Album) && !selectedAlbums.stream().anyMatch((y) -> y.AlbumID.equals(x.AlbumID))).collect(Collectors.toList()));
            }
        }

        AlbumGroupManager = new GridLayoutManager(this, 2);
        AlbumGroup.setLayoutManager(AlbumGroupManager);
        AlbumGroupAdapter = new AlbumAdapter(selectedAlbums, requestManager, this, this, AdapterType.ALBUM);
        AlbumGroup.setAdapter(AlbumGroupAdapter);

        AlbumGroup.addItemDecoration(new GridSpacingItemDecoration(2, 30, true));

        interpolator = new OvershootInterpolator(0);
        songLayout.setTranslationY(height);
        songLayout.setOnTouchListener(this);

        pagerParams = (RelativeLayout.LayoutParams)AlbumGroup.getLayoutParams();

        fragment = new SongFragment(this, requestManager);
        fragmentTransaction.add(R.id.SongFragmentContainer, fragment);
        fragmentTransaction.commit();

        //getWikipediaData();
    }

    void getWikipediaData(){

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
    public void onOverflowClicked(int position, View view, AdapterType type) {
        PopupMenu popupMenu = new PopupMenu(this, view.findViewById(R.id.OverflowMenu));
        MenuInflater inflater = popupMenu.getMenuInflater();
        getPlayInstance().selectedAlbum = selectedAlbums.get(position);
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
                        getPlayInstance().itemToAdd = getPlayInstance().selectedAlbum;
                        Intent intent = new Intent(getBaseContext(), AddToPlaylistActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_GotoArtist:
                        getPlayInstance().selectedGroup = getPlayInstance().artists.stream().filter(x -> x.GetName().equals(getPlayInstance().albums.get(position).GetAlbumArtist())).findFirst().get();
                        Intent artist = new Intent(getBaseContext(), AlbumGroupActivity.class);
                        startActivity(artist);
                        break;
                    case R.id.action_Edit:
                        getPlayInstance().itemToEdit = selectedAlbums.get(position);
                        Intent edit = new Intent(getBaseContext(), EditActivity.class);
                        startActivity(edit);
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

    }

    @Override
    public void onPlayButtonClicked(int position, AdapterType type) {
        List<Song> selectedSongs = new ArrayList<Song>();
        getPlayInstance().selectedAlbum = selectedAlbums.get(position);
        selectedSongs = getPlayInstance().songs.stream().filter((x) -> x.Album.equals(getPlayInstance().selectedAlbum.GetName())).collect(Collectors.toList());
        Collections.sort(selectedSongs, Comparator.comparingInt(p -> p.GetSongNumberInt()));
        Collections.sort(selectedSongs, Comparator.comparingInt(p -> p.GetDiscNumberInt()));
        for (Song song : selectedSongs){
            song.SetNowPlayingSource(AdapterType.ALBUM);
        }

        if(selectedSongs.size() != 0){
            playMusic(selectedSongs, false);
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_search:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemViewClicked(int position, AdapterType type) {
        getPlayInstance().selectedAlbum = selectedAlbums.get(position);
        Intent genre = new Intent(this, AlbumViewActivity.class);
        startActivity(genre);
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