package com.pestopasta.cluzcs160;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.cloud.backend.core.CloudBackend;
import com.google.cloud.backend.core.CloudEntity;
import com.google.cloud.backend.core.CloudQuery;
import com.pestopasta.slidingmenu.adapter.NavDrawerListAdapter;
import com.pestopasta.slidingmenu.model.NavDrawerItem;

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
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements LocationListener, GoogleMap.OnMapClickListener, SensorEventListener {

    Button addTag;
    //private LocationManager locationManager;
    //private LocationListener locListener;
    private String provider;
    Handler h = new Handler();

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    // nav drawer title
    private CharSequence mDrawerTitle;

    // used to store app title
    private CharSequence mTitle;

    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;

    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!haveNetworkConnection()) {
            AlertDialog.Builder builder = new AlertDialog.Builder( this );
            builder
                    .setMessage( "Error accessing the internet. Please try reopening the app when connectivity is restored." )
                    .setCancelable( false )
                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.this.finish();
                        }
                    });
            AlertDialog error = builder.create();
            error.show();
            return;
        } else {

            setContentView(R.layout.activity_main);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);

            mTitle = mDrawerTitle = getTitle();

            // load slide menu items
            navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

            // nav drawer icons from resources
            navMenuIcons = getResources()
                    .obtainTypedArray(R.array.nav_drawer_icons);

            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

            navDrawerItems = new ArrayList<NavDrawerItem>();

            // adding nav drawer items to array

            // User image
            navDrawerItems.add(new NavDrawerItem(0, null));
            // Filter header
            navDrawerItems.add(new NavDrawerItem("Filters"));
            // Public checkbox
            navDrawerItems.add(new NavDrawerItem(1, "Search"));
            // Public checkbox
            navDrawerItems.add(new NavDrawerItem(2, "Public"));
            // Private checkbox
            //navDrawerItems.add(new NavDrawerItem(2, "Private"));
            // Thick Divider
            //navDrawerItems.add(new NavDrawerItem(3, null));
            // Sign Out
            //navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
            // Settings
            //navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));

            // Recycle the typed array
            navMenuIcons.recycle();

            mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

            // setting the nav drawer list adapter
            adapter = new NavDrawerListAdapter(this, R.layout.drawer_list_item,
                    navDrawerItems);
            mDrawerList.setAdapter(adapter);

        /*
        mDrawerListener = new DrawerLayout.DrawerListener(
        ){
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerStateChanged(int newState) {
                invalidateOptionsMenu();
            }

            public void onDrawerSlide(View drawerView, float slideOffset) {
                invalidateOptionsMenu();
            }
        };
        */

            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                    R.drawable.purpletheme_ic_navigation_drawer, //nav menu toggle icon
                    R.string.app_name, // nav drawer open - description for accessibility
                    R.string.app_name // nav drawer close - description for accessibility
            ) {
                public void onDrawerClosed(View view) {
                    getActionBar().setTitle(mTitle);
                    // calling onPrepareOptionsMenu() to show action bar icons
                    invalidateOptionsMenu();
                    h.post(animateActionBarHide);
                }

                public void onDrawerOpened(View drawerView) {
                    getActionBar().setTitle(mDrawerTitle);
                    // calling onPrepareOptionsMenu() to hide action bar icons
                    invalidateOptionsMenu();
                    h.post(animateActionBarShow);
                    //Set Public Checked by Default
                    CheckBox publicCheckbox = (CheckBox) findViewById(R.id.checkbox);
                    if (publicCheckbox != null) {
                        publicCheckbox.setChecked(true);
                    }
                }
            };

            mDrawerLayout.setScrimColor(Color.parseColor("#88ffffff"));
            mDrawerLayout.setDrawerListener(mDrawerToggle);

            if (savedInstanceState == null) {
                // on first time display view for first nav item
                displayView(0);
            }

            getMyLocation();
            setUpMapIfNeeded();
            hideActionbarRunnable.run();
            //Hardcoding marker
		/*MarkerOptions marker = new MarkerOptions().position(new LatLng(37.871993, -122.257862))
				.title("New Clue");
		Marker m = myMap.addMarker(marker);*/
            count = 0;
        /*if(count == 0) {
            db.put(count, new AudioFile("Campanile Tour", 37.871993, -122.257862,""+count, new File(Environment.getExternalStorageDirectory(), "test3.pcm")));
            count += 1;
            db.put(count, new AudioFile("Lost Watch", 37.871934, -122.258120,""+count, new File(Environment.getExternalStorageDirectory(), "test2.pcm")));
            count += 1;
            db.put(count, new AudioFile("Geocaching Clue", 37.873012, -122.258785,""+count, new File(Environment.getExternalStorageDirectory(), "test1.pcm")));
            count += 1;
        }
        addMarkers();*/
            image = (ImageView) findViewById(R.id.compassIcon);
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            reload(null);


            addTag = (Button) findViewById(R.id.button1);

            addTag.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, AddTagActivity.class);
                    System.out.println("YAYAYAYAYYAYAYYAOOOOOO:" + myLat);
                    intent.putExtra("lat", String.valueOf(myLat));
                    intent.putExtra("long", String.valueOf(myLong));
                    startActivity(intent);

                }
            });

            myMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                @Override
                public void onInfoWindowClick(Marker arg0) {
                    //arg0.showInfoWindow();

                    int i = mapper.get(arg0.getSnippet());
                    AudioFile af = db.get(i);
                    currAf = af;
                    System.out.println("CLICK CLICK CLICK CLICK " + af.title);
                    Intent intent = new Intent(MainActivity.this, PlayBackActivity.class);
                    startActivity(intent);
                }
            });

            SystemBarTintManager tintManager = new SystemBarTintManager(com.pestopasta.cluzcs160.MainActivity.this);
            tintManager.setStatusBarTintEnabled(true);
            int actionBarColor = Color.parseColor("#BBffffff");
            tintManager.setStatusBarTintColor(actionBarColor);

        }
	}

    private boolean haveNetworkConnection() { boolean haveConnectedWifi = false; boolean haveConnectedMobile = false; ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); NetworkInfo[] netInfo = cm.getAllNetworkInfo(); for (NetworkInfo ni : netInfo) { if (ni.getTypeName().equalsIgnoreCase("WIFI")) if (ni.isConnected()) haveConnectedWifi = true; if (ni.getTypeName().equalsIgnoreCase("MOBILE")) if (ni.isConnected()) haveConnectedMobile = true; } return haveConnectedWifi || haveConnectedMobile; }


    /**
     * Slide menu item click listener
     * */
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // display view for selected nav drawer item
            displayView(position);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorManager != null) {
            Thread t = new Thread(animateActionBarShowTemp);
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_DELAY_GAME);
            t.start();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        View v = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (v instanceof EditText) {
            View w = getCurrentFocus();
            int scrcoords[] = new int[2];
            w.getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];

            //Log.d("Activity", "Touch event "+event.getRawX()+","+event.getRawY()+" "+x+","+y+" rect "+w.getLeft()+","+w.getTop()+","+w.getRight()+","+w.getBottom()+" coords "+scrcoords[0]+","+scrcoords[1]);
            if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom()) ) {

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        return ret;
    }
	
	/*@Override
	protected void onResume() {
		super.onResume();
		getMyLocation();
		setUpMapIfNeeded();

		//Hardcoding marker
		MarkerOptions marker = new MarkerOptions().position(new LatLng(37.871993, -122.257862))
				.title("New Clue").snippet("Take the elevator!");
		myMap.addMarker(marker);


		addTag = (Button)findViewById(R.id.button1);

		addTag.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, AddTagActivity.class);
				System.out.println("YAYAYAYAYYAYAYYAOOOOOO:" + myLat);
				intent.putExtra("lat", String.valueOf(myLat));
				intent.putExtra("long", String.valueOf(myLong));
				startActivity(intent);

			}
		});
	}*/
	/*@Override
	 protected void onResume() {
	super.onResume();
	locationManager.requestLocationUpdates(provider, 400, 1, this);

	 }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /***
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        if (menu != null && mDrawerLayout != null) {
            boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
            menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        }
        return super.onPrepareOptionsMenu(menu);

    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            // Pass any configuration change to the drawer toggles
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }
	
	public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
	    switch (item.getItemId()) {
	      default:
	            return super.onOptionsItemSelected(item);
	      
	    
	    }
	}

    /**
     * Diplaying fragment view for selected nav drawer list item
     * */
    private void displayView(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        switch (position) {
            /*
            case 0:
                fragment = new HomeFragment();
                break;
            case 1:
                fragment = new FindPeopleFragment();
                break;
            case 2:
                fragment = new PhotosFragment();
                break;
            case 3:
                fragment = new CommunityFragment();
                break;
            case 4:
                fragment = new PagesFragment();
                break;
            case 5:
                fragment = new WhatsHotFragment();
                break;
            */
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment).commit();

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(navMenuTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		//Toast.makeText(this, (myLat + "," + myLong), Toast.LENGTH_LONG);
	}

    @Override
    public void onProviderDisabled(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        // TODO Auto-generated method stub

    }

    public void getMyLocation() {
        MyLocation location = new MyLocation();
        MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {

            @Override
            public void gotLocation(Location loc) {
                currentLocation = loc;
                if (loc != null) {
                    myLat = loc.getLatitude();
                    myLong = loc.getLongitude();
                    latlonglocation = new LatLng(myLat, myLong);
                    cameraPosition = new CameraPosition(latlonglocation, 18, 0,
                            0);
                    updateMap();

                }
            };
        }; // ends LocationResult
        location.getLocation(this, locationResult);

    }// ends getMyLocation method

    private void setUpMapIfNeeded() {
        if (myMap == null) {
            myMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
            myMap.setMyLocationEnabled(true);
            int actionBarHeight = 0;
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                SystemBarTintManager tintManager = new SystemBarTintManager(com.pestopasta.cluzcs160.MainActivity.this);
                int statusBarHeight = tintManager.getConfig().getStatusBarHeight();
                myMap.setPadding(0,statusBarHeight+actionBarHeight,0,110);
            } else {
                myMap.setPadding(0, actionBarHeight, 0, 110);
            }
            myMap.setOnMapClickListener(this);
            //myMap.setOnMarkerClickListener(this);
        }
    }

    private void updateMap() {
        CameraUpdate cameraUpdate = CameraUpdateFactory
                .newCameraPosition(cameraPosition);
        myMap.moveCamera(cameraUpdate);
    }

    public void addMarkers() {
        System.out.println("HHHHHHHHEEEEERRRRRRRRE" + count);
        for(int i = 0; i < count; i += 1) {
            if(db.containsKey(i)) {
                System.out.println(i);
                AudioFile temp = db.get(i);
                MarkerOptions marker = new MarkerOptions().position(new LatLng(temp.x, temp.y))
                        .title(temp.title).snippet(temp.mySnip);
                Marker m = myMap.addMarker(marker);
                mapper.put(temp.mySnip, i);
            }
        }
    }


    public GoogleMap myMap;
    public double myLat;
    public double myLong;
    public LatLng latlonglocation;
    private CameraPosition cameraPosition;
    Location currentLocation;


    public static HashMap<Integer, AudioFile> db = new HashMap<Integer, AudioFile> ();
    public static HashMap<String, Integer> mapper = new HashMap<String, Integer> ();
    public static int count;
    public static AudioFile currAf;

	/*public boolean onMarkerClick(Marker marker) {
		// TODO Auto-generated method stub
		System.out.println("Click");
		int i = mapper.get(marker.getSnippet());
		AudioFile af = db.get(i);
		Toast.makeText(this, af.mySnip, Toast.LENGTH_LONG);
		return false;
	}*/

    public void onMapClick(LatLng point) {
        ActionBar bar = getActionBar();
        if (bar.isShowing()) {
            Thread t = new Thread(animateActionBarHide);
            t.start();
        } else {
            Thread t = new Thread(animateActionBarShowTemp);
            t.start();
        }
    }

    Runnable hideActionbarRunnable = new Runnable() {
        @Override
        public void run() {
            ActionBar bar = getActionBar();
            if (bar != null) {

                bar.hide();
                setUpMapIfNeeded();
                DrawerLayout.LayoutParams lp = (DrawerLayout.LayoutParams) mDrawerList.getLayoutParams();
                /*
                // Only set the tint if the device is running KitKat or above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    SystemBarTintManager tintManager = new SystemBarTintManager(com.pestopasta.cluzcs160.MainActivity.this);
                    tintManager.setStatusBarTintEnabled(true);
                    tintManager.setTintAlpha(0);
                    int id = getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
                    if (id == 0) {
                        // not on KitKat
                    } else {
                        boolean enabled = getResources().getBoolean(id);
                        // enabled = are translucent bars supported on this device
                        if (enabled) {
                            Window w = getWindow();
                            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                        }
                    }
                }
                */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    SystemBarTintManager tintManager = new SystemBarTintManager(com.pestopasta.cluzcs160.MainActivity.this);
                    int statusBarHeight = tintManager.getConfig().getStatusBarHeight();
                    myMap.setPadding(0, statusBarHeight, 0, 110);
                    lp.setMargins(0, statusBarHeight, 0, 0);
                    mDrawerList.setLayoutParams(lp);
                } else {
                    myMap.setPadding(0, 0, 0, 110);
                    lp.setMargins(0, 0, 0, 0);
                    mDrawerList.setLayoutParams(lp);
                }
            }
        }
    };

    Runnable showActionbarRunnable = new Runnable() {
        @Override
        public void run() {
            ActionBar bar = getActionBar();
            if (bar != null) {
                bar.show();
                setUpMapIfNeeded();
                int actionBarHeight = 0;
                TypedValue tv = new TypedValue();
                DrawerLayout.LayoutParams lp = (DrawerLayout.LayoutParams) mDrawerList.getLayoutParams();
                if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                    actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
                }
                // Only set the tint if the device is running KitKat or above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    SystemBarTintManager tintManager = new SystemBarTintManager(com.pestopasta.cluzcs160.MainActivity.this);
                    //tintManager.setStatusBarTintEnabled(true);
                    //int actionBarColor = Color.parseColor("#BBffffff");
                    //tintManager.setStatusBarTintColor(actionBarColor);
                    int statusBarHeight = tintManager.getConfig().getStatusBarHeight();
                    myMap.setPadding(0, statusBarHeight + actionBarHeight, 0, 110);
                    lp.setMargins(0, statusBarHeight + actionBarHeight, 0, 0);
                    mDrawerList.setLayoutParams(lp);
                } else {
                    myMap.setPadding(0, actionBarHeight, 0, 110);
                    lp.setMargins(0, actionBarHeight, 0, 0);
                    mDrawerList.setLayoutParams(lp);
                }
            }
        }
    };

    Runnable animateActionBarShowTemp = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(showActionbarRunnable);
            Log.d("animateActionBarShowTemp", "posted showActionbarRunnable");
            h.removeCallbacksAndMessages(null);
            h.postDelayed(hideActionbarRunnable,8000);
        }
    };

    Runnable animateActionBarShow = new Runnable() {
        @Override
        public void run() {
            h.postDelayed(hideActionbarRunnable,8000);
        }
    };

    Runnable animateActionBarHide = new Runnable() {
        @Override
        public void run() {
            h.removeCallbacksAndMessages(null);
            h.post(hideActionbarRunnable);
        }
    };
    public boolean putTag(double latitude, double longitude, String title, Bundle wrapper) {
        //if (wrapper.containsKey("Tags") && wrapper.containsKey("tagTitle") && wrapper.containsKey("tagContentType") && wrapper.containsKey("fileName")) {
        if (wrapper.containsKey("fileName")) {
            CloudEntity tag = new CloudEntity("Tag");
            String tagTitle = wrapper.getString("tagTitle");
            final int tagContentType = wrapper.getInt("tagContentType");
            String fileName = wrapper.getString("fileName");
            tag.put("title", title);
            tag.put("contentType", tagContentType);
            tag.put("latitude", latitude);
            tag.put("longitude", longitude);

            System.out.println("Shit works!");

            final CloudBackend cb;
            try {
                cb = new CloudBackend();
                cb.insert(tag);
            } catch (IOException e) {
                Log.e("Error", "Updating database failed");
                return false;
            }
            return true;
        } else {
            Log.d("Placing tag", "Missing necessary values");
            return false;
        }
    }


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
            File f = null;
            while (iter.hasNext()) {
                CloudEntity temp = iter.next();
                String tt = (String) temp.get("title");
                double lat = ((BigDecimal) temp.get("latitude")).doubleValue();
                double longt = ((BigDecimal) temp.get("longitude")).doubleValue();
                System.out.println(tt+ " " + lat + " " + longt + " " + count);
                db.put(count, new AudioFile(tt, lat, longt,""+count, f));
                ll.add(new AudioFile(tt, lat, longt,""+ count, f));
                count += 1;
            }
            //addMarkers();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("getTAGERROR", "SHOOT");
        }
    }

    public static LinkedList<AudioFile> ll = new LinkedList<AudioFile>();

    public void listAdd() {
        while (ll.size() != 0) {
            Log.d("ListAdd", ("" + ll.size()));
            AudioFile temp = ll.pollFirst();
            MarkerOptions marker = new MarkerOptions().position(new LatLng(temp.x, temp.y))
                    .title(temp.title).snippet(temp.mySnip);
            Marker m = myMap.addMarker(marker);
        }
    }



    //FLASK STUFF




    //fetch the list of messages from the server
    public void reload(View v) {
        LoadTask task = new LoadTask();
        task.execute();
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
            String url = "http://justmunim.pythonanywhere.com/messages/" + id;
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
            String url = "http://justmunim.pythonanywhere.com/messages";
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
            //data.clear();
            //String temp = MainActivity.currAf.title +"^";
            for (int i = itemsList.length(); i > 0; i--) {
                //System.out.println("BAHHAHAHAHHAHAHAHAHAHHA");
                try {
                    JSONArray current = itemsList.getJSONArray(i - 1);
                    Map<String, String> listItem = new HashMap<String, String>(2);
                    //if (current.getString(2).contains(temp)) {
                        listItem.put("ID", current.getString(0));
                        listItem.put("Name", current.getString(1));
                        listItem.put("Comment", current.getString(2));
                        String tt = current.getString(1);
                        String[] arr = current.getString(2).split(" ");
                        double d1 = Double.parseDouble(arr[0]);
                        double d2 = Double.parseDouble(arr[1]);
                        db.put(count, new AudioFile(tt, d1, d2,""+count, null));
                        count += 1;
                        //data.add(listItem);
                    //}

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            //adapter.notifyDataSetChanged();
            addMarkers();
        }

    }

    // define the display assembly compass picture
    private ImageView image;

    // record the compass picture angle turned
    private float currentDegree = 0f;

    // device sensor manager
    private SensorManager mSensorManager;

    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        image.startAnimation(ra);
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    public class Compass extends Activity implements SensorEventListener {

        // define the display assembly compass picture
        private ImageView image;

        // record the compass picture angle turned
        private float currentDegree = 0f;

        // device sensor manager
        private SensorManager mSensorManager;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //setContentView(R.layout.compass);
            image = (ImageView) findViewById(R.id.compassIcon);


            // initialize your android device sensor capabilities
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        }

		/* @Override
	    public void onCreateView(View v){

	    }*/

        @Override
        public void onResume() {
            super.onResume();

            // for the system's orientation sensor registered listeners
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_DELAY_GAME);
        }

        @Override
        public void onPause() {
            super.onPause();

            // to stop the listener and save battery
            mSensorManager.unregisterListener(this);
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            // get the angle around the z-axis rotated
            float degree = Math.round(event.values[0]);

            // create a rotation animation (reverse turn degree degrees)
            RotateAnimation ra = new RotateAnimation(
                    currentDegree,
                    -degree,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            // how long the animation will take place
            ra.setDuration(210);

            // set the animation after the end of the reservation status
            ra.setFillAfter(true);

            // Start the animation
            image.startAnimation(ra);
            currentDegree = -degree;

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}
