package com.example.music_player.DriveHelpers;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.music_player.Classes.Album;
import com.example.music_player.Classes.Group;
import com.example.music_player.Classes.Song;
import com.example.music_player.enums.GroupType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.LyricsHandler;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.example.music_player.Classes.Singleton.getPlayInstance;
import static com.example.music_player.MainActivity.ConvertTime;

public class DownloaderAsyncTask extends AsyncTask<Void, Void, Boolean> {

    Drive drive;
    DriveServiceHelper mDriveServiceHelper;
    List<File> files;
    boolean retVal = false;

    public DownloaderAsyncTask(Drive drive, DriveServiceHelper driveServiceHelper, List<File> files){
        this.drive = drive;
        this.mDriveServiceHelper = driveServiceHelper;
        this.files = files;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        retVal = false;

        for (File file : files) {
            if (file.getFileExtension() != null && file.getFileExtension().equals("mp3")) {
                try {
                    if (file.getFileExtension() != null) {
                        //detecting the file type

                        InputStream inputStream = drive.files().get(file.getId()).executeMediaAsInputStream();

                        try {
                            BodyContentHandler handler = new BodyContentHandler();
                            Metadata metadata = new Metadata();

                            java.io.File temp = java.io.File.createTempFile("playertemp", "temp");
                            String tempPath = temp.getAbsolutePath();
                            FileOutputStream out = new FileOutputStream(temp);
                            BufferedOutputStream bis = null;
                            try {
                                bis = new BufferedOutputStream(out);
                                byte buf[] = new byte[128];
                                do {
                                    int numread = inputStream.read(buf);
                                    if (numread <= 0)
                                        break;
                                    bis.write(buf, 0, numread);
                                } while (true);
                            } finally {
                                if (bis != null) {
                                    bis.close();
                                }
                            }
                            out.close();

                            FileInputStream fileInputStream = new FileInputStream(new java.io.File(tempPath));
                            ParseContext pcontext = new ParseContext();

                            //Mp3 parser
                            Mp3Parser Mp3Parser = new Mp3Parser();
                            Mp3Parser.parse(fileInputStream, handler, metadata, pcontext);
                            LyricsHandler lyrics = null;
                            lyrics = new LyricsHandler(fileInputStream, handler);

                            while (lyrics.hasLyrics()) {
                                System.out.println(lyrics.toString());
                            }

                            String[] metadataNames = metadata.names();

                            for (String name : metadataNames) {
                                String i = metadata.get(name);
                            }

                            //System.gc();
                            inputStream.close();
                            fileInputStream.close();

                            String xmp = "xmpDM:";
                            Song song = new Song();

                            if (metadata.get(xmp + "duration") != null) {
                                song.SongID = file.getId();
                                song.Genre = metadata.get(xmp + "genre");
                                song.Artist = metadata.get(xmp + "artist");
                                song.AlbumArtist = metadata.get(xmp + "albumArtist");
                                song.Album = metadata.get(xmp + "album");
                                song.SongLength = ConvertTime((int) Float.parseFloat(metadata.get(xmp + "duration")));
                                if(metadata.get(xmp+"trackNumber") != null) {
                                    String trackNum = metadata.get(xmp + "trackNumber");
                                    //song.SongNumber = Integer.parseInt(trackNum.contains("/") ? trackNum.substring(0, trackNum.indexOf("/")) : trackNum);
                                }
                                if(metadata.get(xmp+"discNumber") != null) {
                                    String discNum = metadata.get(xmp + "discNumber");
                                    //song.DiscNumber = Integer.parseInt(discNum.contains("/") ? discNum.substring(0, discNum.indexOf("/")) : discNum);
                                }
                                song.Art = "";
                                //song.Art = file.getThumbnailLink();
                                song.Title = metadata.get("title");
                                getPlayInstance().songs.add(song);

                                if (getPlayInstance().albums.stream().noneMatch((x) -> x.Name.equals(song.Album))) {
                                    Album album = new Album();
                                    album.Name = song.Album;
                                    album.Genre = song.Genre;
                                    album.Art = "";
                                    album.AlbumArtist = song.AlbumArtist;
                                    getPlayInstance().albums.add(album);
                                }

                                if (getPlayInstance().artists.stream().noneMatch((x) -> x.Name.equals(song.AlbumArtist))) {
                                    Group artist = new Group();
                                    artist.type = GroupType.ARTIST;
                                    artist.Name = song.AlbumArtist;
                                    getPlayInstance().artists.add(artist);
                                }

                                if (!getPlayInstance().genres.stream().anyMatch((x) -> x.Name.equals(song.Genre))) {
                                    Group genre = new Group();
                                    genre.type = GroupType.GENRE;
                                    genre.Name = song.Genre;
                                    getPlayInstance().genres.add(genre);
                                }
                            }

                        } catch (Exception e) {
                            Log.i("Ross", e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    Log.i("Ross", e.getMessage());
                }
            }
            retVal = true;
        }
        return retVal;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        mDriveServiceHelper.createFile(getPlayInstance().songs, null).addOnCompleteListener(new OnCompleteListener<GoogleDriveFileHolder>() {
            @Override
            public void onComplete(@NonNull Task<GoogleDriveFileHolder> task) {
                GoogleDriveFileHolder driveFileHolder = task.getResult();
            }
        });

//        getPlayInstance().viewPagerAdapter.songAdapter.notifyDataSetChanged();
//        getPlayInstance().viewPagerAdapter.albumAdapter.notifyDataSetChanged();
//        getPlayInstance().viewPagerAdapter.artistAdapter.notifyDataSetChanged();
//        getPlayInstance().viewPagerAdapter.genreAdapter.notifyDataSetChanged();
    }
}
