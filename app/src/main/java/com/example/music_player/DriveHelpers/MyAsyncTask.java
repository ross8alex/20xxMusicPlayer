package com.example.music_player.DriveHelpers;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MyAsyncTask extends AsyncTask<Void, Void, String> {

    InputStream inputStream;

    public MyAsyncTask(InputStream inputStream){
        this.inputStream = inputStream;
    }

    @Override
    protected String doInBackground(Void... voids) {
        String tempPath = "";
        try {

            java.io.File temp = java.io.File.createTempFile("playertemp", "temp");
            temp.deleteOnExit();
            tempPath = temp.getAbsolutePath();
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
            inputStream.close();

        } catch (IOException e) {

            Log.i("Ross", "IO Exception while fetching file list");
        }


        return tempPath;

    }

//    @Override
//    protected void onPostExecute(String something) {
//        super.onPostExecute(something);
//
////        if (files.size() == 0){
////
////            Log.i("Ross", "No Files");
////        }
////        for (File file : files) {
////
////            Log.i("Ross", "\nFound file: File Name :" +
////                    file.getName() + " File Id :" + file.getId());
////        }
//    }
}