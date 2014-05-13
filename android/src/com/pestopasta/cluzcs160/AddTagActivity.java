package com.pestopasta.cluzcs160;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.google.cloud.backend.core.AsyncBlobUploader;
import com.google.cloud.backend.core.CloudBackend;
import com.google.cloud.backend.core.CloudEntity;
import com.google.cloud.backend.core.CloudQuery;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class AddTagActivity extends Activity {
	
	Button startRec, stopRec, playBack, done;
	double x, y;
	Boolean recording = false;
	File myFile;
    EditText et;

    private final static int AUDIO_TYPE = 1111;
    private final static int IMAGE_TYPE = 1112;
    private final static int VIDEO_TYPE = 1113;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        if (!haveNetworkConnection()) {
            AlertDialog.Builder builder = new AlertDialog.Builder( this );
            builder
                    .setMessage( "Error accessing the internet." )
                    .setCancelable( false )
                    .setNeutralButton( "Ok", new DialogInterface.OnClickListener()
                    {
                        public void onClick ( DialogInterface dialog, int which )
                        {
                            AddTagActivity.this.finish();
                        }
                    } );
            AlertDialog error = builder.create();
            error.show();
            return;
        }
        setContentView(R.layout.activity_addtag);
        Intent intent = getIntent();
        x = Double.parseDouble(intent.getStringExtra("lat"));
        y = Double.parseDouble(intent.getStringExtra("long"));
        //TextView tv2 = (TextView) findViewById(R.id.textView2);
        //tv2.setText("Your Location:" + intent.getStringExtra("lat") + " , " + intent.getStringExtra("long"));
        done = (Button)findViewById(R.id.doneButton);
        //done.setText("Your Location:" + intent.getStringExtra("lat") + " , " + intent.getStringExtra("long"));
        //setContentView(R.layout.activity_addtag);
        startRec = (Button)findViewById(R.id.startrec);
        //stopRec = (Button)findViewById(R.id.stoprec);
        playBack = (Button)findViewById(R.id.playback);
        playBack.setVisibility(View.GONE);
        et=(EditText)findViewById(R.id.editText1);
        et.addTextChangedListener(textWatcher);
        done.setEnabled(false);
        //String s = "Your Location:" + intent.getStringExtra("lat") + " , " + intent.getStringExtra("long");
        //System.out.println("MAYAYAMAYHFFODFF:" + intent.getStringExtra("lat"));
        //playBack.setText(s);
        new Thread(new Runnable() {
            @Override
            public void run() {
                getTagsWithinCooords(12,12,12,12);
            }
        });
        startRec.setOnClickListener(startRecOnClickListener);
        //stopRec.setOnClickListener(stopRecOnClickListener);
        playBack.setOnClickListener(playBackOnClickListener);
        done.setOnClickListener(doneOnClickListener);

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SystemBarTintManager tintManager = new SystemBarTintManager(AddTagActivity.this);
            tintManager.setStatusBarTintEnabled(true);
            int actionBarColor = Color.parseColor("#BBffffff");
            tintManager.setStatusBarTintColor(actionBarColor);
            RelativeLayout relLayout = (RelativeLayout)findViewById(R.id.addtagLayout);
            int statusBarHeight = tintManager.getConfig().getStatusBarHeight();
            relLayout.setPadding(40, 40+statusBarHeight, 40, 40);
        }
        */

    }
    
    OnClickListener startRecOnClickListener
    = new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			if (!recording) {
			startRec.setBackground(getResources().getDrawable(R.drawable.stop_red));
			//startRec.setText("Stop Recording");
			Thread recordThread = new Thread(new Runnable(){

				@Override
				public void run() {
					recording = true;
					startRecord();
				}
				
			});
			
			recordThread.start();
			} else {
				startRec.setBackground(getResources().getDrawable(R.drawable.record_red));
				//startRec.setText("Start Recording");
				recording = false;
                if (txt != null ) {
                    done.setEnabled(true);
                }
                playBack.setVisibility(View.VISIBLE);

			}

		}};

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            if (charSequence.length() > 0 && myFile != null) {
                done.setEnabled(true);
            } else {
                done.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
		
	/*OnClickListener stopRecOnClickListener
	= new OnClickListener(){
		
		@Override
		public void onClick(View arg0) {
			recording = false;
		}};*/
		
	OnClickListener playBackOnClickListener
	    = new OnClickListener(){

			@Override
			public void onClick(View v) {
				playRecord();
			}
		
	};

	OnClickListener doneOnClickListener
    = new OnClickListener(){

		@Override
		public void onClick(View v) {
            if (!haveNetworkConnection()) {
                AlertDialog.Builder builder = new AlertDialog.Builder( AddTagActivity.this );
                builder
                        .setMessage( "Error accessing the internet, please try again when connectivity is available." )
                        .setCancelable( false )
                        .setNeutralButton( "Ok", new DialogInterface.OnClickListener()
                        {
                            public void onClick ( DialogInterface dialog, int which )
                            {}
                        } );
                AlertDialog error = builder.create();
                error.show();
                return;
            } else {
                if (myFile != null) {
                    //MarkerOptions marker = new MarkerOptions().position(new LatLng(x, y))
                    //		.title("New Clue 2");
                    System.out.println("in ADDTAG");
                    txt = et.getText().toString();
                    //PostTask pt = new PostTask();
                    //pt.execute()
                    HashMap<Integer, AudioFile> temp = MainActivity.db;
                    temp.put(MainActivity.count, new AudioFile(txt, x, y, "" + MainActivity.count, myFile));
                    String[] arr = new String[2];
                    arr[0] = txt;
                    arr[1] = (x + " " + y);
                    PostTask pt = new PostTask();
                    pt.execute(arr);
                    //MainActivity.count += 1;j7t
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CloudBackend cb = new CloudBackend();
                            //File file = new File(Environment.getExternalStorageDirectory(), "test3.pcm");
                            Log.d("ADDTAG", "Test");
                            final File fileUp = new File(Environment.getExternalStorageDirectory(), txt.replace(" ", "_") + ".pcm");
                            //final File fileUp = new File(Environment.getExternalStorageDirectory(), "Geocaching_Clue.pcm");
                            new AsyncBlobUploader(AddTagActivity.this, cb, AUDIO_TYPE)
                                    .execute(fileUp);
                            Intent intent = new Intent(AddTagActivity.this, MainActivity.class);
                            //MainActivity.db = temp;
                            //MainActivity.count += 1;
                            startActivity(intent);
                        }
                    });
                    //Intent intent = new Intent(AddTagActivity.this, MainActivity.class);
                    //MainActivity.db = temp;
                    MainActivity.count += 1;
                    //startActivity(intent);
                    //MainActivity.myMap.addMarker(marker);
                }
            }
		}
	
};

    private boolean haveNetworkConnection() { boolean haveConnectedWifi = false; boolean haveConnectedMobile = false; ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); NetworkInfo[] netInfo = cm.getAllNetworkInfo(); for (NetworkInfo ni : netInfo) { if (ni.getTypeName().equalsIgnoreCase("WIFI")) if (ni.isConnected()) haveConnectedWifi = true; if (ni.getTypeName().equalsIgnoreCase("MOBILE")) if (ni.isConnected()) haveConnectedMobile = true; } return haveConnectedWifi || haveConnectedMobile; }

    private void startRecord(){
        EditText et=(EditText)findViewById(R.id.editText1);
        txt = (String) et.getText().toString();
        File file = new File(Environment.getExternalStorageDirectory(), txt.replace(" ", "_") + ".pcm");
				
		try {
			file.createNewFile();
			
			OutputStream outputStream = new FileOutputStream(file);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
			DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);
			
			int minBufferSize = AudioRecord.getMinBufferSize(11025, 
					AudioFormat.CHANNEL_CONFIGURATION_MONO, 
					AudioFormat.ENCODING_PCM_16BIT);
			
			short[] audioData = new short[minBufferSize];
			
			AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
					11025,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					minBufferSize);
			
			audioRecord.startRecording();
			
			while(recording){
				int numberOfShort = audioRecord.read(audioData, 0, minBufferSize);
				for(int i = 0; i < numberOfShort; i++){
					dataOutputStream.writeShort(audioData[i]);
				}
			}
			
			audioRecord.stop();
			dataOutputStream.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		myFile = file;
	}

	void playRecord(){
		
		//File file = new File(Environment.getExternalStorageDirectory(), "test.pcm");
		File file = myFile;
        int shortSizeInBytes = Short.SIZE/Byte.SIZE;
		
		int bufferSizeInBytes = (int)(file.length()/shortSizeInBytes);
		short[] audioData = new short[bufferSizeInBytes];
		
		try {
			InputStream inputStream = new FileInputStream(file);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
			DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);
			
			int i = 0;
			while(dataInputStream.available() > 0){
				audioData[i] = dataInputStream.readShort();
				i++;
			}
			
			dataInputStream.close();
			
			AudioTrack audioTrack = new AudioTrack(
					AudioManager.STREAM_MUSIC,
					11025,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					bufferSizeInBytes,
					AudioTrack.MODE_STREAM);
			
			audioTrack.play();
			audioTrack.write(audioData, 0, bufferSizeInBytes);

			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	String txt;


    public void getTagsWithinCooords(double minLatitude, double minLongitude, double maxLatitude, double maxLongitude) {
        System.out.println("INCOOORD");
        try {
            CloudBackend cb = new CloudBackend();
            CloudQuery cq = new CloudQuery("Tag");
            //Filter F = new Filter();
            //cq.setFilter(F.and(F.gt("latitude", minLatitude), F.lt("latitude", maxLatitude), F.gt("longitude", minLongitude), F.lt("longitude", maxLongitude)));
            List<CloudEntity> l = cb.list(cq);
            /*CloudEntity[] arr = (CloudEntity[]) l.toArray();
            for (int i =0; i < arr.length; i += 1) {
                System.out.println(arr[i].get("title"));
            }*/
            Iterator<CloudEntity> iter = l.iterator();
            while (iter.hasNext()) {
                System.out.println(iter.next().get("title"));
            }

        } catch (Exception e) {
            Log.e("getTAGERROR", "SHOOT");
        }
    }


    private class PostTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String url = "http://justmunim.pythonanywhere.com/messages";

            HttpResponse response;
            HttpClient httpclient = new DefaultHttpClient();
            try {

                HttpPost post = new HttpPost(url);
                List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("name", params[0]));
                postParameters.add(new BasicNameValuePair("comment", params[1]));

                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters);
                post.setEntity(entity);

                response = httpclient.execute(post);

            } catch (ClientProtocolException e) {
                //TODO Handle problems..
            } catch (IOException e) {
                //TODO Handle problems..
            }

            return null;
        }

        @Override
        protected void onPostExecute(String arg0) {
            //reload(null);
        }

    }

}
