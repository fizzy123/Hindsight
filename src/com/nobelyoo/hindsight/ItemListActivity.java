package com.nobelyoo.hindsight;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a {@link ItemListFragment} and the item details (if present) is a {@link ItemDetailFragment}.
 * <p>
 * This activity also implements the required {@link ItemListFragment.Callbacks} interface to listen for item selections.
 */
public class ItemListActivity extends Activity {

    private JSONArray result = null;

	private ItemListActivity activity;
	private ListView listView;
	private JSONAdapter adapter;
	private ImageLoader imageLoader;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Instantiate List view stuff 
		setContentView(R.layout.activity_item_list);
		listView = (ListView) findViewById(R.id.item_list);
		adapter = new JSONAdapter(activity);
		listView.setAdapter(adapter);
		activity = this;
		imageLoader = new ImageLoader(this);
		new LoadMemoriesTask().execute();
		
		// Acquire a reference to the system Location Manager
		//locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		// Request location updates from GPS and NETWORK
		//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		
		// Start task that regularly checks location to see if it should update
		//locationHandler.postDelayed(locationRunnable, 0);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		// Remove location checking tasks
		locationHandler.removeCallbacks(locationRunnable);
	}
	
	private Location currentLocation = null;
	private LocationManager locationManager;
	// Define a listener that responds to location updates
	private LocationListener locationListener = new LocationListener() {
	    public void onLocationChanged(Location location) {
	      if (currentLocation == null) {
	    	  locationManager.removeUpdates(locationListener);
	    	  runOnUiThread(new Runnable() {
	    		  public void run() { 
	    			  //new LoadMemoriesTask().execute();
		          }
			  });
	      }
	      if (isBetterLocation(location, currentLocation)){
	    	  currentLocation = location;
	      }
	    }

	    public void onStatusChanged(String provider, int status, Bundle extras) {}

	    public void onProviderEnabled(String provider) {}

	    public void onProviderDisabled(String provider) {}
	};
				  
	private static final int TWO_MINUTES = 1000 * 60 * 2;

    /** Determines whether one Location reading is better than the current Location fix
      * @param location  The new Location that you want to evaluate
      * @param currentBestLocation  The current Location fix, to which you want to compare the new one
      */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
        // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
          return provider2 == null;
        }
        return provider1.equals(provider2);
    }
    
	/*
	 * Checks for updated location data at 5 minute intervals
	 * 
	 * Re-runs LoadsLocationTask whenever the locations is detected to have changed significantly.
	 */
	private Handler locationHandler = new Handler();
	
	// Stops listening for location updates, executes LoadMemoriesTask and schedules locationRunnable
	private Runnable updateViewRunnable = new Runnable() {
		@Override
		public void run() {
			locationManager.removeUpdates(locationListener);
			runOnUiThread(new Runnable() {
				public void run() { 
					//new LoadMemoriesTask().execute();
	            }
		    });
			locationHandler.postDelayed(locationRunnable, 30000);
		}
	};
	
	// Starts listening for location updates and schedules updateViewRunnable to run in 2 min
	private Runnable locationRunnable = new Runnable() {

        @Override
        public void run() {
			// Register the listener with the Location Manager to receive location updates
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            locationHandler.postDelayed(updateViewRunnable, 12000);
        }
    };
	
	/**
	 * Loads data in separate thread
	 */
	private class LoadMemoriesTask extends AsyncTask<Void, Void, Boolean> {
	    
		int test;
		protected Boolean doInBackground(Void... args) {
			try {
				// Create a new HttpClient and build GET request
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet("http://128.61.107.111:56788/memories/view_near/?latitude=33.775422&longitude=-84.3910626"); 
				//+ 
				//"?latitude=" + Double.toString(currentLocation.getLatitude()) + 
				//"&longitude=" + Double.toString(currentLocation.getLongitude()));
				// check session id for user
				// Get saved preferences for current user
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				String sessionid = prefs.getString("sessionid", null);
				httpGet.setHeader("sessionid", sessionid);
				
				HttpResponse response = httpClient.execute(httpGet);
				JSONObject json = new JSONObject(convertStreamToString(response.getEntity().getContent()));
				result = json.getJSONArray("memories");
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}

		/*
		 * Populate list view
		 */
		protected void onPostExecute(Boolean success) {
			// If successful, refresh list view
			if (success) {
				//test = adapter.getCount();
				// I think this is a bad way of doing it, but I don't know how to get notifyDataSetChanged to work
				listView.setAdapter(new JSONAdapter(activity));
			}
		}
	}
	
	// Converts Stream to String
	private static String convertStreamToString(InputStream is) {

	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();

	    String line = null;
	    try {
	        while ((line = reader.readLine()) != null) {
	            sb.append(line + "\n");
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            is.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    return sb.toString();
	}
	
	private class JSONAdapter extends BaseAdapter implements ListAdapter {

	    private final Activity activity;
	    private JSONAdapter(Activity activity) {
	        this.activity = activity;
	    }
	    
	    @Override public int getCount() {
	    	if (result == null) {
	    		return 0;
	    	} else {
	    		return result.length();
	    	}
	    }

	    @Override public JSONObject getItem(int position) {
	        return result.optJSONObject(position);
	    }

	    @Override public long getItemId(int position) {
	        JSONObject jsonObject = getItem(position);
	        return jsonObject.optLong("id");
	    }

	    @Override public View getView(int position, View convertView, ViewGroup parent) {
	    	LayoutInflater inflater = (LayoutInflater) activity
	    	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    	View rowView = inflater.inflate(R.layout.list_item, parent, false);
	    	ImageView imageView = (ImageView) rowView.findViewById(R.id.image);
    	    TextView textView = (TextView) rowView.findViewById(R.id.distance);
    	    try {
    	    	String url = "http://128.61.107.111:56788/media/" + result.getJSONObject(position).getString("image");
    	    	imageLoader.DisplayImage(url, imageView);
    	    	textView.setText(result.getJSONObject(position).getString("image_text"));
    	    	textView.setText(result.getJSONObject(position).getString("distance"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
    	    return rowView;
	    }
	}
}
