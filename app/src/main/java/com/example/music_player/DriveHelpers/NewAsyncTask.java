package com.example.music_player.DriveHelpers;

import android.os.AsyncTask;

import com.example.music_player.Classes.Song;
import com.google.api.services.drive.model.File;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.LyricsHandler;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.example.music_player.MainActivity.ConvertTime;

public class NewAsyncTask extends AsyncTask<Void, Void, Song> {

    InputStream inputStream;
    File file;

    public NewAsyncTask(InputStream inputStream, File file){
        this.inputStream = inputStream;
        this.file = file;
    }

    @Override
    protected Song doInBackground(Void... voids) {
        Song song = new Song();

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

            if (metadata.get(xmp + "duration") != null) {
                song.SongID = file.getId();
                song.Genre = metadata.get(xmp + "genre");
                song.Artist = metadata.get(xmp + "artist");
                song.AlbumArtist = metadata.get(xmp + "albumArtist");
                song.Album = metadata.get(xmp + "album");
                song.SongLength = ConvertTime((int) Float.parseFloat(metadata.get(xmp + "duration")));
                song.Art = "";
                //song.Art = file.getThumbnailLink();
                //song.SongNumber = Integer.parseInt(metadata.get(xmp+"trackNumber"));
                song.Title = metadata.get("title");
            }
        }catch (Exception e){

        }
        return song;
    }
}
