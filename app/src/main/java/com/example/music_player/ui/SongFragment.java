package com.example.music_player.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.music_player.AddToPlaylistActivity;
import com.example.music_player.AlbumGroupActivity;
import com.example.music_player.AlbumViewActivity;
import com.example.music_player.Classes.Album;
import com.example.music_player.Classes.Song;
import com.example.music_player.EditActivity;
import com.example.music_player.MainActivity;
import com.example.music_player.MusicService.IsPlayingListener;
import com.example.music_player.MusicService.PlayMusicService;
import com.example.music_player.R;
import com.example.music_player.enums.AdapterType;
import com.example.music_player.enums.RepeatType;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thesurix.gesturerecycler.GestureAdapter;
import com.thesurix.gesturerecycler.GestureManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.music_player.Classes.Singleton.getPlayInstance;
import static com.example.music_player.ui.GroupAdapter.distinctByKey;

public class SongFragment extends Fragment implements AllSongsClickListener {
    //Context context;

    public ImageView AlbumImageNow;
    public TextView SongNow;
    public TextView ArtistNow;
    public ImageView PlayButton;
    public  ImageView QueueButton;
    public ImageView Overflow;

    public ImageView SongArt;
    public RecyclerView SongQueue;
    public RelativeLayout SongNameCard;
    public ImageView SongPlayBack;
    public ImageView SongPlayButton;
    public ImageView SongPlayForward;
    public ImageView SongPlayShuffle;
    public ImageView SongPlayRepeat;
    public SeekBar seekBar;
    public CardView PlayBar;
    public RelativeLayout nowPlayingToolbar;
    public TextView nowPlayingSource1;
    public TextView nowPlayingSource2;
    public RelativeLayout nowPlayingArt;
    public ImageView ArtFull;
    public LinearLayout nowPlayingImagesMulti;
    public ImageView Art1;
    public ImageView Art2;
    public ImageView Art3;
    public ImageView Art4;
    public TextView currentPosition;
    public TextView duration;

    public RelativeLayout.LayoutParams SongNameCardParams;

    public Thread thread;
    IsPlayingListener isPlayingListener;
    LinearLayoutManager songLayoutManager;
    public ReorderAdapter adapter;

    String ButtonColor;
    String ButtonHighlighted;
    int nightModeFlags;

    Handler handler;
    Runnable task;
    RequestManager requestManager;

    CoordinatorLayout coordinatorLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.song_fragment_layout, container, false);

        AlbumImageNow = root.findViewById(R.id.ImageNow);
        SongNow = root.findViewById(R.id.SongNameNow);
        ArtistNow = root.findViewById(R.id.ArtistNameNow);
        PlayButton = root.findViewById(R.id.PlayButton);
        QueueButton = root.findViewById(R.id.QueueButton);
        Overflow = root.findViewById(R.id.Overflow);

        SongArt = root.findViewById(R.id.SongAlbumArt);
        SongQueue = root.findViewById(R.id.SongQueue);
        SongNameCard = root.findViewById(R.id.SongNameCard);
        SongPlayBack = root.findViewById(R.id.SongPlayBack);
        SongPlayButton = root.findViewById(R.id.SongPlayButton);
        SongPlayForward = root.findViewById(R.id.SongPlayForward);
        SongPlayShuffle = root.findViewById(R.id.SongPlayShuffle);
        SongPlayRepeat = root.findViewById(R.id.SongPlayRepeat);
        seekBar = root.findViewById(R.id.SeekBar);
        PlayBar = root.findViewById(R.id.PlayBar);
        nowPlayingToolbar = root.findViewById(R.id.nowPlayingToolbar);
        nowPlayingSource1 = root.findViewById(R.id.Source1);
        nowPlayingSource2 = root.findViewById(R.id.Source2);
        nowPlayingArt = root.findViewById(R.id.nowPlayingArt);
        ArtFull = root.findViewById(R.id.ArtFull);
        nowPlayingImagesMulti = root.findViewById(R.id.nowPlayingImagesMulti);
        Art1 = root.findViewById(R.id.Art1);
        Art2 = root.findViewById(R.id.Art2);
        Art3 = root.findViewById(R.id.Art3);
        Art4 = root.findViewById(R.id.Art4);
        currentPosition = root.findViewById(R.id.currentPosition);
        duration = root.findViewById(R.id.Duration);

        requestManager = Glide.with(this);

        nightModeFlags = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

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

        ButtonHighlighted = "#3498db";

        PlayButton.setColorFilter(Color.parseColor(ButtonColor));
        SongPlayButton.setColorFilter(Color.parseColor(ButtonColor));
        SongPlayBack.setColorFilter(Color.parseColor(ButtonColor));
        SongPlayForward.setColorFilter(Color.parseColor(ButtonColor));
        SongPlayShuffle.setColorFilter(Color.parseColor(ButtonColor));
        SongPlayRepeat.setColorFilter(Color.parseColor(ButtonColor));
        QueueButton.setColorFilter(Color.parseColor(ButtonColor));

        songLayoutManager = new LinearLayoutManager(requireContext());
        SongQueue.setLayoutManager(songLayoutManager);
        adapter = new ReorderAdapter(getPlayInstance().getMusicValues().SongQueue, requireContext(), this, ButtonColor, false);
        adapter.setData(getPlayInstance().getMusicValues().SongQueue);
        SongQueue.setAdapter(adapter);

        SongNameCardParams = (RelativeLayout.LayoutParams) SongNameCard.getLayoutParams();

        if(!getPlayInstance().getMusicValues().shuffled){
            SongPlayShuffle.setColorFilter(Color.parseColor(ButtonColor));
        }
        else{
            SongPlayShuffle.setColorFilter(Color.parseColor(ButtonHighlighted));
        }

        switch(getPlayInstance().getMusicValues().repeatStatus){
            case SINGLE:
                requestManager.load(R.drawable.repeat_black_48dp).into(SongPlayRepeat);
                SongPlayRepeat.setColorFilter(Color.parseColor(ButtonColor));
                break;
            case REPEAT:
                requestManager.load(R.drawable.repeat_black_48dp).into(SongPlayRepeat);
                SongPlayRepeat.setColorFilter(Color.parseColor(ButtonHighlighted));
                break;
            case REPEAT_ONCE:
                requestManager.load(R.drawable.repeat_one_black_48dp).into(SongPlayRepeat);
                SongPlayRepeat.setColorFilter(Color.parseColor(ButtonHighlighted));
                break;
        }

        adapter.setDataChangeListener(new GestureAdapter.OnDataChangeListener() {
            @Override
            public void onItemRemoved(Object o, int i, int i1) {
                getPlayInstance().getMusicValues().SongQueue.remove(i);
                Song deletedSong = (Song)o;
                int position = i1;

                if(i > getPlayInstance().getMusicValues().currentSongIndex){

                }
                else if(i < getPlayInstance().getMusicValues().currentSongIndex){
                    getPlayInstance().getMusicValues().currentSongIndex--;
                }
                else if(i == getPlayInstance().getMusicValues().currentSongIndex){
                    getPlayInstance().getMusicValues().currentSongIndex--;
                    Intent intent = new Intent(requireContext(), PlayMusicService.class);
                    intent.setAction(PlayMusicService.ActionForward);
                    requireContext().startService(intent);
                }

                Snackbar snackbar = Snackbar.make(root, "Song Removed", BaseTransientBottomBar.LENGTH_LONG)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getPlayInstance().getMusicValues().SongQueue.add(i, deletedSong);
                                adapter.setData(getPlayInstance().getMusicValues().SongQueue);
                                //adapter.notifyDataSetChanged();
                            }
                        });
                snackbar.show();
            }

            @Override
            public void onItemReorder(Object o, int i, int i1) {
                if(i > i1){
                    getPlayInstance().getMusicValues().SongQueue.add(i1, (Song)o);
                    getPlayInstance().getMusicValues().SongQueue.remove(i + 1);
                }
                else{
                    getPlayInstance().getMusicValues().SongQueue.add(i1 + 1, (Song)o);
                    getPlayInstance().getMusicValues().SongQueue.remove(i);
                }
                if(i1 == getPlayInstance().getMusicValues().currentSongIndex){
                    getPlayInstance().getMusicValues().currentSongIndex++;
                }

                if(i == getPlayInstance().getMusicValues().currentSongIndex){
                    getPlayInstance().getMusicValues().currentSongIndex = i1;
                }
            }
        });

        GestureManager gestureManager = new GestureManager.Builder(SongQueue)
                .setSwipeEnabled(true)
                .setLongPressDragEnabled(true)
                .setManualDragEnabled(true)
                .setSwipeFlags(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
                .setDragFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN)
                .build();


        PlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getPlayInstance().getMusicValues().mediaPlayer != null) {
                    if (getPlayInstance().getMusicValues().mediaPlayer.isPlaying()) {
                        Intent intent = new Intent(requireContext(), PlayMusicService.class);
                        intent.setAction(PlayMusicService.ActionPause);
                        requireContext().startService(intent);
                    } else {
                        Intent intent = new Intent(requireContext(), PlayMusicService.class);
                        intent.setAction(PlayMusicService.ActionPlay);
                        requireContext().startService(intent);
                    }
                }
            }
        });

        SongPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getPlayInstance().getMusicValues().mediaPlayer != null) {
                    if (getPlayInstance().getMusicValues().mediaPlayer.isPlaying()) {
                        Intent intent = new Intent(requireContext(), PlayMusicService.class);
                        intent.setAction(PlayMusicService.ActionPause);
                        requireContext().startService(intent);
                    } else {
                        Intent intent = new Intent(requireContext(), PlayMusicService.class);
                        intent.setAction(PlayMusicService.ActionPlay);
                        requireContext().startService(intent);
                    }
                }
            }
        });

        SongPlayBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), PlayMusicService.class);
                intent.setAction(PlayMusicService.ActionBack);
                requireContext().startService(intent);
            }
        });

        SongPlayForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), PlayMusicService.class);
                intent.setAction(PlayMusicService.ActionForward);
                requireContext().startService(intent);
            }
        });

        SongPlayShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!getPlayInstance().getMusicValues().shuffled) {
                    SongPlayShuffle.setColorFilter(Color.parseColor(ButtonHighlighted));
                    getPlayInstance().getMusicValues().shuffled = true;
                    getPlayInstance().getMusicValues().SongQueueTemp.clear();
                    getPlayInstance().getMusicValues().SongQueueTemp.addAll(getPlayInstance().getMusicValues().SongQueue);
                    Collections.shuffle(getPlayInstance().getMusicValues().SongQueue);
                    getPlayInstance().getMusicValues().SongQueue.remove(getPlayInstance().getMusicValues().SongQueue.indexOf(getPlayInstance().selectedSong));
                    getPlayInstance().getMusicValues().SongQueue.add(0, getPlayInstance().selectedSong);
                    getPlayInstance().getMusicValues().currentSongIndex = 0;
                    adapter.setData(getPlayInstance().getMusicValues().SongQueue);
                }
                else{
                    SongPlayShuffle.setColorFilter(Color.parseColor(ButtonColor));
                    getPlayInstance().getMusicValues().shuffled = false;
                    getPlayInstance().getMusicValues().SongQueue.clear();
                    getPlayInstance().getMusicValues().SongQueue.addAll(getPlayInstance().getMusicValues().SongQueueTemp);
                    getPlayInstance().getMusicValues().currentSongIndex = getPlayInstance().getMusicValues().SongQueue.indexOf(getPlayInstance().selectedSong);
                    adapter.setData(getPlayInstance().getMusicValues().SongQueue);
                }
            }
        });

        SongPlayRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(getPlayInstance().getMusicValues().repeatStatus){
                    case SINGLE:
                        getPlayInstance().getMusicValues().repeatStatus = RepeatType.REPEAT;
                        requestManager.load(R.drawable.repeat_black_48dp).into(SongPlayRepeat);
                        SongPlayRepeat.setColorFilter(Color.parseColor(ButtonHighlighted));
                        break;
                    case REPEAT:
                        getPlayInstance().getMusicValues().repeatStatus = RepeatType.REPEAT_ONCE;
                        requestManager.load(R.drawable.repeat_one_black_48dp).into(SongPlayRepeat);
                        SongPlayRepeat.setColorFilter(Color.parseColor(ButtonHighlighted));
                        break;
                    case REPEAT_ONCE:
                        getPlayInstance().getMusicValues().repeatStatus = RepeatType.SINGLE;
                        requestManager.load(R.drawable.repeat_black_48dp).into(SongPlayRepeat);
                        SongPlayRepeat.setColorFilter(Color.parseColor(ButtonColor));
                        break;
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser && getPlayInstance().getMusicValues().mediaPlayer != null){
                    getPlayInstance().getMusicValues().mediaPlayer.seekTo(progress * 1000);
                    getPlayInstance().getMusicValues().mediaSession.setPlaybackState(getPlayInstance().getMusicValues().playbackStateBuilder.setState(getPlayInstance().getMusicValues().mediaPlayer.isPlaying() ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED, getPlayInstance().getMusicValues().mediaPlayer.getCurrentPosition(), 1).build());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        QueueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(PlayBar.getVisibility() == View.GONE){
                    OpenQueueLayout(true);
                }
                else{
                    OpenQueueLayout(false);
                }
            }
        });

        Overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getContext(), v);
                MenuInflater inflater = popupMenu.getMenuInflater();

                inflater.inflate(R.menu.queue_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        List<Song> selectedSongs = new ArrayList<Song>();
                        switch (item.getItemId()) {
                            case R.id.action_AddtoPlaylist:
                                getPlayInstance().itemToAdd = getPlayInstance().selectedSong;
                                Intent intent = new Intent(getContext(), AddToPlaylistActivity.class);
                                startActivity(intent);
                                break;
                            case R.id.action_GotoArtist:
                                getPlayInstance().selectedGroup = getPlayInstance().artists.stream().filter(x -> x.GetName().equals(getPlayInstance().selectedSong.GetArtist())).findFirst().get();
                                Intent artist = new Intent(getContext(), AlbumGroupActivity.class);
                                startActivity(artist);
                                break;
                            case R.id.action_GotoAlbum:
                                getPlayInstance().selectedAlbum = getPlayInstance().albums.stream().filter(x -> x.GetName().equals(getPlayInstance().selectedSong.GetAlbum())).findFirst().get();
                                Intent album = new Intent(getContext(), AlbumViewActivity.class);
                                startActivity(album);
                                break;
                            case R.id.action_ClearQueue:
                                getPlayInstance().getMusicValues().SongQueue.clear();
                                adapter.setData(getPlayInstance().getMusicValues().SongQueue);
                                break;
                            case R.id.action_SaveQueue:
                                getPlayInstance().itemToAdd = getPlayInstance().getMusicValues().SongQueue;
                                Intent addQueue = new Intent(getContext(), AddToPlaylistActivity.class);
                                startActivity(addQueue);
                                break;
                            case R.id.action_Edit:
                                getPlayInstance().itemToEdit = getPlayInstance().selectedSong;
                                Intent edit = new Intent(getContext(), EditActivity.class);
                                startActivity(edit);
                                break;
                            case R.id.action_Delete:
                                AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                        .setTitle("Delete")
                                        .setMessage("Delete " + getPlayInstance().selectedSong.GetTitle())
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                getPlayInstance().deleteSong(getPlayInstance().selectedSong);
                                                Intent intent = new Intent(requireContext(), PlayMusicService.class);
                                                intent.setAction(PlayMusicService.ActionForward);
                                                requireContext().startService(intent);
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

        handler = new Handler(Looper.getMainLooper());

        task = new Runnable() {
            @Override
            public void run() {
                if(getPlayInstance().getMusicValues().mediaPlayer != null) {
                    int position = getPlayInstance().getMusicValues().mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(getPlayInstance().getMusicValues().mediaPlayer.getCurrentPosition() / 1000, true);
                    currentPosition.setText(MainActivity.ConvertTime(getPlayInstance().getMusicValues().mediaPlayer.getCurrentPosition()));
                }
                handler.postDelayed(this, 1000);
            }
        };

        handler.post(task);

        SetSelectedSong(getPlayInstance().getMusicValues().mediaPlayer.isPlaying());

        return root;
    }

    @Override
    public void onDestroy() {
        //thread.interrupt();

        handler.removeCallbacks(task);
        //MainActivity.musicValues.mediaPlayer.setIsPlayingListener(null);
        super.onDestroy();
    }

    public SongFragment(){

    }

    public SongFragment(Context context, RequestManager requestManager){
        //this.context = context;
        //this.requestManager = requestManager;
        //this.requestManager = Glide.with(this);
    }

    public void setOnPlayChangedListener(IsPlayingListener listener){
        isPlayingListener = listener;
    }

    public void SetSelectedSong(boolean isPlaying){
        if(getPlayInstance().selectedSong != null) {
            requestManager.load(getPlayInstance().selectedSong.Art).into(AlbumImageNow);
            SongNow.setText(getPlayInstance().selectedSong.Title);
            ArtistNow.setText(getPlayInstance().selectedSong.Artist);

            requestManager.load(getPlayInstance().selectedSong.Art).into(SongArt);
            try {
                seekBar.setMax(getPlayInstance().getMusicValues().mediaPlayer.getDuration() / 1000);
            }
            catch(Exception ex){

            }
            duration.setText(getPlayInstance().selectedSong.SongLength);

            if(!getPlayInstance().getMusicValues().shuffled){
                SongPlayShuffle.setColorFilter(Color.parseColor(ButtonColor));
            }
            else{
                SongPlayShuffle.setColorFilter(Color.parseColor(ButtonHighlighted));
            }

            if(getPlayInstance().selectedSong.GetNowPlayingSource().equals(AdapterType.SONG)) {
                nowPlayingImagesMulti.setVisibility(View.VISIBLE);
                ArtFull.setVisibility(View.GONE);
                nowPlayingSource1.setText("All Songs");
                nowPlayingSource2.setText("");

                List<Song> distinctSongs = getPlayInstance().getMusicValues().SongQueue.stream().filter(distinctByKey(Song::GetArt)).filter(x -> x.GetNowPlayingSource().equals(AdapterType.SONG)).collect(Collectors.toList());

                if(distinctSongs.size() > 0){
                    requestManager.load(distinctSongs.get(0).GetArt()).into(Art1);
                    if (distinctSongs.size() > 1){
                        requestManager.load(distinctSongs.get(1).GetArt()).into(Art2);
                        if (distinctSongs.size() > 2){
                            requestManager.load(distinctSongs.get(2).GetArt()).into(Art3);
                            if (distinctSongs.size() > 3){
                                requestManager.load(distinctSongs.get(3).GetArt()).into(Art4);
                            }
                            else{
                                requestManager.load(distinctSongs.get(0).GetArt()).into(Art4);
                            }
                        }
                        else{
                            requestManager.load(distinctSongs.get(1).GetArt()).into(Art4);
                            requestManager.load(distinctSongs.get(0).GetArt()).into(Art3);
                        }
                    }
                    else{
                        requestManager.load(distinctSongs.get(0).GetArt()).into(Art2);
                        requestManager.load(distinctSongs.get(0).GetArt()).into(Art3);
                        requestManager.load(distinctSongs.get(0).GetArt()).into(Art4);
                    }
                }
            }
            else if (getPlayInstance().selectedSong.GetNowPlayingSource().equals(AdapterType.ALBUM)){
                nowPlayingImagesMulti.setVisibility(View.GONE);
                ArtFull.setVisibility(View.VISIBLE);
                nowPlayingSource1.setText(getPlayInstance().selectedSong.Album);
                nowPlayingSource2.setText("- " + getPlayInstance().selectedSong.Artist);

                requestManager.load(getPlayInstance().selectedSong.Art).into(ArtFull);
            }
            else if (getPlayInstance().selectedSong.GetNowPlayingSource().equals(AdapterType.PLAYLIST)){
                nowPlayingImagesMulti.setVisibility(View.VISIBLE);
                ArtFull.setVisibility(View.GONE);
                nowPlayingSource1.setText(getPlayInstance().selectedPlaylist.GetName());
                nowPlayingSource2.setText("");

                List<Song> distinctSongs = getPlayInstance().getMusicValues().SongQueue.stream().filter(distinctByKey(Song::GetArt)).filter(x -> x.GetNowPlayingSource().equals(AdapterType.PLAYLIST)).collect(Collectors.toList());

                if(distinctSongs.size() > 0){
                    requestManager.load(distinctSongs.get(0).GetArt()).into(Art1);
                    if (distinctSongs.size() > 1){
                        requestManager.load(distinctSongs.get(1).GetArt()).into(Art2);
                        if (distinctSongs.size() > 2){
                            requestManager.load(distinctSongs.get(2).GetArt()).into(Art3);
                            if (distinctSongs.size() > 3){
                                requestManager.load(distinctSongs.get(3).GetArt()).into(Art4);
                            }
                            else{
                                requestManager.load(distinctSongs.get(0).GetArt()).into(Art4);
                            }
                        }
                        else{
                            requestManager.load(distinctSongs.get(1).GetArt()).into(Art4);
                            requestManager.load(distinctSongs.get(0).GetArt()).into(Art3);
                        }
                    }
                    else{
                        requestManager.load(distinctSongs.get(0).GetArt()).into(Art2);
                        requestManager.load(distinctSongs.get(0).GetArt()).into(Art3);
                        requestManager.load(distinctSongs.get(0).GetArt()).into(Art4);
                    }
                }
            }

            if (isPlaying){
                requestManager.load(R.drawable.pause_black_48dp).into(SongPlayButton);
                requestManager.load(R.drawable.pause_black_48dp).into(PlayButton);

                //SongQueue.smoothScrollToPosition(getPlayInstance().getMusicValues().currentSongIndex);

                if(getPlayInstance().recents.stream().anyMatch(x -> x.Name.equals(getPlayInstance().selectedSong.Album))){
                    getPlayInstance().recents.remove(getPlayInstance().recents.stream().filter(x -> x.Name.equals(getPlayInstance().selectedSong.Album)).findFirst().get());
                }
                Collections.reverse(getPlayInstance().recents);
                getPlayInstance().recents.add(getPlayInstance().albums.stream().filter(x -> x.Name.equals(getPlayInstance().selectedSong.Album)).findFirst().get());
                Collections.reverse(getPlayInstance().recents);
                if(getPlayInstance().recents.size() > 20){
                    getPlayInstance().recents.remove(0);
                }
                getPlayInstance().editor.putString("recents", new Gson().toJson(getPlayInstance().recents));
                getPlayInstance().editor.commit();
            }
            else{
                requestManager.load(R.drawable.play_arrow_black_48dp).into(SongPlayButton);
                requestManager.load(R.drawable.play_arrow_black_48dp).into(PlayButton);
            }

            adapter.setData(getPlayInstance().getMusicValues().SongQueue);
            adapter.notifyDataSetChanged();
        }
    }

    public void OpenFullLayout(boolean open){
        if(open){
            QueueButton.setVisibility(View.VISIBLE);
            Overflow.setVisibility(View.VISIBLE);
            PlayButton.setVisibility(View.GONE);

            SongNow.setSelected(true);

            SongNameCardParams.removeRule(RelativeLayout.LEFT_OF);
            SongNameCardParams.addRule(RelativeLayout.LEFT_OF, R.id.QueueButton);
        }
        else{
            QueueButton.setVisibility(View.GONE);
            Overflow.setVisibility(View.GONE);
            PlayButton.setVisibility(View.VISIBLE);

            SongNow.setSelected(false);

            SongNameCardParams.removeRule(RelativeLayout.LEFT_OF);
            SongNameCardParams.addRule(RelativeLayout.LEFT_OF, R.id.PlayButton);
            OpenQueueLayout(false);
        }
    }

    public void OpenQueueLayout(boolean open){
        if(open){
            PlayBar.setVisibility(View.VISIBLE);
            QueueButton.setColorFilter(Color.parseColor("#3498db"));
        }
        else{
            PlayBar.setVisibility(View.GONE);
            QueueButton.setColorFilter(Color.parseColor(ButtonColor));
        }
    }

    public void getRecents(){
        getPlayInstance().recents = new Gson().fromJson(getPlayInstance().prefs.getString("recents", ""), new TypeToken<List<Album>>(){}.getType());

        if(getPlayInstance().recents == null){
            getPlayInstance().recents = new ArrayList<Album>();
        }
    }

    @Override
    public void onOverflowClicked(int position, View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view.findViewById(R.id.OverflowMenu));
        int Position = position;
        MenuInflater inflater = popupMenu.getMenuInflater();

        inflater.inflate(R.menu.queue_item_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                List<Song> selectedSongs = new ArrayList<>();
                selectedSongs =  getPlayInstance().getMusicValues().SongQueue.subList(position, getPlayInstance().getMusicValues().SongQueue.size());
                for (Song song : selectedSongs){
                    song.SetNowPlayingSource(AdapterType.SONG);
                }
                switch (item.getItemId()) {
                    case R.id.action_PlayNext:
                        playNext(selectedSongs);
                        break;
                    case R.id.action_AddtoPlaylist:
                        getPlayInstance().itemToAdd = getPlayInstance().getMusicValues().SongQueue.get(position);
                        Intent intent = new Intent(getContext(), AddToPlaylistActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_GotoArtist:
                        getPlayInstance().selectedGroup = getPlayInstance().artists.stream().filter(x -> x.GetName().equals(getPlayInstance().getMusicValues().SongQueue.get(position).GetArtist())).findFirst().get();
                        Intent artist = new Intent(getContext(), AlbumGroupActivity.class);
                        startActivity(artist);
                        break;
                    case R.id.action_GotoAlbum:
                        getPlayInstance().selectedAlbum = getPlayInstance().albums.stream().filter(x -> x.GetName().equals(getPlayInstance().getMusicValues().SongQueue.get(position).GetAlbum())).findFirst().get();
                        Intent album = new Intent(getContext(), AlbumViewActivity.class);
                        startActivity(album);
                        break;
                    case R.id.action_RemoveFromQueue:
                        getPlayInstance().getMusicValues().SongQueue.remove(position);
                        adapter.setData(getPlayInstance().getMusicValues().SongQueue);
                        break;
                    case R.id.action_Edit:
                        getPlayInstance().itemToEdit = getPlayInstance().getMusicValues().SongQueue.get(position);
                        Intent edit = new Intent(getContext(), EditActivity.class);
                        startActivity(edit);
                        break;
                    case R.id.action_Delete:
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                .setTitle("Delete")
                                .setMessage("Delete " + getPlayInstance().getMusicValues().SongQueue.get(position).GetTitle())
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        getPlayInstance().deleteSong(getPlayInstance().getMusicValues().SongQueue.get(position));
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
        getPlayInstance().playFromQueue(position);
    }

    void shuffled(List<Song> selectedSongs, boolean keepFirst){
        getPlayInstance().shuffled(selectedSongs, keepFirst);
    }

    void playNext(List<Song> selectedSongs){
        if (getPlayInstance().playNext(selectedSongs)) {
            SetSelectedSong(getPlayInstance().getMusicValues().mediaPlayer.isPlaying());
        }
    }

    void addToQueue(List<Song> selectedSongs){
        if (getPlayInstance().addToQueue(selectedSongs)){
            SetSelectedSong(getPlayInstance().getMusicValues().mediaPlayer.isPlaying());
        }
    }
}
