package com.pestopasta.cluzcs160;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.cloud.backend.core.AsyncBlobDownloader;
import com.google.cloud.backend.core.CloudBackend;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class PlayBackActivity extends Activity{
	private Button playButton;
	private ImageButton pauseButton;
	private AudioTrack audioTrack;
	private RatingBar ratingBar;
	private Button btnSubmit;
    private Button btnComment;
	private boolean paused = false;

    private final static int AUDIO_TYPE = 1111;
    private final static int IMAGE_TYPE = 1112;
    private final static int VIDEO_TYPE = 1113;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          getActionBar().setDisplayHomeAsUpEnabled(true);
          getActionBar().setHomeButtonEnabled(true);
        if (!haveNetworkConnection()) {
            AlertDialog.Builder builder = new AlertDialog.Builder( this );
            builder
                    .setMessage( "Error accessing the internet" )
                    .setCancelable( false )
                    .setNeutralButton( "Ok", new DialogInterface.OnClickListener()
                    {
                        public void onClick ( DialogInterface dialog, int which )
                        {
                            PlayBackActivity.this.finish();
                        }
                    } );
            AlertDialog error = builder.create();
            error.show();
            return;
        }
          setContentView(R.layout.activity_playback);
          ((TextView) findViewById(R.id.titl)).setText(MainActivity.currAf.title);
          playButton = (Button)findViewById(R.id.play);
          //pauseButton = (ImageButton)findViewById(R.id.imageButton2);
          addListenerOnRatingBar();
      	  addListenerOnButton();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CloudBackend cb = new CloudBackend();
                File file = new File(Environment.getExternalStorageDirectory(), MainActivity.currAf.title.replace(" ", "_") + ".pcm");
                new AsyncBlobDownloader(MainActivity.currAf, cb, PlayBackActivity.this)
                        .execute(file);
            }
        });
          playButton.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                //System.out.println("GSFSDFSADFASFASDFSDFADFSADFSADFADFASFSDFSDF");
                //audioTrack.play();
                Thread loopbackThread;
                if(!paused) {
                    paused = true;
                    playButton.setBackground(getResources().getDrawable(R.drawable.stop_red));
                    loopbackThread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            playRecord();
                        }

                    });

                    loopbackThread.start();
                    //playRecord();
                } else {
                    playButton.setBackground(getResources().getDrawable(R.drawable.play_red));
                    //System.out.println("TTTTTTTTTTTTTJJJJJJJJJJJJJJJJJKKKKKKKK");
                    audioTrack.pause();
                    //loopbackThread.interrupt();
                    //audioTrack.stop();
                    paused = false;
                }

            }

        	  
          });

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SystemBarTintManager tintManager = new SystemBarTintManager(PlayBackActivity.this);
            tintManager.setStatusBarTintEnabled(true);
            int actionBarColor = Color.parseColor("#BBffffff");
            tintManager.setStatusBarTintColor(actionBarColor);
            RelativeLayout relLayout = (RelativeLayout)findViewById(R.id.playbackLayout);
            int statusBarHeight = tintManager.getConfig().getStatusBarHeight();
            relLayout.setPadding(40, 40+statusBarHeight, 40, 40);
        }
        */

          /*pauseButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				System.out.println("GSFSDFSADFASFASDFSDFADFSADFSADFADFASFSDFSDF");
				audioTrack.pause();
				paused = true;
			}
        	  
          });*/
          //playRecord();
    }

    private boolean haveNetworkConnection() { boolean haveConnectedWifi = false; boolean haveConnectedMobile = false; ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); NetworkInfo[] netInfo = cm.getAllNetworkInfo(); for (NetworkInfo ni : netInfo) { if (ni.getTypeName().equalsIgnoreCase("WIFI")) if (ni.isConnected()) haveConnectedWifi = true; if (ni.getTypeName().equalsIgnoreCase("MOBILE")) if (ni.isConnected()) haveConnectedMobile = true; } return haveConnectedWifi || haveConnectedMobile; }


    public void addListenerOnRatingBar() {
		 
		ratingBar = (RatingBar) findViewById(R.id.ratingBar);
	 
		ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
			public void onRatingChanged(RatingBar ratingBar, float rating,
				boolean fromUser) {
	 
	 
			}
		});
	  }
	 
	  public void addListenerOnButton() {
	 
		ratingBar = (RatingBar) findViewById(R.id.ratingBar);
		btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnComment = (Button) findViewById(R.id.btnComment);
	 
		//if click on me, then display the current rating value.
		btnSubmit.setOnClickListener(new OnClickListener() {
	 
			@Override
			public void onClick(View v) {
	 
				Toast.makeText(PlayBackActivity.this,
					String.valueOf("You Rated " + MainActivity.currAf.title + ": "
				+ ratingBar.getRating() + "/4.0 \n Thanks for the feedback!"),
						Toast.LENGTH_LONG).show();
	 
			}
	 
		});

          btnComment.setOnClickListener(new OnClickListener() {

              @Override
              public void onClick(View v) {

                  Intent intent = new Intent(PlayBackActivity.this, CommentActivity.class);
                  startActivity(intent);


              }

          });


	  }
	  
	void playRecord(){
        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CloudBackend cb = new CloudBackend();
                file = new File(Environment.getExternalStorageDirectory(), "test3.pcm");
                new AsyncBlobDownloader(MainActivity.currAf, cb, PlayBackActivity.this)
                        .execute(file);
            }
        });*/



          File file = MainActivity.currAf.myfile;
		  if (file == null) {
		  System.out.print("NULL NULL NULL");
		  }
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
		   
		   audioTrack = new AudioTrack(
		     AudioManager.STREAM_MUSIC,
		     11025,
		     AudioFormat.CHANNEL_CONFIGURATION_MONO,
		     AudioFormat.ENCODING_PCM_16BIT,
		     bufferSizeInBytes,
		     AudioTrack.MODE_STREAM);
		// set setNotificationMarkerPosition accouding audio length
		   audioTrack.setNotificationMarkerPosition(bufferSizeInBytes);

		  // now add OnPlaybackPositionUpdateListener to audioTrack 
		      audioTrack.setPlaybackPositionUpdateListener(
		                               new AudioTrack.OnPlaybackPositionUpdateListener() {
		          @Override
		          public void onMarkerReached(AudioTrack track) {
		              // do your work here....
		        	  playButton.setBackground(getResources().getDrawable(R.drawable.play_red));
		        	  paused = false;
		          }

				@Override
				public void onPeriodicNotification(AudioTrack arg0) {
					// TODO Auto-generated method stub
					
				}
		      });
		   
		   audioTrack.play();
		   audioTrack.write(audioData, 0, bufferSizeInBytes);
		   
		  } catch (FileNotFoundException e) {
		   e.printStackTrace();
		  } catch (IOException e) {
		   e.printStackTrace();
		  }
		 }
}
