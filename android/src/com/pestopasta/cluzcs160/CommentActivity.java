package com.pestopasta.cluzcs160;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentActivity extends Activity {

    List<Map<String, String>> data = new ArrayList<Map<String, String>>();
    SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
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
                            CommentActivity.this.finish();
                        }
                    } );
            AlertDialog error = builder.create();
            error.show();
            return;
        }
        //create a simple adapter
        adapter = new SimpleAdapter(this,
                data,							 //a list of hashmaps
                R.layout.message_item, 			 //the layout to use for each item
                new String[] {"Name", "Comment", "ID" }, 	 //the array of keys
                new int[] {R.id.nameText, R.id.commentText, R.id.hiddenId });	//array of view ids that should display the values (same order)
        ListView messageList = (ListView)findViewById(R.id.currentmessages);
        messageList.setAdapter(adapter);
        messageList.setLongClickable(true);
        //messageList.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        messageList.setStackFromBottom(false);

        //on long click, offer to delete item in a popup.
        messageList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> listView, View v, int position,
                                           long id) {


                final HashMap<String,String> item = (HashMap)listView.getItemAtPosition(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(CommentActivity.this);

                // Add the Delete button
                builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        DeleteTask task = new DeleteTask();
                        task.execute(item.get("ID"));
                    }
                });

                //Add the Cancel button
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                // Set other dialog properties
                builder.setTitle("Delete Item");

                // Create the AlertDialog and show it
                AlertDialog dialog = builder.create();
                dialog.show();
                return false;
            }
        });

        reload(null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private boolean haveNetworkConnection() { boolean haveConnectedWifi = false; boolean haveConnectedMobile = false; ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); NetworkInfo[] netInfo = cm.getAllNetworkInfo(); for (NetworkInfo ni : netInfo) { if (ni.getTypeName().equalsIgnoreCase("WIFI")) if (ni.isConnected()) haveConnectedWifi = true; if (ni.getTypeName().equalsIgnoreCase("MOBILE")) if (ni.isConnected()) haveConnectedMobile = true; } return haveConnectedWifi || haveConnectedMobile; }


    //post values from the entry form to the server
    public void post(View v) {
        EditText nameBox = (EditText)findViewById(R.id.namebox);
        String name = nameBox.getText().toString();

        EditText messageBox = (EditText)findViewById(R.id.messagebox);
        String message = messageBox.getText().toString();

        if (!haveNetworkConnection()) {
            AlertDialog.Builder builder = new AlertDialog.Builder( CommentActivity.this );
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
            PostTask task = new PostTask();
            task.execute(name, message);
        }
    }

    //fetch the list of messages from the server
    public void reload(View v) {
        LoadTask task = new LoadTask();
        task.execute();
    }

    private class PostTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String url = "http://munim15.pythonanywhere.com/messages";

            HttpResponse response;
            HttpClient httpclient = new DefaultHttpClient();
            try {

                HttpPost post = new HttpPost(url);
                List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("name", params[0]));
                postParameters.add(new BasicNameValuePair("comment", MainActivity.currAf.title +"^"+ params[1]));

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
            reload(null);
        }

    }

    private class DeleteTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String id = params[0];
            String url = "http://munim15.pythonanywhere.com/messages/" + id;
            HttpResponse response;
            HttpClient httpclient = new DefaultHttpClient();
            try {
                HttpDelete delete = new HttpDelete(url);
                response = httpclient.execute(delete);

            } catch (ClientProtocolException e) {
                //TODO Handle problems..
            } catch (IOException e) {
                //TODO Handle problems..
            }
            return null;
        }

        @Override
        protected void onPostExecute(String arg0) {
            reload(null);
        }

    }

    private class LoadTask extends AsyncTask<Void, Void, JSONArray> {

        protected JSONArray doInBackground(Void...arg0) {
            String url = "http://munim15.pythonanywhere.com/messages";
            HttpResponse response;
            HttpClient httpclient = new DefaultHttpClient();
            String responseString = "";

            try {
                response = httpclient.execute(new HttpGet(url));
                if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();

                } else{
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(response.getStatusLine().getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                //TODO Handle problems..
            } catch (IOException e) {
                //TODO Handle problems..
            }
            try {
                JSONArray messages = new JSONArray(responseString);
                return messages;
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }


        protected void onPostExecute(JSONArray itemsList) {
            data.clear();
            String temp = MainActivity.currAf.title +"^";
            for (int i = itemsList.length(); i > 0; i--) {
                //System.out.println("BAHHAHAHAHHAHAHAHAHAHHA");
                try {
                    JSONArray current = itemsList.getJSONArray(i - 1);
                    Map<String, String> listItem = new HashMap<String, String>(2);
                    if (current.getString(2).contains(temp)) {
                        listItem.put("ID", current.getString(0));
                        listItem.put("Name", current.getString(1));
                        listItem.put("Comment", current.getString(2).substring(temp.length()));
                        data.add(listItem);
                   }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            adapter.notifyDataSetChanged();
        }

    }

}
