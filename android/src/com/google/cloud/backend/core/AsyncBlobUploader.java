package com.google.cloud.backend.core;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.IOException;

public class AsyncBlobUploader extends AsyncTask<File, Void, String> {
    final static int AUDIO_TYPE = 1111;
    final static int IMAGE_TYPE = 1112;
    final static int VIDEO_TYPE = 1113;
    private Context context;
    private ProgressDialog pd;
    private CloudBackend cb;
    private String bucketName;

    public AsyncBlobUploader(Context context, CloudBackend cb, int mediaType) {
        this.context = context;
        this.cb = cb;
        if (mediaType == AUDIO_TYPE) {
            this.bucketName = "audio_tags";
        } else if (mediaType == IMAGE_TYPE) {
            this.bucketName = "image_tags";
        } else if (mediaType == VIDEO_TYPE) {
            this.bucketName = "video_tags";
        } else {
            Log.e("File Upload", "Invalid File Type");
            cancel(true);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pd = ProgressDialog.show(context, null,
                "Loading... Please wait...");
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setIndeterminate(true);
        pd.setCancelable(true);
        pd.show();
    }

    protected String doInBackground(File... files) {
        File file = files[0];
        String uploadUrl = cb.getUploadBlobURL(bucketName, file.getName(),"PUBLIC_READ_FOR_APP_USERS");
        String url = uploadUrl.split("&Signature")[0]; // url without Signature

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        FileBody filebody = new FileBody(file, ContentType.create(getMimeType(file
                .toString())), file.getName());

        MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
        multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        multipartEntity.addPart("file", filebody);
        httppost.setEntity(multipartEntity.build());
        System.out.println( "executing request " + httppost.getRequestLine( ) );
        try {
            HttpResponse response = httpclient.execute( httppost );
            Log.i("response", response.getStatusLine().toString());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        httpclient.getConnectionManager( ).shutdown( );

        return (String) uploadUrl;
    }

    protected void onPostExecute(String result) {

        pd.dismiss();
        Log.d("BlobUrl", result);

    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }
}
