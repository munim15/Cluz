package com.google.cloud.backend.core;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.VideoView;

import com.pestopasta.cluzcs160.AudioFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AsyncBlobDownloader extends AsyncTask<File, Integer, File> {
    final static int AUDIO_TYPE = 1111;
    final static int IMAGE_TYPE = 1112;
    final static int VIDEO_TYPE = 1113;

    private AudioFile audioContent;
    private ImageView imageContent;
    private VideoView videoContent;
    private ProgressDialog pd;
    private CloudBackend cb;
    private String bucketName;
    private int mediaType;
    private Context context;

    public AsyncBlobDownloader(AudioFile audioContent, CloudBackend cb, Context context) {
        this.audioContent = audioContent;
        this.cb = cb;
        this.bucketName = "audio_tags";
        this.context = context;
        this.mediaType = AUDIO_TYPE;
    }

    public AsyncBlobDownloader(ImageView imageContent, CloudBackend cb, Context context) {
        this.imageContent = imageContent;
        this.cb = cb;
        this.bucketName = "image_tags";
        this.context = context;
        this.mediaType = IMAGE_TYPE;
    }

    public AsyncBlobDownloader(VideoView videoContent, CloudBackend cb, Context context) {
        this.videoContent = videoContent;
        this.cb = cb;
        this.bucketName = "video_tags";
        this.context = context;
        this.mediaType = VIDEO_TYPE;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pd = ProgressDialog.show(context, null,
                "Loading... Please wait...");
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setCancelable(true);
        pd.show();
    }

    protected File doInBackground(File... files) {
        File file = files[0];
        String downloadUrl = cb.getDownloadBlobURL(bucketName,
                file.getName());
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(downloadUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.i("Response",
                        "Server returned HTTP " + connection.getResponseCode()
                                + " " + connection.getResponseMessage()
                );
            }
            int fileLength = connection.getContentLength();

            input = connection.getInputStream();
            output = new FileOutputStream(file);

            byte data[] = new byte[4096];

            int count;
            while ((count = input.read(data)) != -1) {
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        return file;
    }

    protected void onPostExecute(File result) {
        pd.dismiss();
        if (mediaType == IMAGE_TYPE) {
            imageContent.setImageURI(Uri.fromFile(result));
        } else if (mediaType == AUDIO_TYPE) {
            // add code to set downloaded file to audio track
            Log.d("POSTEXEC", "Audio File");
            audioContent.myfile = result;
        } if (mediaType == VIDEO_TYPE) {
            // add code to set downloaded file to video track
        }
    }
}
