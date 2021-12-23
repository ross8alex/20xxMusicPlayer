package com.example.music_player;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.music_player.Classes.Album;
import com.example.music_player.Classes.Group;
import com.example.music_player.Classes.PlayMusicValues;
import com.example.music_player.Classes.Playlist;
import com.example.music_player.Classes.PlaylistSong;
import com.example.music_player.Classes.Song;
import com.example.music_player.DriveHelpers.DownloaderAsyncTask;
import com.example.music_player.DriveHelpers.DriveServiceHelper;
import com.example.music_player.DriveHelpers.MyAsyncTask;
import com.example.music_player.MusicService.IsPlayingListener;
import com.example.music_player.MusicService.PlayMusicService;
import com.example.music_player.enums.GroupType;
import com.example.music_player.interfaces.UpdateQueueListener;
import com.example.music_player.ui.SongFragment;
import com.example.music_player.ui.ViewPagerAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static androidx.core.content.ContentProviderCompat.requireContext;
import static com.example.music_player.Classes.Singleton.getPlayInstance;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnTouchListener, IsPlayingListener {
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private FirebaseAuth mAuth;
    static FirebaseUser currentUser;

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    final int RC_SIGN_IN = 100;
    Scope ACCESS_DRIVE_SCOPE = new Scope(DriveScopes.DRIVE);
    Scope SCOPE_EMAIL = new Scope(Scopes.EMAIL);
    public static DriveServiceHelper mDriveServiceHelper;
    public static Drive googleDriveService;
    GoogleSignInClient mGoogleSignInClient;
    TextView GoogleEmail;
    TextView GoogleName;
    ImageView GoogleImage;

    InputMethodManager inputMethodManager;

    Toolbar toolbar;
    androidx.appcompat.widget.SearchView SearchView;
    CardView SearchWindow;
    RecyclerView SearchResults;
    View backgroundGray;
    View backgroundDummy;
    LinearLayoutManager searchLayoutManager;
    private ItemTouchHelper mItemTouchHelper;

    RequestManager requestManager;

    public TabLayout tabs;
    private ViewPager pager;

    public static Object itemToEdit;
    public static Object itemToDelete;
    float width;
    float height;
    float orientation;
    float lastPositionY;
    float lastTransformY;

    public RelativeLayout songLayout;
    public SearchView search;
    public View greyBackground;

    OvershootInterpolator interpolator;
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    SongFragment fragment;

    public ViewPagerAdapter viewPagerAdapter;

    public Intent serviceIntent;

    LinearLayout.LayoutParams pagerParams;

    private AppBarConfiguration mAppBarConfiguration;
    private Bundle savedInstanceState;

    private SongFragment songFragment;

    ValueEventListener songListener;
    ValueEventListener albumListener;
    ValueEventListener playlistListener;
    ValueEventListener playlistSongListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPlayInstance().prefs = PreferenceManager.getDefaultSharedPreferences(this);
        getPlayInstance().editor = getPlayInstance().prefs.edit();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        inputMethodManager = ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));

        width = getResources().getDisplayMetrics().widthPixels;
        height = getResources().getDisplayMetrics().heightPixels;

        requestManager = Glide.with(this);

        setTheme(R.style.Theme_Music_Player_NoActionBar);
        setTitle("20XX Java Music Player");
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View hView = navigationView.getHeaderView(0);

        GoogleEmail = hView.findViewById(R.id.GoogleEmail);
        GoogleName = hView.findViewById(R.id.GoogleName);
        GoogleImage = hView.findViewById(R.id.GoogleImage);

        songListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getPlayInstance().songs.clear();
                getPlayInstance().artists.clear();
                getPlayInstance().genres.clear();

                for (DataSnapshot songSnapshot : snapshot.getChildren()) {
                    Song song = songSnapshot.getValue(Song.class);
                    song.setNumbers();
                    getPlayInstance().songs.add(song);

                    if (!getPlayInstance().artists.stream().anyMatch((x) -> x.GetName().equals(song.GetArtist()))) {
                        if (song.GetArtist() != "") {
                            Group artist = new Group();
                            artist.Name = song.Artist;
                            artist.type = GroupType.ARTIST;
                            artist.Songs.add(song);
                            getPlayInstance().artists.add(artist);
                        }
                    } else {
                        Group artist = getPlayInstance().artists.stream().filter((x) -> x.GetName().equals(song.Artist)).findFirst().get();
                        artist.Songs.add(song);
                    }

                    if (!getPlayInstance().genres.stream().anyMatch((x) -> x.GetName().equals(song.GetGenre()))) {
                        if (song.GetGenre() != "") {
                            Group genre = new Group();
                            genre.Name = song.Genre;
                            genre.type = GroupType.GENRE;
                            genre.Songs.add(song);
                            getPlayInstance().genres.add(genre);
                        }
                    } else {
                        Group genre = getPlayInstance().genres.stream().filter((x) -> x.GetName().equals(song.GetGenre())).findFirst().get();
                        genre.Songs.add(song);
                    }
                }
                Collections.sort(getPlayInstance().songs, Comparator.comparing(p -> p.GetTitle().toLowerCase()));
                Collections.sort(getPlayInstance().artists, Comparator.comparing(p -> p.GetName().toLowerCase()));
                Collections.sort(getPlayInstance().albums, Comparator.comparing(p -> p.GetName().toLowerCase()));
                Collections.sort(getPlayInstance().genres, Comparator.comparing(p -> p.GetName().toLowerCase()));

                if (viewPagerAdapter.songAdapter != null)
                    viewPagerAdapter.songAdapter.notifyDataSetChanged();
                if (viewPagerAdapter.artistAdapter != null)
                    viewPagerAdapter.artistAdapter.notifyDataSetChanged();
                if (viewPagerAdapter.albumAdapter != null)
                    viewPagerAdapter.albumAdapter.notifyDataSetChanged();
                if (viewPagerAdapter.genreAdapter != null)
                    viewPagerAdapter.genreAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        albumListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getPlayInstance().albums.clear();

                for (DataSnapshot albumSnapshot : snapshot.getChildren()) {
                    Album album = albumSnapshot.getValue(Album.class);
                    getPlayInstance().albums.add(album);

                    if (!getPlayInstance().genres.stream().anyMatch((x) -> x.GetName().equals(album.GetGenre()))) {
                        if (album.GetGenre() != "") {
                            Group genre = new Group();
                            genre.Name = album.Genre;
                            genre.type = GroupType.GENRE;
                            getPlayInstance().genres.add(genre);
                        }
                    }
                }
                Collections.sort(getPlayInstance().albums, Comparator.comparing(p -> p.GetName().toLowerCase()));
                Collections.sort(getPlayInstance().artists, Comparator.comparing(p -> p.GetName().toLowerCase()));
                Collections.sort(getPlayInstance().genres, Comparator.comparing(p -> p.GetName().toLowerCase()));

                if (viewPagerAdapter.albumAdapter != null)
                    viewPagerAdapter.albumAdapter.notifyDataSetChanged();
                if (viewPagerAdapter.artistAdapter != null)
                    viewPagerAdapter.artistAdapter.notifyDataSetChanged();
                if (viewPagerAdapter.albumAdapter != null)
                    viewPagerAdapter.albumAdapter.notifyDataSetChanged();
                if (viewPagerAdapter.genreAdapter != null)
                    viewPagerAdapter.genreAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        playlistListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot playlistSnapshot : snapshot.getChildren()) {
                    Playlist playlist = playlistSnapshot.getValue(Playlist.class);

                    if(getPlayInstance().playlists.stream().noneMatch(x -> x.ID.equals(playlist.ID))){
                        getPlayInstance().playlists.add(playlist);
                    }
                    else{
                        Playlist playlist1 = getPlayInstance().playlists.stream().filter(x -> x.ID.equals(playlist.ID)).findFirst().get();
                        playlist1.Name = playlist.Name;
                    }
                }
                Collections.sort(getPlayInstance().playlists, Comparator.comparing(p -> p.GetName().toLowerCase()));

                if (viewPagerAdapter.playlistAdapter != null){
                    viewPagerAdapter.playlistAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        playlistSongListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (Playlist playlist : getPlayInstance().playlists){
                    playlist.PlaylistSongs.clear();
                }

                for (DataSnapshot playlistSongSnapshot : snapshot.getChildren()) {
                    PlaylistSong playlistSong = playlistSongSnapshot.getValue(PlaylistSong.class);

                    if(getPlayInstance().playlists.stream().noneMatch(x -> x.ID.equals(playlistSong.PlaylistID))){
                        Playlist playlist = new Playlist();
                        playlist.ID = playlistSong.PlaylistID;
                        playlist.PlaylistSongs.add(playlistSong);
                        getPlayInstance().playlists.add(playlist);
                    }
                    else{
                        getPlayInstance().playlists.stream().filter(x -> x.ID.equals(playlistSong.PlaylistID)).findFirst().get().PlaylistSongs.add(playlistSong);
                    }
                }
                Collections.sort(getPlayInstance().playlists, Comparator.comparing(p -> p.GetName().toLowerCase()));

                if (viewPagerAdapter.playlistAdapter != null){
                    viewPagerAdapter.playlistAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            if(getPlayInstance().prefs.getString("email", "").equals("")){
                Intent signIn = new Intent(this, SignInActivity.class);
                signIn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(signIn);
            }
            else {
                mAuth.signInWithEmailAndPassword(getPlayInstance().prefs.getString("email", ""), getPlayInstance().prefs.getString("password", "")).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        currentUser = mAuth.getCurrentUser();
                        //"000000-TestLibrary"
                        getPlayInstance().myRef = database.getReference(currentUser.getUid());

                        GetDataFromFirebase();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String exception = e.getMessage();
                        String i = exception;
                    }
                });
            }
        }

        pager = findViewById(R.id.pager);
        tabs = findViewById(R.id.tabs);

        songLayout = findViewById(R.id.SongLayoutView);
        getPlayInstance().context = getApplicationContext();

        serviceIntent = new Intent(this, PlayMusicService.class);
        startService(serviceIntent);

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
        } else {
            // You can directly ask for the permission.
            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    69);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        interpolator = new OvershootInterpolator(0);
        songLayout.setTranslationY(height);
        pagerParams = (LinearLayout.LayoutParams)pager.getLayoutParams();

        fragment = getSongFragment();
        fragmentManager.popBackStack();
        fragmentTransaction.add(R.id.SongFragmentContainer, fragment);
        fragmentTransaction.commit();

        fragment.getRecents();

        songLayout.setOnTouchListener(this);

        viewPagerAdapter = new ViewPagerAdapter(this, new ArrayList<String>(){{
            add("Recents");
            add("Playlists");
            add("Artists");
            add("Albums");
            add("Songs");
            add("Genres");
        }}, requestManager);
        pager.setAdapter(viewPagerAdapter);
        tabs.setupWithViewPager(pager, true);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        pager.setOffscreenPageLimit(6);
        pager.setCurrentItem(getPlayInstance().prefs.getInt("currentPage", 0));
        int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        pager.setPageMargin(pageMargin);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                getPlayInstance().editor.putInt("currentPage", position);
                getPlayInstance().editor.commit();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPagerAdapter.setUpdateQueueListener(new UpdateQueueListener() {
            @Override
            public void onUpdated(boolean added) {
                //fragment.adapter.setData(getPlayInstance().musicValues.SongQueue);
                fragment.SetSelectedSong(getPlayInstance().getMusicValues().mediaPlayer.isPlaying());
            }
        });

        if(viewPagerAdapter.recentsAdapter != null) {
            viewPagerAdapter.recentsAdapter.notifyDataSetChanged();
        }

        getPlayInstance().account = GoogleSignIn.getLastSignedInAccount(this);
        if(getPlayInstance().account == null && currentUser != null){
//            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//            startActivityForResult(signInIntent, RC_SIGN_IN);
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Sign In")
                    .setMessage("Connect To Google Drive")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent settings = new Intent(getBaseContext(), SettingsActivity.class);
                            startActivity(settings);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .create();
            alertDialog.show();
        }
        else if(currentUser != null){
            setAccountInfo();
        }
    }

    public SongFragment getSongFragment(){
        if(songFragment == null){
            songFragment = new SongFragment(this, requestManager);
        }
        return songFragment;
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
    protected void onPause() {
        if(getPlayInstance().myRef != null) {
            if(getPlayInstance().myRef.child("Albums") != null) {
                getPlayInstance().myRef.child("Albums").orderByKey().removeEventListener(albumListener);
            }
            if(getPlayInstance().myRef.child("Songs") != null) {
                getPlayInstance().myRef.child("Songs").orderByKey().removeEventListener(songListener);
            }
            if(getPlayInstance().myRef.child("Playlists") != null) {
                getPlayInstance().myRef.child("Playlists").orderByKey().removeEventListener(playlistListener);
            }
            if(getPlayInstance().myRef.child("PlaylistSongs") != null) {
                getPlayInstance().myRef.child("PlaylistSongs").orderByKey().removeEventListener(playlistSongListener);
            }
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        if (getPlayInstance().getMusicValues().mediaPlayer == null) {
            getPlayInstance().getMusicValues().ResetMediaPlayer(getApplicationContext());
        }

        if(getPlayInstance().account != null){
            setAccountInfo();
        }

        if(viewPagerAdapter.recentsAdapter != null) {
            viewPagerAdapter.recentsAdapter.notifyDataSetChanged();
        }

        if(getPlayInstance().selectedSong != null){
            songLayout.setTranslationY(height - PixelFromDP(75));

            pagerParams.bottomMargin = PixelFromDP(75);

            fragment.SetSelectedSong(getPlayInstance().getMusicValues().mediaPlayer.isPlaying());
            fragment.OpenFullLayout(false);
        }

        if(currentUser != null){
            getPlayInstance().myRef = database.getReference(currentUser.getUid());
            GetDataFromFirebase();
        }

        super.onResume();
    }

    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    public boolean onSearchRequested() {
        pagerParams.bottomMargin = PixelFromDP(0);
        return super.onSearchRequested();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            getPlayInstance().account = completedTask.getResult(ApiException.class);
            setAccountInfo();
            // Signed in successfully, show authenticated UI.
            //updateUI(account);
        } catch (ApiException e) {
            String i = e.getMessage();
            i += "";
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            //Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            //updateUI(null);
        }
    }

    private void setAccountInfo(){
        //GoogleName.setText(getPlayInstance().account.getDisplayName().toString());
        //GoogleEmail.setText(getPlayInstance().account.getEmail().toString());
        Glide.with(this).load(getPlayInstance().account.getPhotoUrl()).into(GoogleImage);
        checkForGooglePermissions();
    }

    private void checkForGooglePermissions() {

        if (!GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(getApplicationContext()),
                ACCESS_DRIVE_SCOPE,
                SCOPE_EMAIL)) {
            GoogleSignIn.requestPermissions(
                    MainActivity.this,
                    RC_SIGN_IN,
                    GoogleSignIn.getLastSignedInAccount(getApplicationContext()),
                    ACCESS_DRIVE_SCOPE,
                    SCOPE_EMAIL);
        } else {
            //Toast.makeText(this, "Permission to access Drive and Email has been granted", Toast.LENGTH_SHORT).show();
            driveSetUp();
        }
    }

    private void driveSetUp() {

        GoogleSignInAccount mAccount = GoogleSignIn.getLastSignedInAccount(MainActivity.this);

        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        getApplicationContext(), Collections.singleton(DriveScopes.DRIVE));
        credential.setSelectedAccount(mAccount.getAccount());
        googleDriveService =
                new com.google.api.services.drive.Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName("GoogleDriveIntegration 3")
                        .build();
         mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
         getPlayInstance().mDriveServiceHelper = new DriveServiceHelper(googleDriveService);

//        try {
//            mDriveServiceHelper.retrieveSongFile(googleDriveService).addOnCompleteListener(new OnCompleteListener<List<File>>() {
//                @Override
//                public void onComplete(@NonNull Task<List<File>> task) {
//                    List<File> file = task.getResult();
//                    if (file != null && file.size() != 0){
//                        try {
//                            mDriveServiceHelper.getInputStream(googleDriveService, file.get(0).getId()).addOnCompleteListener(new OnCompleteListener<InputStream>() {
//                                @Override
//                                public void onComplete(@NonNull Task<InputStream> task) {
//                                    InputStream inputStream = task.getResult();
//                                    mDriveServiceHelper.getJsonString(inputStream).addOnCompleteListener(new OnCompleteListener<String>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<String> task) {
//                                            String result = task.getResult();
//                                            songs = new Gson().fromJson(result, new TypeToken<List<Song>>(){}.getType());
//                                            if (songs == null || songs.size() == 0){
//                                                RetrieveSongsFromDrive();
//                                            }
//                                            else{
//                                                viewPagerAdapter = new ViewPagerAdapter(getBaseContext(), new ArrayList<String>(){{
//                                                    add("Recents");
//                                                    add("Playlists");
//                                                    add("Artists");
//                                                    add("Albums");
//                                                    add("Songs");
//                                                    add("Genres");
//                                                }});
//                                                pager.setAdapter(viewPagerAdapter);
//                                                tabs.setupWithViewPager(pager, true);
//                                                tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
//                                                pager.setOffscreenPageLimit(6);
//                                                pager.setCurrentItem(prefs.getInt("currentPage", 0));
//                                                int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
//                                                pager.setPageMargin(pageMargin);
//                                            }
//                                        }
//                                    });
//                                }
//                            });
//
//                        } catch (IOException exception) {
//                            exception.printStackTrace();
//                        }
//                    }
//                }
//            });
//        } catch (IOException exception) {
//            exception.printStackTrace();
//        }
    }

    public void GetDataFromFirebase(){
        getPlayInstance().songs.clear();
        getPlayInstance().albums.clear();
        getPlayInstance().artists.clear();
        getPlayInstance().genres.clear();
        getPlayInstance().playlists.clear();

        getPlayInstance().myRef.child("Songs").orderByKey().addValueEventListener(songListener);
        getPlayInstance().myRef.child("Albums").orderByKey().addValueEventListener(albumListener);
        getPlayInstance().myRef.child("Playlists").orderByKey().addValueEventListener(playlistListener);
        getPlayInstance().myRef.child("PlaylistSongs").orderByKey().addValueEventListener(playlistSongListener);

    }

    public void RetrieveSongsFromDrive(){
        try {
            mDriveServiceHelper.retrieveAllFiles(googleDriveService).addOnCompleteListener(new OnCompleteListener<List<File>>() {
                @Override
                public void onComplete(@NonNull Task<List<File>> task) {
                    List<File> files = task.getResult();

                    DownloaderAsyncTask downloaderAsyncTask = new DownloaderAsyncTask(googleDriveService, mDriveServiceHelper, files);
                    try {
                        downloaderAsyncTask.execute().get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {

        }
    }

    public static void playMusic(String fileID){
        try {
            mDriveServiceHelper.getInputStream(googleDriveService, fileID).addOnCompleteListener(new OnCompleteListener<InputStream>() {
                @Override
                public void onComplete(@NonNull Task<InputStream> task) {
                    createMediaPlayer(task.getResult());
                }
            });
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

//    @Override
//    protected void onDestroy() {
//        if(getPlayInstance().musicValues.mediaPlayer != null){
//            //musicValues.mediaPlayer.stop();
//            getPlayInstance().musicValues.mediaPlayer.release();
//        }
//        stopService(serviceIntent);
//        super.onDestroy();
//    }

    public static String ConvertTime(int seconds)
    {
        String retval = "";
        seconds /= 1000;

        int h = seconds / 3600;
        int m = (seconds - (36000 * h)) / 60;
        int s = seconds - (3600 * h) - (m * 60);

        String hour = "";
        String minute = "";
        String second = "";

        if (h > 0)
        {
            hour = String.valueOf(h) + ":";
        }

        if(m > 9)
        {
            minute = String.valueOf(m) + ":";
        }
        else
        {
            minute = "0" + String.valueOf(m) + ":";
        }

        if (s > 9)
        {
            second = String.valueOf(s);
        }
        else
        {
            second = "0" + String.valueOf(s);
        }

        retval = hour + minute + second;

        return retval;
    }

    public static MediaPlayer createMediaPlayer(InputStream stream) {
        MediaPlayer mediaplayer = null;
        try {
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(new MyAsyncTask(stream).execute().get());
            mp.prepare();
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.i("Ross", "done");
                    mp.start();
                }
            });
            mediaplayer = mp;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mediaplayer;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 69:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                }  else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_search){
            //onSearchRequested();
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
//            search.setVisibility(View.VISIBLE);
//            greyBackground.setVisibility(View.VISIBLE);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

//    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
//        // Load client secrets.
//        InputStream in = MainActivity.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
//        if (in == null) {
//            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
//        }
//        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
//
//        java.io.File tokenFolder = new java.io.File(Environment.getExternalStorageDirectory() + java.io.File.separator + TOKENS_DIRECTORY_PATH);
//        if (!tokenFolder.exists()) {
//            tokenFolder.mkdirs();
//        }
//
//        // Build flow and trigger user authorization request.
//        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
//                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
//                .setDataStoreFactory(new FileDataStoreFactory(tokenFolder))
//                .setAccessType("offline")
//                .build();
//        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
//        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
//    }
//
//    private GoogleAccountCredential getCredentials2(){
//        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(DriveScopes.DRIVE));
//        credential.setSelectedAccountName("rosswarrenalexander@gmail.com");
//        return credential;
//    }

//    public void GetAllSongs(){
//        songs = new ArrayList<Song>();
//        //Nectar - Joji
//        songs.add(new Song("Nectar", "Joji", "Joji", "Ew", "", "00000", "https://firebasestorage.googleapis.com/v0/b/xx-player-2802d.appspot.com/o/b9equNf1nyYghMlbkduAtG7Xezm2%2FSongArt%2FUpgrade.png?alt=media&token=bf041958-da41-4f12-8942-22e0353b2966", 1, 0, "Alternative", "03:28"));
//        songs.add(new Song("Nectar", "Joji", "Joji", "MODUS", "", "00000", "https://firebasestorage.googleapis.com/v0/b/xx-player-2802d.appspot.com/o/b9equNf1nyYghMlbkduAtG7Xezm2%2FSongArt%2FUpgrade.png?alt=media&token=bf041958-da41-4f12-8942-22e0353b2966", 2, 0, "Alternative", "03:27"));
//        songs.add(new Song("Nectar", "Joji", "Joji", "Tick Tock", "", "00000", "https://firebasestorage.googleapis.com/v0/b/xx-player-2802d.appspot.com/o/b9equNf1nyYghMlbkduAtG7Xezm2%2FSongArt%2FUpgrade.png?alt=media&token=bf041958-da41-4f12-8942-22e0353b2966", 3, 0, "Alternative", "02:12"));
//        songs.add(new Song("Nectar", "Joji", "Joji", "Daylight", "", "00000", "https://firebasestorage.googleapis.com/v0/b/xx-player-2802d.appspot.com/o/b9equNf1nyYghMlbkduAtG7Xezm2%2FSongArt%2FUpgrade.png?alt=media&token=bf041958-da41-4f12-8942-22e0353b2966", 4, 0, "Alternative", "02:44"));
//        songs.add(new Song("Nectar", "Joji", "Joji", "Upgrade", "", "00000", "https://firebasestorage.googleapis.com/v0/b/xx-player-2802d.appspot.com/o/b9equNf1nyYghMlbkduAtG7Xezm2%2FSongArt%2FUpgrade.png?alt=media&token=bf041958-da41-4f12-8942-22e0353b2966", 5, 0, "Alternative", "01:30"));
//        songs.add(new Song("Nectar", "Joji", "Joji", "Gimme Love", "", "00000", "https://firebasestorage.googleapis.com/v0/b/xx-player-2802d.appspot.com/o/b9equNf1nyYghMlbkduAtG7Xezm2%2FSongArt%2FUpgrade.png?alt=media&token=bf041958-da41-4f12-8942-22e0353b2966", 6, 0, "Alternative", "03:35"));
//        songs.add(new Song("Nectar", "Joji", "Joji", "Run", "", "00000", "https://firebasestorage.googleapis.com/v0/b/xx-player-2802d.appspot.com/o/b9equNf1nyYghMlbkduAtG7Xezm2%2FSongArt%2FUpgrade.png?alt=media&token=bf041958-da41-4f12-8942-22e0353b2966", 7, 0, "Alternative", "03:15"));
//        songs.add(new Song("Nectar", "Joji", "Joji", "Sanctuary", "", "00000", "https://firebasestorage.googleapis.com/v0/b/xx-player-2802d.appspot.com/o/b9equNf1nyYghMlbkduAtG7Xezm2%2FSongArt%2FUpgrade.png?alt=media&token=bf041958-da41-4f12-8942-22e0353b2966", 8, 0, "Alternative", "03:00"));
//        songs.add(new Song("Nectar", "Joji", "Joji", "High Hopes (feat. Omar Apollo)", "", "00000", "https://firebasestorage.googleapis.com/v0/b/xx-player-2802d.appspot.com/o/b9equNf1nyYghMlbkduAtG7Xezm2%2FSongArt%2FUpgrade.png?alt=media&token=bf041958-da41-4f12-8942-22e0353b2966", 9, 0, "Alternative", "03:02"));
//        songs.add(new Song("Nectar", "Joji", "Joji", "NITROUS", "", "00000", "https://firebasestorage.googleapis.com/v0/b/xx-player-2802d.appspot.com/o/b9equNf1nyYghMlbkduAtG7Xezm2%2FSongArt%2FUpgrade.png?alt=media&token=bf041958-da41-4f12-8942-22e0353b2966", 10, 0, "Alternative", "02:12"));
//        songs.add(new Song("Nectar", "Joji", "Joji", "Pretty Boy (feat. Lil Yachty)", "", "00000", "https://firebasestorage.googleapis.com/v0/b/xx-player-2802d.appspot.com/o/b9equNf1nyYghMlbkduAtG7Xezm2%2FSongArt%2FUpgrade.png?alt=media&token=bf041958-da41-4f12-8942-22e0353b2966", 11, 0, "Alternative", "02:37"));
//
//        //Flavored - Pop Up!
//        songs.add(new Song("Flavored", "Pop Up!", "Pop Up!", "Vision", "", "00000", "https://firebasestorage.googleapis.com/v0/b/xx-player-2802d.appspot.com/o/000000-TestLibrary%2FSongArt%2FVision.png?alt=media&token=6335a550-27b7-4ec2-8fa1-f4fb3e1dd227", 8, 0, "Future Funk", "03:03"));
//        songs.add(new Song("Flavored", "Pop Up!", "Pop Up!", "Hot!", "", "00000", "https://firebasestorage.googleapis.com/v0/b/xx-player-2802d.appspot.com/o/000000-TestLibrary%2FSongArt%2FVision.png?alt=media&token=6335a550-27b7-4ec2-8fa1-f4fb3e1dd227", 2, 0, "Future Funk", "03:36"));
//
//        //Smooth - Pop Up!
//        songs.add(new Song("Smooth", "Pop Up!", "Pop Up!", "Carbon", "", "00000", "https://firebasestorage.googleapis.com/v0/b/xx-player-2802d.appspot.com/o/000000-TestLibrary%2FSongArt%2FCarbon.png?alt=media&token=8d747e12-437b-4c4d-a2e7-5106338c5683", 4, 0, "Future Funk", "04:10"));
//
//        //Chroma Velocity - City Girl
//        songs.add(new Song("Chroma Velocity", "City Girl", "City Girl", "Ellipsis", "", "00000", "https://firebasestorage.googleapis.com/v0/b/xx-player-2802d.appspot.com/o/000000-TestLibrary%2FSongArt%2FThe%20Limitless%20Void.png?alt=media&token=edf37ca3-9ce8-4904-a081-568c203c316b", 5, 0, "Dance", "04:00"));
//
//        //Collections.sort(songs, Comparator.comparing(p -> p.GetTitle()));
//    }

//    public void GetAlbums(){
//        albums = new ArrayList<Album>();
//        albums.add(new Album("Nectar", "Joji", "Alternative", "https://firebasestorage.googleapis.com/v0/b/xx-player-2802d.appspot.com/o/b9equNf1nyYghMlbkduAtG7Xezm2%2FSongArt%2FUpgrade.png?alt=media&token=bf041958-da41-4f12-8942-22e0353b2966", "0001"));
//        albums.add(new Album("Flavored", "Pop Up!", "Future Funk", "https://firebasestorage.googleapis.com/v0/b/xx-player-2802d.appspot.com/o/000000-TestLibrary%2FSongArt%2FFlowers.png?alt=media&token=3e8036b2-c0be-4cc6-9ce2-d7a8ee1e3fd9", "0002"));
//        albums.add(new Album("Chroma Velocity", "City Girl", "Dance", "https://firebasestorage.googleapis.com/v0/b/xx-player-2802d.appspot.com/o/000000-TestLibrary%2FSongArt%2FThe%20Limitless%20Void.png?alt=media&token=edf37ca3-9ce8-4904-a081-568c203c316b", "0003"));
//        albums.add(new Album("Smooth", "Pop Up!", "Future Funk", "https://firebasestorage.googleapis.com/v0/b/xx-player-2802d.appspot.com/o/000000-TestLibrary%2FSongArt%2FDrive.png?alt=media&token=e1be56ac-9b54-439a-b797-1d25ac76c361", "0004"));
//
//        Collections.sort(albums, Comparator.comparing(p -> p.Name));
//    }

    public void GetArtistsAndGenres(){
        for (Album album : getPlayInstance().albums){
            if(!getPlayInstance().artists.stream().anyMatch((x) -> x.Name == album.AlbumArtist)){
                Group artist = new Group();
                artist.type = GroupType.ARTIST;
                artist.Name = album.AlbumArtist;
                getPlayInstance().artists.add(artist);
            }

            if(!getPlayInstance().genres.stream().anyMatch((x) -> x.Name == album.Genre)){
                Group genre = new Group();
                genre.type = GroupType.GENRE;
                genre.Name = album.Genre;
                getPlayInstance().genres.add(genre);
            }
        }

        for (Song song : getPlayInstance().songs){
            if(!getPlayInstance().artists.stream().anyMatch((x) -> x.Name == song.Artist)){
                Group artist = new Group();
                artist.Name = song.Artist;
                artist.type = GroupType.ARTIST;
                artist.Songs.add(song);
                getPlayInstance().artists.add(artist);
            }
            else{
                Group artist = getPlayInstance().artists.stream().filter((x) -> x.Name == song.Artist).findFirst().get();
                artist.Songs.add(song);
            }

            if(!getPlayInstance().genres.stream().anyMatch((x) -> x.Name == song.Genre)){
                Group genre = new Group();
                genre.Name = song.Genre;
                genre.type = GroupType.GENRE;
                genre.Songs.add(song);
                getPlayInstance().genres.add(genre);
            }
            else{
                Group genre = getPlayInstance().genres.stream().filter((x) -> x.Name == song.Genre).findFirst().get();
                genre.Songs.add(song);
            }
        }

        for (Song song : getPlayInstance().songs){
            if(!getPlayInstance().artists.stream().anyMatch((x) -> x.Name == song.Artist)){
                Group artist = new Group();
                artist.Name = song.Artist;
                artist.type = GroupType.ARTIST;
                artist.Songs.add(song);
                getPlayInstance().artists.add(artist);
            }
            else{
                Group artist = getPlayInstance().artists.stream().filter((x) -> x.Name == song.Artist).findFirst().get();
                artist.Songs.add(song);
            }

            if(!getPlayInstance().genres.stream().anyMatch((x) -> x.Name == song.Genre)){
                Group genre = new Group();
                genre.Name = song.Genre;
                genre.type = GroupType.GENRE;
                genre.Songs.add(song);
                getPlayInstance().genres.add(genre);
            }
            else{
                Group genre = getPlayInstance().genres.stream().filter((x) -> x.Name == song.Genre).findFirst().get();
                genre.Songs.add(song);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.nav_settings){
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);
        }
        else if(id == R.id.nav_about){
            Intent about = new Intent(this, AboutActivity.class);
            startActivity(about);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_BUTTON_PRESS:
                return true;
            case MotionEvent.ACTION_DOWN:
                lastPositionY = event.getY();
                lastTransformY = v.getTranslationY();
                return true;
            case MotionEvent.ACTION_UP:
                if(lastTransformY >= v.getTranslationY() - 10 && lastTransformY <= v.getTranslationY() + 10){
                    if(lastTransformY == 0){
                        //full down
                        songLayout.animate().setInterpolator(interpolator).translationY(height - PixelFromDP(75)).setDuration(500);
                        fragment.OpenFullLayout(false);
                    }
                    else{
                        //full up
                        songLayout.animate().setInterpolator(interpolator).translationY(PixelFromDP(0)).setDuration(500);
                        fragment.OpenFullLayout(true);
                    }

                    return true;
                }
                else if (lastTransformY > v.getTranslationY()){
                    orientation = 0;
                }
                else{
                    orientation = 1;
                }

                lastTransformY = v.getTranslationY();

                if(orientation == 0){
                    //up
                    float deltaY = lastPositionY - event.getY();
                    float translationY = v.getTranslationY();
                    translationY -= deltaY;

                    if(translationY < 0){
                        translationY = 0;
                    }

                    if(translationY > height - PixelFromDP(75)){
                        translationY = height - PixelFromDP(75);
                    }

                    if(translationY < height - PixelFromDP(150)){
                        //full up
                        songLayout.animate().setInterpolator(interpolator).translationY(PixelFromDP(0)).setDuration(500);
                        translationY = 0;
                        fragment.OpenFullLayout(true);
                    }
                    else{
                        songLayout.animate().setInterpolator(interpolator).translationY(height - PixelFromDP(75)).setDuration(500);
                        fragment.OpenFullLayout(false);
                    }
                }
                else{
                    //down
                    float deltaY = lastPositionY - event.getY();
                    float translationY = v.getTranslationY();
                    translationY -= deltaY;

                    if(translationY < 0){
                        translationY = 0;
                    }

                    if(translationY > height - PixelFromDP(75)){
                        translationY = height - PixelFromDP(75);
                    }

                    if(translationY < PixelFromDP(75)){
                        //full up
                        songLayout.animate().setInterpolator(interpolator).translationY(PixelFromDP(0)).setDuration(500);
                        fragment.OpenFullLayout(true);
                    }
                    else{
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

                if (translationY1 < 0){
                    translationY1 = 0;
                }

                if(translationY1 > height - PixelFromDP(75)){
                    translationY1 = height - PixelFromDP(75);
                }

                v.setTranslationY(translationY1);
                return true;
            default:
                return super.onTouchEvent(event);
        }

    }

    int PixelFromDP(int dp) {
        int pixel = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
        return pixel;
    }

    @Override
    public void onPlayChanged(boolean playing) {
        if(fragment != null) {
            fragment.SetSelectedSong(playing);
            if(playing){
                viewPagerAdapter.recentsAdapter.notifyDataSetChanged();
            }
        }

        if(pagerParams.bottomMargin != PixelFromDP(75)) {
            //songLayout.setTranslationY(height - PixelFromDP(75));
            songLayout.animate().setInterpolator(interpolator).translationY(height - PixelFromDP(75)).setDuration(500);

            pagerParams.bottomMargin = PixelFromDP(75);
        }
    }
}