package com.example.music_player;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.music_player.Classes.Song;
import com.example.music_player.MusicService.IsPlayingListener;
import com.example.music_player.MusicService.PlayMusicService;
import com.example.music_player.enums.AdapterType;
import com.example.music_player.ui.AllSongsClickListener;
import com.example.music_player.ui.SongAdapter;
import com.example.music_player.ui.SongFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.music_player.Classes.Singleton.getPlayInstance;

public class AlbumViewActivity extends BaseActivity implements AllSongsClickListener, View.OnTouchListener, IsPlayingListener {
    List<Song> selectedSongs = new ArrayList<Song>();

    RecyclerView songView;
    LinearLayoutManager songLayoutManager;
    SongAdapter songAdapter;
    ImageView albumArt;
    RelativeLayout AlbumView;

    TextView AlbumName;
    TextView AlbumArtist;
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

    int Position;
    Context context;

    RelativeLayout.LayoutParams pagerParams;

    RequestManager requestManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_Music_Player_NoActionBar2);
        setContentView(R.layout.album_view_layout);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");

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
        albumArt = findViewById(R.id.AlbumImage);
        AlbumView = findViewById(R.id.AlbumView);

        AlbumName = findViewById(R.id.AlbumName);
        AlbumArtist = findViewById(R.id.AlbumArtist);
        SongCount = findViewById(R.id.SongCount);

        playButton = findViewById(R.id.floatingPlayButton);
        overflow = findViewById(R.id.Overflow);
        songLayout = findViewById(R.id.SongLayoutView);

        width = getResources().getDisplayMetrics().widthPixels;
        height = getResources().getDisplayMetrics().heightPixels;

        RelativeLayout.LayoutParams imageParams = (RelativeLayout.LayoutParams)albumArt.getLayoutParams();
        imageParams.height = (int)width;

        selectedSongs = getPlayInstance().songs.stream().filter((x) -> x.Album.equals(getPlayInstance().selectedAlbum.Name)).collect(Collectors.toList());
        Collections.sort(selectedSongs, Comparator.comparingInt(p -> p.GetSongNumberInt()));
        Collections.sort(selectedSongs, Comparator.comparingInt(p -> p.GetDiscNumberInt()));

        requestManager.load(getPlayInstance().selectedAlbum.Art).into(albumArt);
        AlbumName.setText(getPlayInstance().selectedAlbum.Name);
        AlbumArtist.setText(getPlayInstance().selectedAlbum.AlbumArtist);
        SongCount.setText(selectedSongs.size() + " songs");

        songLayoutManager = new LinearLayoutManager(this);
        songView.setLayoutManager(songLayoutManager);

        songAdapter = new SongAdapter(selectedSongs, this);
        songView.setAdapter(songAdapter);
        songView.setOverScrollMode(View.OVER_SCROLL_NEVER);

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

                inflater.inflate(R.menu.album_menu, popupMenu.getMenu());
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
                            case R.id.action_AddtoPlaylist:
                                getPlayInstance().itemToAdd = getPlayInstance().selectedAlbum;
                                Intent intent = new Intent(getBaseContext(), AddToPlaylistActivity.class);
                                startActivity(intent);
                                break;
                            case R.id.action_GotoArtist:
                                getPlayInstance().selectedGroup = getPlayInstance().artists.stream().filter(x -> x.GetName().equals(getPlayInstance().selectedAlbum.GetAlbumArtist())).findFirst().get();
                                Intent artist = new Intent(getBaseContext(), AlbumGroupActivity.class);
                                startActivity(artist);
                                break;
                            case R.id.action_Edit:
                                getPlayInstance().itemToEdit = getPlayInstance().selectedAlbum;
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
                                                getPlayInstance().deleteAlbum(getPlayInstance().selectedAlbum, selectedSongs);
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

    public Boolean LightBackground() {

        requestManager.asBitmap().load(getPlayInstance().selectedAlbum.Art).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                BitmapDrawable drawable = (BitmapDrawable) albumArt.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                Color color = getDominantColor(bitmap);
                int argb = color.toArgb();
                if (argb > -10000) {
                    toolbar.getContext().setTheme(R.style.Theme_Music_Player_PopupOverlayDark);;
                }
                else{
                    toolbar.getContext().setTheme(R.style.Theme_Music_Player_PopupOverlay);
                }
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
        return false;
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
            case R.id.action_search:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onOverflowClicked(int position, View view) {
        PopupMenu popupMenu = new PopupMenu(this, view.findViewById(R.id.OverflowMenu));
        MenuInflater inflater = popupMenu.getMenuInflater();

        inflater.inflate(R.menu.album_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                List<Song> overflowSelectedSongs = selectedSongs.subList(position, selectedSongs.size());
                for (Song song : overflowSelectedSongs){
                    song.SetNowPlayingSource(AdapterType.ALBUM);
                }
                switch (item.getItemId()) {
                    case R.id.action_Shuffle:
                        shuffled(overflowSelectedSongs, false);
                        break;
                    case R.id.action_PlayNext:
                        playNext(overflowSelectedSongs);
                        break;
                    case R.id.action_AddtoQueue:
                        addToQueue(overflowSelectedSongs);
                        break;
                    case R.id.action_AddtoPlaylist:
                        getPlayInstance().itemToAdd = selectedSongs.get(position);
                        Intent intent = new Intent(getBaseContext(), AddToPlaylistActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_GotoArtist:
                        getPlayInstance().selectedGroup = getPlayInstance().artists.stream().filter(x -> x.GetName().equals(getPlayInstance().selectedAlbum.GetAlbumArtist())).findFirst().get();
                        Intent artist = new Intent(getBaseContext(), AlbumGroupActivity.class);
                        startActivity(artist);
                        break;
                    case R.id.action_Edit:
                        getPlayInstance().itemToEdit = selectedSongs.get(Position);
                        Intent edit = new Intent(getBaseContext(), EditActivity.class);
                        startActivity(edit);
                        break;
                    case R.id.action_Delete:
                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                .setTitle("Delete")
                                .setMessage("Delete " + selectedSongs.get(position).GetTitle())
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        getPlayInstance().deleteSong(selectedSongs.get(position));
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
