package com.example.music_player.DriveHelpers;

import android.media.MediaDataSource;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamMediaDataSource extends MediaDataSource {
    private Drive.Files files;
    private File file;
    private InputStream is;
    private long streamLength = -1, lastReadEndPosition;

    public InputStreamMediaDataSource(){

    }

    public InputStreamMediaDataSource(InputStream is, long streamLength, Drive.Files files, File file) {
        this.is = is;
        this.streamLength = streamLength;
        this.files = files;
        this.file = file;

        if (streamLength <= 0){
            try {
                this.streamLength = is.available(); //Correct value of InputStream#available() method not always supported by InputStream implementation!
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void close() throws IOException {
        is.close();
    }

    @Override
    public synchronized int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
        if (position >= streamLength)
            return -1;

        if (position + size > streamLength)
            size -= (position + size) - streamLength;

        if (position < lastReadEndPosition) {
            is.close();
            lastReadEndPosition = 0;
            is = files.get(file.getId()).executeMediaAsInputStream(); //new FileInputStream(mediaFile) for example.
        }

        long skipped = is.skip(position - lastReadEndPosition);
        if (skipped == position - lastReadEndPosition) {
            int bytesRead = is.read(buffer, offset, size);
            lastReadEndPosition = position + bytesRead;
            return bytesRead;
        } else {
            return -1;
        }
    }

    @Override
    public synchronized long getSize() throws IOException {
        return streamLength;
    }
}
