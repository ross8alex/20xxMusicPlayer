package com.example.music_player.DriveHelpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.music_player.Classes.Song;
import com.example.music_player.MainActivity;
import com.example.music_player.MusicService.PlayMusicService;
import com.example.music_player.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.gson.Gson;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.music_player.Classes.Singleton.getPlayInstance;

public class DriveServiceHelper {

    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;
    private final String TAG = "DRIVE_TAG";
    InputStreamMediaDataSource inputStream = new InputStreamMediaDataSource();
    boolean complete;
    Color color;


    public DriveServiceHelper(Drive driveService) {

        mDriveService = driveService;
    }

    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    public Task<GoogleDriveFileHolder> createFile(String folderId, String filename) {
        return Tasks.call(mExecutor, () -> {
            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();

            List<String> root;
            if (folderId == null) {

                root = Collections.singletonList("root");

            } else {

                root = Collections.singletonList(folderId);
            }
            File metadata = new File()
                    .setParents(root)
                    .setMimeType("text/plain")
                    .setName(filename);

            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {

                throw new IOException("Null result when requesting file creation.");
            }
            googleDriveFileHolder.setId(googleFile.getId());
            return googleDriveFileHolder;
        });
    }


// TO CREATE A FOLDER

    public Task<GoogleDriveFileHolder> createFolder(String folderName, @Nullable String folderId) {
        return Tasks.call(mExecutor, () -> {

            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();

            List<String> root;
            if (folderId == null) {

                root = Collections.singletonList("root");

            } else {

                root = Collections.singletonList(folderId);
            }
            File metadata = new File()
                    .setParents(root)
                    .setMimeType("application/vnd.google-apps.folder")
                    .setName(folderName);

            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }
            googleDriveFileHolder.setId(googleFile.getId());
            return googleDriveFileHolder;
        });
    }


    public Task<Void> downloadFile(java.io.File targetFile, String fileId) {
        return Tasks.call(mExecutor, () -> {

            // Retrieve the metadata as a File object.
            OutputStream outputStream = new FileOutputStream(targetFile);
            mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            return null;
        });
    }

    public Task<Void> deleteFolderFile(String fileId) {

        return Tasks.call(mExecutor, () -> {

            // Retrieve the metadata as a File object.
            if (fileId != null) {
                mDriveService.files().delete(fileId).execute();
            }

            return null;

        });
    }

// TO LIST FILES

    public List<File> listDriveImageFiles() throws IOException{

        FileList result;
        String pageToken = null;
        do {
            result = mDriveService.files().list()
/*.setQ("mimeType='image/png' or mimeType='text/plain'")This si to list both image and text files. Mind the type of image(png or jpeg).setQ("mimeType='image/png' or mimeType='text/plain'") */
//                    .setSpaces("drive")
//                    .setFields("nextPageToken, files(id, name)")
//                    .setPageToken(pageToken)
                    .execute();

            pageToken = result.getNextPageToken();
            Log.i("Ross", "" + result.size());
        } while (pageToken != null);

        return result.getFiles();
    }

// TO UPLOAD A FILE ONTO DRIVE

    public Task<GoogleDriveFileHolder> uploadFile(final java.io.File localFile,
                                                  final String mimeType, @Nullable final String folderId) {
        return Tasks.call(mExecutor, new Callable<GoogleDriveFileHolder>() {
            @Override
            public GoogleDriveFileHolder call() throws Exception {
                // Retrieve the metadata as a File object.

                List<String> root;
                if (folderId == null) {
                    root = Collections.singletonList("root");
                } else {

                    root = Collections.singletonList(folderId);
                }

                File metadata = new File()
                        .setParents(root)
                        .setMimeType(mimeType)
                        .setName(localFile.getName());

                FileContent fileContent = new FileContent(mimeType, localFile);

                File fileMeta = mDriveService.files().create(metadata,
                        fileContent).execute();
                GoogleDriveFileHolder googleDriveFileHolder = new
                        GoogleDriveFileHolder();
                googleDriveFileHolder.setId(fileMeta.getId());
                googleDriveFileHolder.setName(fileMeta.getName());
                return googleDriveFileHolder;
            }
        });
    }

    public Task<GoogleDriveFileHolder> searchFile(String fileName, String mimeType) {
        return Tasks.call(mExecutor, () -> {

            FileList result = mDriveService.files().list()
                    .setQ("name = '" + fileName + "' and mimeType ='" + mimeType + "'")
                    .setSpaces("drive")
                    .setFields("files(id, name,size,createdTime,modifiedTime,starred)")
                    .execute();
            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
            if (result.getFiles().size() > 0) {

                googleDriveFileHolder.setId(result.getFiles().get(0).getId());
                googleDriveFileHolder.setName(result.getFiles().get(0).getName());
                googleDriveFileHolder.setModifiedTime(result.getFiles().get(0).getModifiedTime());
                googleDriveFileHolder.setSize(result.getFiles().get(0).getSize());
            }


            return googleDriveFileHolder;
        });
    }

    public Task<List<GoogleDriveFileHolder>> searchFiles() {
        return Tasks.call(mExecutor, () -> {

            FileList result = mDriveService.files().list()
                    .setSpaces("drive")
                    .setFields("files(id, name,size,createdTime,modifiedTime,starred)")
                    .execute();
            List<GoogleDriveFileHolder> googleDriveFileHolder = new ArrayList<>();
            if (result.getFiles().size() > 0) {
                GoogleDriveFileHolder file = new GoogleDriveFileHolder();
                file.setId(result.getFiles().get(0).getId());
                file.setName(result.getFiles().get(0).getName());
                file.setModifiedTime(result.getFiles().get(0).getModifiedTime());
                if(result.getFiles().get(0).getSize() != null) {
                    file.setSize(result.getFiles().get(0).getSize());
                }
                googleDriveFileHolder.add(file);
            }


            return googleDriveFileHolder;
        });
    }

    public Task<List<File>> retrieveAllFiles(Drive service) throws IOException {
        return Tasks.call(mExecutor, () -> {
            List<File> result = new ArrayList<File>();
            Drive.Files.List request = service.files().list()
                    .setSpaces("drive")
//                    .setFields("files(id, name,size,createdTime,modifiedTime,starred, spaces)");
                    .setFields("*");

            do {
                try {
                    FileList files = request.execute();

                    result.addAll(files.getFiles());
                    request.setPageToken(files.getNextPageToken());
                } catch (IOException e) {
                    System.out.println("An error occurred: " + e);
                    request.setPageToken(null);
                }
            } while (request.getPageToken() != null &&
                    request.getPageToken().length() > 0);

            return result;
        });
    }

    public Task<List<File>> retrieveSongFile(Drive service) throws IOException {
        return Tasks.call(mExecutor, () -> {
            List<File> result = new ArrayList<File>();
            Drive.Files.List request = service.files().list()
                    .setSpaces("drive")
                    .setFields("*")
                    .setQ("name = '20XX Song List'");

            do {
                try {
                    FileList files = request.execute();

                    result.addAll(files.getFiles());
                    request.setPageToken(files.getNextPageToken());
                } catch (IOException e) {
                    System.out.println("An error occurred: " + e);
                    request.setPageToken(null);
                }
            } while (request.getPageToken() != null &&
                    request.getPageToken().length() > 0);

            return result;
        });
    }

    public Task<InputStream> getInputStream(Drive service, String fileID) throws IOException{
        return Tasks.call(mExecutor, () -> {
            return service.files().get(fileID).executeMediaAsInputStream();
        });
    }

    public Task<String> getJsonString(InputStream inputStream){
        return Tasks.call(mExecutor, () -> {
            return new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining("\n"));
        });
    }

    public Task<GoogleDriveFileHolder> createFile(final List<Song> songs, @Nullable final String folderId) {
        return Tasks.call(mExecutor, new Callable<GoogleDriveFileHolder>() {
            @Override
            public GoogleDriveFileHolder call() throws Exception {
                // Retrieve the metadata as a File object.

                List<String> root;
                if (folderId == null) {
                    root = Collections.singletonList("root");
                } else {

                    root = Collections.singletonList(folderId);
                }

                InputStream inputStream = new ByteArrayInputStream(new Gson().toJson(songs).getBytes());
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
                java.io.File localFile = new java.io.File(tempPath);

                File metadata = new File()
                        .setParents(root)
                        .setMimeType("text/plain")
                        .setName("20XX Song List");

                FileContent fileContent = new FileContent("text/plain", localFile);

                File fileMeta = mDriveService.files().create(metadata,
                        fileContent).execute();
                GoogleDriveFileHolder googleDriveFileHolder = new
                        GoogleDriveFileHolder();
                googleDriveFileHolder.setId(fileMeta.getId());
                googleDriveFileHolder.setName(fileMeta.getName());
                return googleDriveFileHolder;
            }
        });
    }

    public Task<Notification.Builder> BuildNotification(Context context, Song song, String ActionBack, String ActionPlay, String ActionPause, String ActionForward){
        return Tasks.call(mExecutor, new Callable<Notification.Builder>() {
            @Override
            public Notification.Builder call() throws Exception {
                Intent intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                Notification.Builder notificationBuilder = new Notification.Builder(context, createChannel(context))
                        .setSmallIcon(R.drawable.play_arrow_black_24dp)
                        .setLargeIcon(GetGlideImage(song))
                        .setColorized(true)
                        .setColor(color.toArgb())
                        .setSubText(song.Album)
                        .setContentTitle(song.Title)
                        .setContentText(song.Artist)
                        .setGroup("20XX")
                        .setGroupAlertBehavior(Notification.GROUP_ALERT_ALL)
                        .setStyle(new Notification.MediaStyle().setShowActionsInCompactView(0, 1, 2).setMediaSession(getPlayInstance().getMusicValues().mediaSession.getSessionToken()))
                        .setContentIntent(pendingIntent)
                        .addAction(GenerateAction(R.drawable.skip_previous_black_36dp, "Back", ActionBack, context))
                        .addAction(GenerateAction(getPlayInstance().getMusicValues().mediaPlayer.isPlaying() ? R.drawable.pause_black_48dp : R.drawable.play_arrow_black_48dp, getPlayInstance().getMusicValues().mediaPlayer.isPlaying() ? "Pause" : "Play", getPlayInstance().getMusicValues().mediaPlayer.isPlaying() ? ActionPause : ActionPlay, context))
                        .addAction(GenerateAction(R.drawable.skip_next_black_36dp, "Forward", ActionForward, context))
                        .setOngoing(false);

                return notificationBuilder;
            }
        });
    }

    public Task<Boolean> LightBackground(String art) {
        return Tasks.call(mExecutor, new Callable<Boolean>() {
            public Boolean call() throws Exception {
                URL url = null;
                try {
                    url = new URL(art);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream stream = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(stream);

                    Color color = getDominantColor(bitmap);
                    int argb = color.toArgb();
                    if (argb > -10000) {
                        return true;
                    }
                } catch (
                        Exception e) {
                    String message = e.getMessage();
                }
                return false;
            }
        });
    }

    public Task<Bitmap> GetGlideImageTask(Song song) {
        return Tasks.call(mExecutor, new Callable<Bitmap>() {
            public Bitmap call() throws Exception {
                URL url = null;
                try {
                    url = new URL(song.Art);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream stream = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(stream);

                    color = getDominantColor(bitmap);
                    return bitmap;
                } catch (
                        Exception e) {
                    String message = e.getMessage();
                }
                return null;
            }
        });
    }

    private Bitmap GetGlideImage(Song song)
    {
        URL url = null;
        try {
            url = new URL(song.Art);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream stream = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(stream);

            color = getDominantColor(bitmap);
            return bitmap;
        }
        catch(Exception e){

        }
        return null;
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

    public String createChannel(Context context){
        String output = "20XX_Player";

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel(output, "Playback", NotificationManager.IMPORTANCE_LOW);
        if(notificationManager != null){
            notificationManager.createNotificationChannel(notificationChannel);
        }

        return output;
    }

    public Notification.Action GenerateAction(int icon, String title, String intentAction, Context context){
        Intent intent = new Intent(context, PlayMusicService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(context, 1, intent, 0);

//        Drawable dr = context.getDrawable(icon);
//        Bitmap bitmap = ((BitmapDrawable)dr).getBitmap();
//        Bitmap smallerBitmap = Bitmap.createScaledBitmap(bitmap, 35, 35, true);

        //Icon newIcon = Icon.createWithBitmap(smallerBitmap);

        Icon icon1 = Icon.createWithResource(context, icon);

        return new Notification.Action.Builder(icon1, title, pendingIntent).build();
    }
}