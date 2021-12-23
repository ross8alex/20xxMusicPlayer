package com.example.music_player;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.example.music_player.Classes.Setting;
import com.example.music_player.Classes.Song;
import com.example.music_player.enums.SettingType;
import com.example.music_player.interfaces.SettingClickListener;
import com.example.music_player.ui.SettingsAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.music_player.Classes.Singleton.getPlayInstance;

public class SettingsActivity extends AppCompatActivity implements SettingClickListener {

    RecyclerView AccountRecycler;
    RecyclerView GeneralRecycler;
    RecyclerView PrivacyRecycler;
    RecyclerView PlaybackRecycler;
    RecyclerView DownloadRecycler;

    SettingsAdapter AccountAdapter;
    SettingsAdapter GeneralAdapter;
    SettingsAdapter PrivacyAdapter;
    SettingsAdapter PlaybackAdapter;
    SettingsAdapter DownloadAdapter;

    LinearLayoutManager AccountManager;
    LinearLayoutManager GeneralManager;
    LinearLayoutManager PrivacyManager;
    LinearLayoutManager PlaybackManager;
    LinearLayoutManager DownloadManager;

    List<Setting> AccountSettings = new ArrayList<Setting>();
    List<Setting> GeneralSettings = new ArrayList<Setting>();
    List<Setting> PrivacySettings = new ArrayList<Setting>();
    List<Setting> PlaybackSettings = new ArrayList<Setting>();
    List<Setting> DownloadSettings = new ArrayList<Setting>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_Music_Player_NoActionBar2);
        setTitle("Settings");
        setContentView(R.layout.settings_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark, getTheme()));

        AccountRecycler = findViewById(R.id.AccountRecyclerView);
        GeneralRecycler = findViewById(R.id.GeneralRecyclerView);
        PrivacyRecycler = findViewById(R.id.PrivacyRecyclerView);
        PlaybackRecycler = findViewById(R.id.PlaybackRecyclerView);
        DownloadRecycler = findViewById(R.id.DownloadingRecyclerView);

        if(getPlayInstance().account != null) {
            AccountSettings.add(new Setting("Connected Google Drive Account", getPlayInstance().account.getEmail(), false, false));
        }
        else{
            AccountSettings.add(new Setting("Connect to Google Drive Account", "", false, false));
        }
        AccountSettings.add(new Setting("Refresh", "", false, false));

        GeneralSettings.add(new Setting("Manage Devices", "", false, false));

        PrivacySettings.add(new Setting("Manage Play History", "", false, false));
        PrivacySettings.add(new Setting("Delete My Library", "", false, false));

        PlaybackSettings.add(new Setting("Equalizer", "", false, false));
        PlaybackSettings.add(new Setting("Stream only on Wi-Fi", "", false, true));
//        PlaybackSettings.add(new Setting("Quality on mobile network", "Normal", false, false));
//        PlaybackSettings.add(new Setting("Quality on wifi network", "Normal", false, false));
//        PlaybackSettings.add(new Setting("Cache music while streaming", "", false, true));
        PlaybackSettings.add(new Setting("Allow external devices to start playback", "For example, car Bluetooth, wired headsets", false, true));
        PlaybackSettings.add(new Setting("Show album art on lock screen", "", false, true));

        DownloadSettings.add(new Setting("Download only on Wi-Fi", "Reduces data usage", false, false));
        DownloadSettings.add(new Setting("Download quality", "Normal", false, false));
        DownloadSettings.add(new Setting("Auto-Download", "Download music for me", false, true));
        DownloadSettings.add(new Setting("Clear cache", "", false, false));
        DownloadSettings.add(new Setting("Manage Download", "", false, false));

        AccountManager = new LinearLayoutManager(this);
        GeneralManager = new LinearLayoutManager(this);
        PrivacyManager = new LinearLayoutManager(this);
        PlaybackManager = new LinearLayoutManager(this);
        DownloadManager = new LinearLayoutManager(this);

        AccountRecycler.setLayoutManager(AccountManager);
        GeneralRecycler.setLayoutManager(GeneralManager);
        PrivacyRecycler.setLayoutManager(PrivacyManager);
        PlaybackRecycler.setLayoutManager(PlaybackManager);
        DownloadRecycler.setLayoutManager(DownloadManager);

        AccountAdapter = new SettingsAdapter(AccountSettings, this, SettingType.ACCOUNT);
        GeneralAdapter = new SettingsAdapter(GeneralSettings, this, SettingType.GENERAL);
        PrivacyAdapter = new SettingsAdapter(PrivacySettings, this, SettingType.PRIVACY);
        PlaybackAdapter = new SettingsAdapter(PlaybackSettings, this, SettingType.PLAYBACK);
        DownloadAdapter = new SettingsAdapter(DownloadSettings, this, SettingType.DOWNLOAD);

        AccountRecycler.setAdapter(AccountAdapter);
        GeneralRecycler.setAdapter(GeneralAdapter);
        PrivacyRecycler.setAdapter(PrivacyAdapter);
        PlaybackRecycler.setAdapter(PlaybackAdapter);
        DownloadRecycler.setAdapter(DownloadAdapter);

        AccountRecycler.addItemDecoration(new DividerItemDecoration(this, AccountManager.getOrientation()));
        GeneralRecycler.addItemDecoration(new DividerItemDecoration(this, GeneralManager.getOrientation()));
        PrivacyRecycler.addItemDecoration(new DividerItemDecoration(this, PrivacyManager.getOrientation()));
        PlaybackRecycler.addItemDecoration(new DividerItemDecoration(this, PlaybackManager.getOrientation()));
        DownloadRecycler.addItemDecoration(new DividerItemDecoration(this, DownloadManager.getOrientation()));

        AccountRecycler.setOverScrollMode(View.OVER_SCROLL_NEVER);
        GeneralRecycler.setOverScrollMode(View.OVER_SCROLL_NEVER);
        PrivacyRecycler.setOverScrollMode(View.OVER_SCROLL_NEVER);
        PlaybackRecycler.setOverScrollMode(View.OVER_SCROLL_NEVER);
        DownloadRecycler.setOverScrollMode(View.OVER_SCROLL_NEVER);
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
    public void onSwitchClicked(int position, SettingType type) {

    }

    @Override
    public void onItemViewClicked(int position, SettingType type) {
        switch (type){
            case ACCOUNT:
                switch (position){
                    case 0:
                        Intent googleSignIn = new Intent(this, GoogleSignInActivity.class);
                        startActivity(googleSignIn);
                        break;
                }
                break;
            case PRIVACY:
                switch (position){
                    case 0:

                        break;
                    case 1:
                        AlertDialog alertDialog = new AlertDialog.Builder(this)
                                .setTitle("Delete")
                                .setMessage("Delete library from Google Drive?")
                                .setPositiveButton("Delete From Google Drive", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        for (Song song : getPlayInstance().songs){
                                            getPlayInstance().deleteSong(song);
                                        }
                                    }
                                })
                                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .setNegativeButton("Delete But Keep Google Drive Files", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        for (Song song : getPlayInstance().songs){
                                            getPlayInstance().deleteSongKeepDrive(song);
                                        }
                                    }
                                })
                                .create();
                        alertDialog.show();
                        break;
                }
                break;
        }
    }
}