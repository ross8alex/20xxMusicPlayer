package com.example.music_player;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.music_player.Classes.Album;
import com.example.music_player.Classes.Song;

import static com.example.music_player.Classes.Singleton.getPlayInstance;

public class EditActivity extends Activity {
    RelativeLayout EditLayout;

    ImageView ItemImage;
    EditText SongEdit;
    EditText AlbumEdit;
    EditText AlbumArtist;
    EditText Artist;
    EditText SongNumber;
    EditText DiscNumber;
    EditText Genre;
    Button Cancel;
    Button Save;
    ImageView editImage;

    Album selectedAlbum;
    Song selectedSong;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_layout);

        EditLayout = findViewById(R.id.EditLayout);
        ItemImage = findViewById(R.id.ItemImage);
        SongEdit = findViewById(R.id.Song);
        AlbumEdit = findViewById(R.id.Album);
        AlbumArtist = findViewById(R.id.AlbumArtist);
        Artist = findViewById(R.id.Artist);
        SongNumber = findViewById(R.id.SongNumber);
        DiscNumber = findViewById(R.id.DiscNumber);
        Genre = findViewById(R.id.Genre);
        Cancel = findViewById(R.id.Cancel);
        Save = findViewById(R.id.Save);
        editImage = findViewById(R.id.editImage);

        editImage.setColorFilter(Color.parseColor("#FFFFFF"));

        if(getPlayInstance().itemToEdit.getClass().equals(Album.class)){
            selectedAlbum = (Album)getPlayInstance().itemToEdit;

            Glide.with(this).load(selectedAlbum.Art).into(ItemImage);
            SongEdit.setVisibility(View.GONE);
            Artist.setVisibility(View.GONE);
            SongNumber.setVisibility(View.GONE);
            DiscNumber.setVisibility(View.GONE);

            AlbumEdit.setText(selectedAlbum.Name);
            AlbumArtist.setText(selectedAlbum.AlbumArtist);
            Genre.setText(selectedAlbum.Genre);
        }
        else if(getPlayInstance().itemToEdit.getClass().equals(Song.class)){
            selectedSong = (Song)getPlayInstance().itemToEdit;

            Glide.with(this).load(selectedSong.Art).into(ItemImage);
            SongEdit.setText(selectedSong.Title);
            AlbumEdit.setText(selectedSong.Album);
            AlbumArtist.setText(selectedSong.AlbumArtist);
            Artist.setText(selectedSong.Artist);
            SongNumber.setText(selectedSong.SongNumber);
            DiscNumber.setText(selectedSong.DiscNumber);
            Genre.setText(selectedSong.Genre);
        }

        EditLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getPlayInstance().itemToEdit.getClass().equals(Album.class)){
                    selectedAlbum.SetName(AlbumEdit.getText().toString());
                    selectedAlbum.SetAlbumArtist(AlbumArtist.getText().toString());
                    selectedAlbum.SetGenre(Genre.getText().toString());

                    getPlayInstance().myRef.child("Albums").child(selectedAlbum.AlbumID).setValue(selectedAlbum);
                }
                else if(getPlayInstance().itemToEdit.getClass().equals(Song.class)){
                    selectedSong.SetTitle(SongEdit.getText().toString());
                    selectedSong.SetAlbum(AlbumEdit.getText().toString());
                    selectedSong.SetAlbumArtist(AlbumArtist.getText().toString());
                    selectedSong.SetArtist(Artist.getText().toString());
                    selectedSong.SetSongNumber(SongNumber.getText().toString());
                    selectedSong.SetDiscNumber(DiscNumber.getText().toString());

                    getPlayInstance().myRef.child("Songs").child(selectedSong.SongID).setValue(selectedSong);
                }
            }
        });

        ItemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
