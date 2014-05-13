package com.pestopasta.cluzcs160;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.cloud.backend.core.AsyncBlobUploader;
import com.google.cloud.backend.core.CloudBackend;
import com.google.cloud.backend.core.CloudBackendFragment;
import com.google.cloud.backend.core.CloudEntity;
import com.google.cloud.backend.core.CloudQuery;
import com.google.cloud.backend.core.Filter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseInterface extends Activity {

    private final static int AUDIO_TYPE = 1111;
    private final static int IMAGE_TYPE = 1112;
    private final static int VIDEO_TYPE = 1113;

    private static final String PROCESSING_FRAGMENT_TAG = "BACKEND_FRAGMENT";

    private FragmentManager mFragmentManager;
    private CloudBackendFragment mProcessingFragment;




    public Bundle getTag(String id) {
        Bundle wrapper = new Bundle();
        try {
            CloudBackend cb = new CloudBackend();
            CloudEntity tag = cb.get("Tag", id);
            for (String key: tag.getProperties().keySet()) {
                if ((tag.get(key)) instanceof String) {
                    wrapper.putString(key, (String) tag.get(key));
                } else if ((tag.get(key)) instanceof Integer) {
                    wrapper.putInt(key, (Integer) tag.get(key));
                }
            }
        } catch (IOException e) {
            Log.e("Error", "Retrieving tag from database failed");
        }
        return wrapper;
    }

   /* public boolean putTag(double latitude, double longitude, Bundle wrapper) {
        //if (wrapper.containsKey("Tags") && wrapper.containsKey("tagTitle") && wrapper.containsKey("tagContentType") && wrapper.containsKey("fileName")) {
        if (wrapper.containsKey("fileName")) {
            CloudEntity tag = new CloudEntity("Tag");
            String tagTitle = wrapper.getString("tagTitle");
            int tagContentType = wrapper.getInt("tagContentType");
            String fileName = wrapper.getString("fileName");
            tag.put("title", tagTitle);
            tag.put("contentType", tagContentType);
            tag.put("latitude", latitude);
            tag.put("longitude", longitude);

            try {
                CloudBackend cb = new CloudBackend();
                cb.insert(tag);
            } catch (IOException e) {
                Log.e("Error", "Updating database failed");
                return false;
            }

            mProcessingFragment = (CloudBackendFragment) mFragmentManager.
                    findFragmentByTag(PROCESSING_FRAGMENT_TAG);

            File fileUp = new File(Environment.getExternalStorageDirectory(), fileName);
            new AsyncBlobUploader(this, mProcessingFragment.getCloudBackend(), tagContentType).execute(fileUp);
            return true;
        } else {
            Log.d("Placing tag", "Missing necessary values");
            return false;
        }
    }
*/
    public void getTagsWithinCooords(double minLatitude, double minLongitude, double maxLatitude, double maxLongitude) {
        try {
            CloudBackend cb = new CloudBackend();
            CloudQuery cq = new CloudQuery("Tag");
            Filter F = new Filter();
            cq.setFilter(F.and(F.gt("latitude", minLatitude), F.lt("latitude", maxLatitude), F.gt("longitude", minLongitude), F.lt("longitude", maxLongitude)));
            List<CloudEntity> l = cb.list(cq);
            CloudEntity[] arr = (CloudEntity[]) l.toArray();
            for (int i =0; i < arr.length; i += 1) {
                System.out.println(arr[i].get("title"));
            }
        } catch (Exception e) {
            Log.e("getTAGERROR", "SHOOT");
        }
    }

}
