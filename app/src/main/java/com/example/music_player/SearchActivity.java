package com.example.music_player;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.TypedValue;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.AutoCompleteTextView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.CollapsibleActionView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.music_player.Classes.Album;
import com.example.music_player.Classes.Group;
import com.example.music_player.Classes.Playlist;
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
import com.example.music_player.ui.SuggestionProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.transform.Result;

import static com.example.music_player.Classes.Singleton.getPlayInstance;

public class SearchActivity extends BaseActivity implements View.OnTouchListener, AlbumClickListener, AllSongsClickListener, IsPlayingListener {
    SearchView search;
    TextView NoResults;
    Toolbar toolbar;
    NestedScrollView nestedScrollView;

    public static List<Group> selectedArtists = new ArrayList<Group>();
    List<Group> selectedArtistsMax4 = new ArrayList<Group>();
    public static List<Album> selectedAlbums = new ArrayList<Album>();
    List<Album> selectedAlbumsMax4 = new ArrayList<Album>();
    public static List<Song> selectedSongs = new ArrayList<Song>();
    List<Song> selectedSongsMax4 = new ArrayList<Song>();
    public static List<Playlist> selectedPlaylists = new ArrayList<Playlist>();
    List<Playlist> selectedPlaylistsMax4 = new ArrayList<Playlist>();

    public static AdapterType selectedMedia;

    RelativeLayout MainLayout;
    RelativeLayout.LayoutParams mainLayoutParams;
    RelativeLayout scrollRelativeLayout;
    RelativeLayout ResultsView;

    RelativeLayout ArtistPanel;
    TextView ArtistMoreButton;
    RelativeLayout ArtistClickPanel;
    RecyclerView Artist;
    GridLayoutManager ArtistManager;
    GroupAdapter ArtistAdapter;

    RelativeLayout AlbumPanel;
    TextView AlbumMoreButton;
    RelativeLayout AlbumClickPanel;
    RecyclerView Album;
    GridLayoutManager AlbumManager;
    AlbumAdapter AlbumAdapter;

    RelativeLayout SongPanel;
    TextView SongMoreButton;
    RelativeLayout SongClickPanel;
    RecyclerView Song;
    LinearLayoutManager SongManager;
    AllSongsAdapter SongAdapter;

    RelativeLayout PlaylistPanel;
    TextView PlaylistMoreButton;
    RelativeLayout PlaylistClickPanel;
    RecyclerView Playlist;
    GridLayoutManager PlaylistManager;
    PlaylistAdapter PlaylistAdapter;

    public RelativeLayout songLayout;
    OvershootInterpolator interpolator;
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    SongFragment fragment;
    float lastPositionY;
    float lastTransformY;
    float orientation;
    float width;
    float height;

    RequestManager requestManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Music_Player_NoActionBar2);
        setContentView(R.layout.search_layout);

        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark, getTheme()));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        AppBarLayout appBarLayout = findViewById(R.id.AppToolbar);

        requestManager = Glide.with(this);

        width = getResources().getDisplayMetrics().widthPixels;
        height = getResources().getDisplayMetrics().heightPixels;

        nestedScrollView = findViewById(R.id.nestedScrollView1);
        songLayout = findViewById(R.id.SongLayoutView);
        ResultsView = findViewById(R.id.ResultsView);
        MainLayout = findViewById(R.id.MainLayout);

        Artist = findViewById(R.id.ArtistRecyclerView);
        ArtistPanel = findViewById(R.id.ArtistPanel);
        ArtistMoreButton = findViewById(R.id.MoreArtistButton);
        ArtistClickPanel = findViewById(R.id.ArtistClickPanel);
        Album = findViewById(R.id.AlbumRecycler);
        AlbumPanel = findViewById(R.id.AlbumsPanel);
        AlbumMoreButton = findViewById(R.id.MoreAlbumButton);
        AlbumClickPanel = findViewById(R.id.AlbumClickPanel);
        Song = findViewById(R.id.SongRecyclerView);
        SongPanel = findViewById(R.id.SongsPanel);
        SongMoreButton = findViewById(R.id.MoreSongButton);
        SongClickPanel = findViewById(R.id.SongClickPanel);
        Playlist = findViewById(R.id.PlaylistRecyclerView);
        PlaylistPanel = findViewById(R.id.PlaylistsPanel);
        PlaylistMoreButton = findViewById(R.id.MorePlaylistButton);
        PlaylistClickPanel = findViewById(R.id.PlaylistClickPanel);

        search = findViewById(R.id.Search);
        NoResults = findViewById(R.id.NoResults);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        search.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        search.setIconifiedByDefault(false);
        search.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                search.setQuery(search.getSuggestionsAdapter().convertToString(search.getSuggestionsAdapter().getCursor()), true);
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                search.setQuery(search.getSuggestionsAdapter().convertToString(search.getSuggestionsAdapter().getCursor()), true);
                return true;
            }
        });

        search.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return false;
            }
        });

        mainLayoutParams = (RelativeLayout.LayoutParams)MainLayout.getLayoutParams();

        interpolator = new OvershootInterpolator(0);
        songLayout.setTranslationY(height);
        songLayout.setOnTouchListener(this);

        fragment = new SongFragment(this, requestManager);
        fragmentTransaction.add(R.id.SongFragmentContainer, fragment);
        fragmentTransaction.commit();


        Artist.addItemDecoration(new GridSpacingItemDecoration(2, 30, true));
        Album.addItemDecoration(new GridSpacingItemDecoration(2, 30, true));
        Artist.setOverScrollMode(View.OVER_SCROLL_NEVER);
        Album.setOverScrollMode(View.OVER_SCROLL_NEVER);
        Song.setOverScrollMode(View.OVER_SCROLL_NEVER);

        search.requestFocus();
        //OpenSearchSuggestionsList(search);

//        Intent intent = getIntent();
//        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
//            String query = intent.getStringExtra(SearchManager.QUERY);
//            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
//                    SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
//            suggestions.saveRecentQuery(query, null);
//        }
    }

    private void OpenSearchSuggestionsList(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            try {
                if (child instanceof ViewGroup) {
                    OpenSearchSuggestionsList((ViewGroup) child);
                } else if (child instanceof AutoCompleteTextView) {
                    // Found the right child - show dropdown
                    ((AutoCompleteTextView) child).showDropDown();
                    break; // We're done
                }
            }
            catch(Exception ex) {

            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);

            String queryLowerCase = query.toLowerCase();
            getPlayInstance().Search = query;

            selectedArtists = getPlayInstance().artists.stream().filter((x) -> x.GetName().toLowerCase().contains(queryLowerCase)).collect(Collectors.toList());
            selectedAlbums = getPlayInstance().albums.stream().filter((x) -> x.GetName().toLowerCase().contains(queryLowerCase) || x.GetAlbumArtist().toLowerCase().contains(queryLowerCase) || x.GetGenre().toLowerCase().contains(queryLowerCase)).collect(Collectors.toList());
            selectedSongs = getPlayInstance().songs.stream().filter((x) -> x.GetTitle().toLowerCase().contains(queryLowerCase) || x.GetAlbum().toLowerCase().contains(queryLowerCase) || x.GetAlbumArtist().toLowerCase().contains(queryLowerCase) || x.GetArtist().toLowerCase().contains(queryLowerCase) || x.GetGenre().toLowerCase().contains(queryLowerCase)).collect(Collectors.toList());
            selectedPlaylists.clear();
            for(Playlist playlist : getPlayInstance().playlists){
                if(playlist.GetName().toLowerCase().contains(queryLowerCase) || playlist.PlaylistSongs.stream().anyMatch(x -> getPlayInstance().songs.stream().filter(y -> y.SongID.equals(x.SongID)).findFirst().get().GetTitle().toLowerCase().contains(queryLowerCase) || getPlayInstance().songs.stream().filter(y -> y.SongID.equals(x.SongID)).findFirst().get().GetAlbum().toLowerCase().contains(queryLowerCase) || getPlayInstance().songs.stream().filter(y -> y.SongID.equals(x.SongID)).findFirst().get().GetArtist().toLowerCase().contains(queryLowerCase) || getPlayInstance().songs.stream().filter(y -> y.SongID.equals(x.SongID)).findFirst().get().GetAlbumArtist().toLowerCase().contains(queryLowerCase) || getPlayInstance().songs.stream().filter(y -> y.SongID.equals(x.SongID)).findFirst().get().GetGenre().toLowerCase().contains(queryLowerCase))) {
                    selectedPlaylists.add(playlist);
                }
            }

            ResultsView.setVisibility(View.VISIBLE);

            if(selectedArtists.size() == 0){
                ArtistPanel.setVisibility(View.GONE);
            }
            else if(selectedArtists.size() > 4){
                ArtistPanel.setVisibility(View.VISIBLE);
                selectedArtistsMax4 = selectedArtists.subList(0, 4);
                ArtistMoreButton.setVisibility(View.VISIBLE);
                ArtistMoreButton.setText((selectedArtists.size() - 4) + " MORE");
            }
            else{
                ArtistPanel.setVisibility(View.VISIBLE);
                selectedArtistsMax4 = selectedArtists;
                ArtistMoreButton.setVisibility(View.GONE);
            }

            if(selectedAlbums.size() == 0){
                AlbumPanel.setVisibility(View.GONE);
            }
            else if(selectedAlbums.size() > 4){
                AlbumPanel.setVisibility(View.VISIBLE);
                selectedAlbumsMax4 = selectedAlbums.subList(0, 4);
                AlbumMoreButton.setVisibility(View.VISIBLE);
                AlbumMoreButton.setText((selectedAlbums.size() - 4) + " MORE");
            }
            else{
                AlbumPanel.setVisibility(View.VISIBLE);
                selectedAlbumsMax4 = selectedAlbums;
                AlbumMoreButton.setVisibility(View.GONE);
            }

            if(selectedSongs.size() == 0){
                SongPanel.setVisibility(View.GONE);
            }
            else if(selectedSongs.size() > 4){
                SongPanel.setVisibility(View.VISIBLE);
                selectedSongsMax4 = selectedSongs.subList(0, 4);
                SongMoreButton.setVisibility(View.VISIBLE);
                SongMoreButton.setText((selectedSongs.size() - 4) + " MORE");
            }
            else{
                SongPanel.setVisibility(View.VISIBLE);
                selectedSongsMax4 = selectedSongs;
                SongMoreButton.setVisibility(View.GONE);
            }

            if(selectedPlaylists.size() == 0){
                PlaylistPanel.setVisibility(View.GONE);
            }
            else if(selectedAlbums.size() > 4){
                PlaylistPanel.setVisibility(View.VISIBLE);
                selectedPlaylistsMax4 = selectedPlaylists.subList(0, 4);
                PlaylistMoreButton.setVisibility(View.VISIBLE);
                PlaylistMoreButton.setText((selectedPlaylists.size() - 4) + " MORE");
            }
            else{
                PlaylistPanel.setVisibility(View.VISIBLE);
                selectedPlaylistsMax4 = selectedPlaylists;
                PlaylistMoreButton.setVisibility(View.GONE);
            }
            //PlaylistPanel.setVisibility(View.GONE);

            if(selectedArtists.size() != 0) {
                ArtistClickPanel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedMedia = AdapterType.ARTIST;
                        Intent intent = new Intent(v.getContext(), SearchGroupActivity.class);
                        startActivity(intent);
                    }
                });
                ArtistMoreButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedMedia = AdapterType.ARTIST;
                        Intent intent = new Intent(v.getContext(), SearchGroupActivity.class);
                        startActivity(intent);
                    }
                });
            }

            if(selectedAlbums.size() != 0) {
                AlbumClickPanel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedMedia = AdapterType.ALBUM;
                        Intent intent = new Intent(v.getContext(), SearchGroupActivity.class);
                        startActivity(intent);
                    }
                });
                AlbumMoreButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedMedia = AdapterType.ALBUM;
                        Intent intent = new Intent(v.getContext(), SearchGroupActivity.class);
                        startActivity(intent);
                    }
                });
            }

            if(selectedSongs.size() != 0) {
                SongClickPanel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedMedia = AdapterType.SONG;
                        Intent intent = new Intent(v.getContext(), SearchGroupActivity.class);
                        startActivity(intent);
                    }
                });
                SongMoreButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedMedia = AdapterType.SONG;
                        Intent intent = new Intent(v.getContext(), SearchGroupActivity.class);
                        startActivity(intent);
                    }
                });
            }

            if(selectedPlaylists.size() != 0) {
                PlaylistClickPanel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedMedia = AdapterType.PLAYLIST;
                        Intent intent = new Intent(v.getContext(), SearchGroupActivity.class);
                        startActivity(intent);
                    }
                });
                PlaylistMoreButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedMedia = AdapterType.PLAYLIST;
                        Intent intent = new Intent(v.getContext(), SearchGroupActivity.class);
                        startActivity(intent);
                    }
                });
            }

            if(selectedArtists.size() == 0 && selectedAlbums.size() == 0 && selectedSongs.size() == 0 && selectedPlaylists.size() == 0){
                NoResults.setVisibility(View.VISIBLE);
            }
            else {
                NoResults.setVisibility(View.GONE);
            }

            ArtistManager = new GridLayoutManager(this, 2);
            Artist.setLayoutManager(ArtistManager);
            ArtistAdapter = new GroupAdapter(selectedArtistsMax4, this, this, AdapterType.ARTIST);
            Artist.setAdapter(ArtistAdapter);
            //
            AlbumManager = new GridLayoutManager(this, 2);
            Album.setLayoutManager(AlbumManager);
            AlbumAdapter = new AlbumAdapter(selectedAlbumsMax4, requestManager, this, this, AdapterType.ALBUM);
            Album.setAdapter(AlbumAdapter);
            //
            SongManager = new LinearLayoutManager(this);
            Song.setLayoutManager(SongManager);
            SongAdapter = new AllSongsAdapter(selectedSongsMax4, this, this);
            Song.setAdapter(SongAdapter);
            //
            PlaylistManager = new GridLayoutManager(this, 2);
            Playlist.setLayoutManager(PlaylistManager);
            PlaylistAdapter = new PlaylistAdapter(selectedPlaylists, this, this);
            Playlist.setAdapter(PlaylistAdapter);
        }
        else{
            super.onNewIntent(intent);
        }
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
        nestedScrollView.scrollTo(0, 0);

        if(getPlayInstance().selectedSong != null){
            songLayout.setTranslationY(height - PixelFromDP(75));

            mainLayoutParams.bottomMargin = PixelFromDP(75);

            fragment.SetSelectedSong(getPlayInstance().getMusicValues().mediaPlayer.isPlaying());
            fragment.OpenFullLayout(false);
        }

        super.onResume();
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

    void shuffled(List<Song> selectedAllSongs, boolean keepFirst){
        getPlayInstance().shuffled(selectedAllSongs, keepFirst);
    }

    void playNext(List<Song> selectedAllSongs){
        if (getPlayInstance().playNext(selectedAllSongs)) {
            fragment.SetSelectedSong(getPlayInstance().getMusicValues().mediaPlayer.isPlaying());
        }
    }

    void addToQueue(List<Song> selectedAllSongs){
        if (getPlayInstance().addToQueue(selectedAllSongs)){
            fragment.SetSelectedSong(getPlayInstance().getMusicValues().mediaPlayer.isPlaying());
        }
    }

    void playMusic(List<Song> selectedAllSongs, boolean keepFirst){
        getPlayInstance().playMusic(selectedAllSongs, keepFirst);
    }

    @Override
    public void onOverflowClicked(int position, View view, AdapterType type) {
        PopupMenu popupMenu = new PopupMenu(this, view.findViewById(R.id.OverflowMenu));
        MenuInflater inflater = popupMenu.getMenuInflater();
        Context context = this;

        switch (type) {
            case ALBUM:
                getPlayInstance().selectedAlbum = selectedAlbums.get(position);
                inflater.inflate(R.menu.album_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        List<Song> selectedAllSongs = new ArrayList<Song>();
                        selectedAllSongs = selectedSongs.stream().filter((x) -> x.Album.equals(getPlayInstance().selectedAlbum.GetName())).collect(Collectors.toList());
                        Collections.sort(selectedAllSongs, Comparator.comparingInt(p -> p.GetSongNumberInt()));
                        Collections.sort(selectedAllSongs, Comparator.comparingInt(p -> p.GetDiscNumberInt()));
                        for (Song song : selectedAllSongs){
                            song.SetNowPlayingSource(AdapterType.ALBUM);
                        }
                        switch (item.getItemId()) {
                            case R.id.action_Shuffle:
                                shuffled(selectedAllSongs, false);
                                break;
                            case R.id.action_PlayNext:
                                playNext(selectedAllSongs);
                                break;
                            case R.id.action_AddtoQueue:
                                addToQueue(selectedAllSongs);
                                break;
                            case R.id.action_AddtoPlaylist:
                                getPlayInstance().itemToAdd = selectedAlbums.get(position);
                                Intent intent = new Intent(context, AddToPlaylistActivity.class);
                                context.startActivity(intent);
                                break;
                            case R.id.action_GotoArtist:
                                getPlayInstance().selectedGroup = selectedArtists.stream().filter(x -> x.GetName().equals(selectedAlbums.get(position).GetAlbumArtist())).findFirst().get();
                                Intent artist = new Intent(context, AlbumGroupActivity.class);
                                context.startActivity(artist);
                                break;
                            case R.id.action_Edit:
                                getPlayInstance().itemToEdit = selectedAlbums.get(position);
                                Intent edit = new Intent(getBaseContext(), EditActivity.class);
                                startActivity(edit);
                                break;
                            case R.id.action_Delete:
                                AlertDialog alertDialog = new AlertDialog.Builder(context)
                                        .setTitle("Delete")
                                        .setMessage("Delete " + getPlayInstance().selectedAlbum.Name)
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
                getPlayInstance().selectedGroup = selectedArtists.get(position);
                inflater.inflate(R.menu.group_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        List<Song> selectedAllSongs = new ArrayList<Song>();
                        selectedAllSongs = selectedSongs.stream().filter((x) -> x.Artist.equals(getPlayInstance().selectedGroup.GetName())).collect(Collectors.toList());
                        Collections.sort(selectedAllSongs, Comparator.comparing(p -> p.Title.toLowerCase()));
                        for (Song song : selectedAllSongs){
                            song.SetNowPlayingSource(AdapterType.SONG);
                        }
                        switch (item.getItemId()) {
                            case R.id.action_Shuffle:
                                shuffled(selectedAllSongs, false);
                                break;
                            case R.id.action_PlayNext:
                                playNext(selectedAllSongs);
                                break;
                            case R.id.action_AddtoQueue:
                                addToQueue(selectedAllSongs);
                                break;
                        }

                        return true;
                    }
                });
                popupMenu.show();
                break;
            case PLAYLIST:
                getPlayInstance().selectedPlaylist = selectedPlaylists.get(position);
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
                                        .setMessage("Delete " + getPlayInstance().selectedPlaylist.GetName())
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                getPlayInstance().deletePlaylist(getPlayInstance().selectedPlaylist);
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
        List<Song> selectedAllSongs = new ArrayList<Song>();
        switch(type){
            case ALBUM:
                getPlayInstance().selectedAlbum = selectedAlbums.get(position);
                selectedAllSongs = getPlayInstance().songs.stream().filter((x) -> x.Album.equals(getPlayInstance().selectedAlbum.GetName())).collect(Collectors.toList());
                Collections.sort(selectedAllSongs, Comparator.comparingInt(p -> p.GetSongNumberInt()));
                Collections.sort(selectedAllSongs, Comparator.comparingInt(p -> p.GetDiscNumberInt()));
                for (Song song : selectedAllSongs){
                    song.SetNowPlayingSource(AdapterType.ALBUM);
                }
                break;
            case ARTIST:
                getPlayInstance().selectedGroup = selectedArtists.get(position);
                selectedAllSongs = getPlayInstance().songs.stream().filter((x) -> x.Artist.equals(getPlayInstance().selectedGroup.GetName())).collect(Collectors.toList());
                Collections.sort(selectedAllSongs, Comparator.comparing(p -> p.Title.toLowerCase()));
                for (Song song : selectedAllSongs){
                    song.SetNowPlayingSource(AdapterType.SONG);
                }
                break;
            case PLAYLIST:
                getPlayInstance().selectedPlaylist = selectedPlaylists.get(position);
                Collections.sort(getPlayInstance().selectedPlaylist.PlaylistSongs, Comparator.comparingInt(p -> p.PlaylistSort));
                for (PlaylistSong playlistSong : getPlayInstance().selectedPlaylist.PlaylistSongs){
                    Song song = getPlayInstance().songs.stream().filter(x -> x.SongID.equals(playlistSong.SongID)).findFirst().get();
                    song.SetNowPlayingSource(AdapterType.PLAYLIST);
                    selectedAllSongs.add(song);
                }
                break;
        }

        if(selectedAllSongs.size() != 0){
            playMusic(selectedAllSongs, false);
        }
    }

    @Override
    public void onItemViewClicked(int position, AdapterType type) {
        switch(type){
            case ALBUM:
                getPlayInstance().selectedAlbum = selectedAlbums.get(position);
                Intent album = new Intent(this, AlbumViewActivity.class);
                startActivity(album);
                break;
            case ARTIST:
                getPlayInstance().selectedGroup = selectedArtists.get(position);
                Intent artist = new Intent(this, AlbumGroupActivity.class);
                startActivity(artist);
                break;
            case PLAYLIST:
                getPlayInstance().selectedPlaylist = selectedPlaylists.get(position);
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
                List<Song> selectedAllSongs = new ArrayList<>();
                selectedAllSongs =  selectedSongs.subList(position, selectedSongs.size());
                Collections.sort(selectedAllSongs, Comparator.comparing(p -> p.Title.toLowerCase()));
                for (Song song : selectedAllSongs){
                    song.SetNowPlayingSource(AdapterType.SONG);
                }
                switch (item.getItemId()) {
                    case R.id.action_Shuffle:
                        shuffled(selectedAllSongs, true);
                        break;
                    case R.id.action_PlayNext:
                        playNext(selectedSongs);
                        break;
                    case R.id.action_AddtoQueue:
                        addToQueue(selectedAllSongs);
                        break;
                    case R.id.action_AddtoPlaylist:
                        getPlayInstance().itemToAdd = selectedSongs.get(position);
                        Intent intent = new Intent(context, AddToPlaylistActivity.class);
                        context.startActivity(intent);
                        break;
                    case R.id.action_GotoArtist:
                        getPlayInstance().selectedGroup = selectedArtists.stream().filter(x -> x.GetName().equals(selectedSongs.get(position).GetArtist())).findFirst().get();
                        Intent artist = new Intent(context, AlbumGroupActivity.class);
                        startActivity(artist);
                        break;
                    case R.id.action_GotoAlbum:
                        getPlayInstance().selectedAlbum = selectedAlbums.stream().filter(x -> x.GetName().equals(selectedSongs.get(position).GetAlbum())).findFirst().get();
                        Intent album = new Intent(context, AlbumViewActivity.class);
                        startActivity(album);
                        break;
                    case R.id.action_Edit:
                        getPlayInstance().itemToEdit = selectedSongs.get(position);
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
        List<Song> selectedAllSongs = selectedSongs.subList(position, getPlayInstance().songs.size());
        for (Song song : selectedAllSongs){
            song.SetNowPlayingSource(AdapterType.SONG);
        }
        playMusic(selectedAllSongs, true);
    }

    @Override
    public void onPlayChanged(boolean playing) {
        if(fragment != null) {
            fragment.SetSelectedSong(playing);
        }

        if(mainLayoutParams.bottomMargin != PixelFromDP(75)) {
            //songLayout.setTranslationY(height - PixelFromDP(75));
            songLayout.animate().setInterpolator(interpolator).translationY(height - PixelFromDP(75)).setDuration(500);

            mainLayoutParams.bottomMargin = PixelFromDP(75);
        }
    }
}