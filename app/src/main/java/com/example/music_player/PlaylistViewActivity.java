package com.example.music_player;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.music_player.Classes.PlaylistSong;
import com.example.music_player.Classes.Song;
import com.example.music_player.MusicService.IsPlayingListener;
import com.example.music_player.MusicService.PlayMusicService;
import com.example.music_player.enums.AdapterType;
import com.example.music_player.ui.AllSongsClickListener;
import com.example.music_player.ui.ReorderAdapter;
import com.example.music_player.ui.SongAdapter;
import com.example.music_player.ui.SongFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.thesurix.gesturerecycler.GestureAdapter;
import com.thesurix.gesturerecycler.GestureManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.example.music_player.Classes.Singleton.getPlayInstance;

public class PlaylistViewActivity extends BaseActivity implements AllSongsClickListener, View.OnTouchListener, IsPlayingListener {
    List<Song> selectedSongs = new ArrayList<Song>();

    RecyclerView songView;
    LinearLayoutManager songLayoutManager;
    ReorderAdapter songAdapter;
    ImageView albumArt1;
    ImageView albumArt2;
    ImageView albumArt3;
    ImageView albumArt4;
    RelativeLayout AlbumView;

    TextView PlaylistName;
    TextView SongCount;

    Toolbar toolbar;
    FloatingActionButton playButton;
    ImageView overflow;

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

    String ButtonColor;

    int Position;
    Context context;

    RelativeLayout.LayoutParams pagerParams;

    RequestManager requestManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_Music_Player_NoActionBar2);
        setContentView(R.layout.playlist_view_layout);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");

        //toolbar.getContext().setTheme(R.style.Theme_Music_Player_PopupOverlay);

//        MainActivity.mDriveServiceHelper.LightBackground(getPlayInstance().selectedAlbum.Art).addOnCompleteListener(new OnCompleteListener<Boolean>() {
//            @Override
//            public void onComplete(@NonNull Task<Boolean> task) {
//                if(task.getResult()){
//                    toolbar.getContext().setTheme(R.style.Theme_Music_Player_PopupOverlayDark);
//                }
//                else{
//                    toolbar.getContext().setTheme(R.style.Theme_Music_Player_PopupOverlay);
//                }
//
//                setSupportActionBar(toolbar);
//
//                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//                getSupportActionBar().setHomeButtonEnabled(true);
//            }
//        });

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        requestManager = Glide.with(this);

        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark, getTheme()));

        songView = findViewById(R.id.Songs);
        albumArt1 = findViewById(R.id.AlbumImage1);
        albumArt2 = findViewById(R.id.AlbumImage2);
        albumArt3 = findViewById(R.id.AlbumImage3);
        albumArt4 = findViewById(R.id.AlbumImage4);
        AlbumView = findViewById(R.id.AlbumView);

        PlaylistName = findViewById(R.id.AlbumName);
        SongCount = findViewById(R.id.SongCount);

        playButton = findViewById(R.id.floatingPlayButton);
        overflow = findViewById(R.id.Overflow);
        songLayout = findViewById(R.id.SongLayoutView);

        width = getResources().getDisplayMetrics().widthPixels;
        height = getResources().getDisplayMetrics().heightPixels;

//        RelativeLayout.LayoutParams imageParams = (RelativeLayout.LayoutParams)albumArt.getLayoutParams();
//        imageParams.height = (int)width;

        Collections.sort(getPlayInstance().selectedPlaylist.PlaylistSongs, Comparator.comparingInt(p -> p.PlaylistSort));
        for (PlaylistSong playlistSong : getPlayInstance().selectedPlaylist.PlaylistSongs){
            selectedSongs.add(getPlayInstance().songs.stream().filter(x -> x.SongID.equals(playlistSong.SongID)).findFirst().get());
        }

        setInfo();

        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags){
            case Configuration.UI_MODE_NIGHT_YES:
                ButtonColor = "#CCFFFFFF";
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                ButtonColor = "#4b4b4b";
                break;
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                break;
        }

//        if(LightBackground()){
//            toolbar.getContext().setTheme(R.style.Theme_Music_Player_PopupOverlay);
//        }
//        else{
//            toolbar.getContext().setTheme(R.style.Theme_Music_Player_PopupOverlayDark);
//        }
//
//        setSupportActionBar(toolbar);
//
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeButtonEnabled(true);

        songLayoutManager = new LinearLayoutManager(this);
        songView.setLayoutManager(songLayoutManager);

        songAdapter = new ReorderAdapter(getPlayInstance().getMusicValues().SongQueue, this, this, ButtonColor, true);
        songAdapter.setData(selectedSongs);
        songView.setAdapter(songAdapter);
        songView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        songAdapter.setDataChangeListener(new GestureAdapter.OnDataChangeListener() {
            @Override
            public void onItemRemoved(Object o, int i, int i1) {
                Song deletedSong = (Song)o;
                List<PlaylistSong> playlistSongs = getPlayInstance().selectedPlaylist.PlaylistSongs.subList(0, getPlayInstance().selectedPlaylist.PlaylistSongs.size());

                PlaylistSong deletedPlaylistSong = getPlayInstance().selectedPlaylist.PlaylistSongs.stream().filter(x -> x.SongID.equals(deletedSong.SongID)).findFirst().get();
                getPlayInstance().deletePlaylistSong(deletedPlaylistSong);
                selectedSongs.remove(i);

                Snackbar snackbar = Snackbar.make(songLayout, "Song Removed", BaseTransientBottomBar.LENGTH_LONG)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                reorderPlaylistFromDelete(playlistSongs);

                                selectedSongs.clear();
                                Collections.sort(getPlayInstance().selectedPlaylist.PlaylistSongs, Comparator.comparingInt(p -> p.PlaylistSort));
                                for (PlaylistSong playlistSong : getPlayInstance().selectedPlaylist.PlaylistSongs){
                                    selectedSongs.add(getPlayInstance().songs.stream().filter(x -> x.SongID.equals(playlistSong.SongID)).findFirst().get());
                                }

                                songAdapter.setData(selectedSongs);
                            }
                        });
                snackbar.show();

                reorderPlaylist();
            }

            @Override
            public void onItemReorder(Object o, int i, int i1) {
                if(i > i1){
                    selectedSongs.add(i1, (Song)o);
                    selectedSongs.remove(i + 1);
                }
                else{
                    selectedSongs.add(i1 + 1, (Song)o);
                    selectedSongs.remove(i);
                }

                reorderPlaylist();
            }
        });

        GestureManager gestureManager = new GestureManager.Builder(songView)
                .setSwipeEnabled(true)
                .setLongPressDragEnabled(true)
                .setManualDragEnabled(true)
                .setSwipeFlags(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
                .setDragFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN)
                .build();

        playButton.setImageResource(R.drawable.play_icon);
        playButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2c3e50")));
        playButton.setColorFilter(Color.parseColor("#FFFFFF"));

        interpolator = new OvershootInterpolator(0);
        songLayout.setTranslationY(height);
        songLayout.setOnTouchListener(this);
        pagerParams = (RelativeLayout.LayoutParams)AlbumView.getLayoutParams();

        fragment = new SongFragment(this, requestManager);
        fragmentTransaction.add(R.id.SongFragmentContainer, fragment);
        fragmentTransaction.commit();

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Song song : selectedSongs){
                    song.SetNowPlayingSource(AdapterType.ALBUM);
                }
                playMusic(selectedSongs, false);
            }
        });

        context = this;

        overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, v);
                MenuInflater inflater = popupMenu.getMenuInflater();

                inflater.inflate(R.menu.playlist_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
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
                            case R.id.action_Edit:
//                                getPlayInstance().itemToEdit = getPlayInstance().selectedPlaylist;
//                                Intent edit = new Intent(getBaseContext(), EditActivity.class);
//                                startActivity(edit);
                                break;
                            case R.id.action_Delete:
                                AlertDialog alertDialog = new AlertDialog.Builder(context)
                                        .setTitle("Delete")
                                        .setMessage("Delete " + getPlayInstance().selectedPlaylist.GetName())
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                getPlayInstance().deletePlaylist(getPlayInstance().playlists.get(Position));
                                                onBackPressed();
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
        });

    }

    void setInfo(){
        List<PlaylistSong> distinctSongs = getPlayInstance().selectedPlaylist.PlaylistSongs.stream().filter(distinctByKey(PlaylistSong::GetArt)).collect(Collectors.toList());

        if(distinctSongs.size() > 0){
            requestManager.load(distinctSongs.get(0).GetArt()).into(albumArt1);
            if (distinctSongs.size() > 1){
                requestManager.load(distinctSongs.get(1).GetArt()).into(albumArt2);
                if (distinctSongs.size() > 2){
                    requestManager.load(distinctSongs.get(2).GetArt()).into(albumArt3);
                    if (distinctSongs.size() > 3){
                        requestManager.load(distinctSongs.get(3).GetArt()).into(albumArt4);
                    }
                    else{
                        requestManager.load(distinctSongs.get(0).GetArt()).into(albumArt4);
                    }
                }
                else{
                    requestManager.load(distinctSongs.get(1).GetArt()).into(albumArt3);
                    requestManager.load(distinctSongs.get(0).GetArt()).into(albumArt4);
                }
            }
            else{
                requestManager.load(distinctSongs.get(0).GetArt()).into(albumArt2);
                requestManager.load(distinctSongs.get(0).GetArt()).into(albumArt3);
                requestManager.load(distinctSongs.get(0).GetArt()).into(albumArt4);
            }
        }

        PlaylistName.setText(getPlayInstance().selectedPlaylist.GetName());
        SongCount.setText(String.valueOf(selectedSongs.size()) + " song" + ((selectedSongs.size() != 1) ? "s":""));
    }

    void reorderPlaylist(){
        for(Song song : selectedSongs){
            PlaylistSong playlistSong = getPlayInstance().selectedPlaylist.PlaylistSongs.stream().filter(x -> x.SongID.equals(song.SongID)).findFirst().get();
            playlistSong.PlaylistSort = selectedSongs.indexOf(song);
            getPlayInstance().myRef.child("PlaylistSongs").child(playlistSong.PlaylistSongID).setValue(playlistSong);
        }
        setInfo();
    }

    void reorderPlaylistFromDelete(List<PlaylistSong> songs){
        for(PlaylistSong playlistSong : songs){
            getPlayInstance().myRef.child("PlaylistSongs").child(playlistSong.PlaylistSongID).setValue(playlistSong);
        }
        setInfo();
    }

    @Override
    public void onOverflowClicked(int position, View view) {
        PopupMenu popupMenu = new PopupMenu(this, view.findViewById(R.id.OverflowMenu));
        MenuInflater inflater = popupMenu.getMenuInflater();

        inflater.inflate(R.menu.song_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                List<Song> overFlowselectedSongs = selectedSongs.subList(position, selectedSongs.size());
                for (Song song : overFlowselectedSongs){
                    song.SetNowPlayingSource(AdapterType.PLAYLIST);
                }
                switch (item.getItemId()) {
                    case R.id.action_Shuffle:
                        shuffled(overFlowselectedSongs, false);
                        break;
                    case R.id.action_PlayNext:
                        playNext(overFlowselectedSongs);
                        break;
                    case R.id.action_AddtoQueue:
                        addToQueue(overFlowselectedSongs);
                        break;
                    case R.id.action_AddtoPlaylist:
                        getPlayInstance().itemToAdd = selectedSongs.get(position);
                        Intent intent = new Intent(getBaseContext(), AddToPlaylistActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_GotoArtist:
                        getPlayInstance().selectedGroup = getPlayInstance().artists.stream().filter(x -> x.GetName().equals(selectedSongs.get(position).Artist)).findFirst().get();
                        Intent artist = new Intent(getBaseContext(), AlbumGroupActivity.class);
                        startActivity(artist);
                        break;
                    case R.id.action_GotoAlbum:
                        getPlayInstance().selectedAlbum = getPlayInstance().albums.stream().filter(x -> x.GetName().equals(selectedSongs.get(position).GetAlbum())).findFirst().get();
                        Intent album = new Intent(context, AlbumViewActivity.class);
                        context.startActivity(album);
                        break;
                    case R.id.action_Edit:
                        getPlayInstance().itemToEdit = selectedSongs.get(position);
                        Intent edit = new Intent(getBaseContext(), EditActivity.class);
                        startActivity(edit);
                        break;
                    case R.id.action_Delete:
                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                .setTitle("Remove")
                                .setMessage("Remove " + selectedSongs.get(position).GetTitle() + " from playlist")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        getPlayInstance().deletePlaylistSong(getPlayInstance().selectedPlaylist.PlaylistSongs.stream().filter(x -> x.SongID.equals(selectedSongs.get(position).SongID)).findFirst().get());
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

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    @Override
    public void onItemViewClicked(int position, View view) {
        List<Song> selectedSongs1 = selectedSongs.subList(position, selectedSongs.size());
        for (Song song : selectedSongs1){
            song.SetNowPlayingSource(AdapterType.ALBUM);
        }
        playMusic(selectedSongs1, true);
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
    IsPlayingListener getIsPlayingListener() {
        return this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public Color getDominantColor(Bitmap bmp)
    {
        //Used for tally
        int red = 0;
        int green = 0;
        int blue = 0;

        int acc = 0;

        for (int x = 0; x < bmp.getWidth(); x++)
        {
            for (int y = 0; y < bmp.getHeight(); y++)
            {
                int tmpColor = bmp.getPixel(x, y);

                red += Color.red(tmpColor);
                green += Color.green(tmpColor);
                blue += Color.blue(tmpColor);

                acc++;
            }
        }

        //Calculate average
        red /= acc;
        green /= acc;
        blue /= acc;

        return Color.valueOf(red, green, blue);
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
            case R.id.action_search:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    int PixelFromDP(int dp)
    {
        int pixel = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
        return pixel;
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
