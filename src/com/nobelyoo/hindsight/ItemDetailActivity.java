package com.nobelyoo.hindsight;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An activity representing a single Item detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ItemListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than a {@link ItemDetailFragment}.
 */
public class ItemDetailActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_item_detail);
		// Start new thread
		new LoadMemoryTask().execute();
		// I don't know what this does. It just is in here by default.
		// Show the Up button in the action bar.
		//getActionBar().setDisplayHomeAsUpEnabled(true);		
	}
	
	/**
	 * Loads data in separate thread
	 */
	private class LoadMemoryTask extends AsyncTask<Void, Void, HttpResponse> {
	    
		private JSONObject result;
		protected HttpResponse doInBackground(Void... args) {
			Intent intent = getIntent();
			try {
				// Create a new HttpClient and build GET request
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet("http://128.61.107.111:56788/memories/view_specific/" +
						intent.getStringExtra(ItemListActivity.MEMORY_ID) +
				"?latitude=" + intent.getStringExtra(ItemListActivity.LATITUDE) + 
				"&longitude=" + intent.getStringExtra(ItemListActivity.LONGITUDE));
				// check session id for user
				// Get saved preferences for current user
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				String sessionid = prefs.getString("sessionid", null);
				httpGet.setHeader("sessionid", sessionid);
				
				// Execute GET request
				return httpClient.execute(httpGet);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		/*
		 * Populate view
		 */
		protected void onPostExecute(HttpResponse response) {
			// get JSON Object
			JSONObject json;
			try {
				json = new JSONObject(convertStreamToString(response.getEntity().getContent()));
				result = json.getJSONObject("memory");
			} catch (IllegalStateException e1) {
				e1.printStackTrace();
			} catch (JSONException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			// If successful, populate view
			if (response.getStatusLine().getStatusCode() == 200) {
				ImageView imageView = (ImageView) findViewById(R.id.image);
	    	    TextView distanceTextView = (TextView) findViewById(R.id.distance);
	    	    TextView captionTextView = (TextView) findViewById(R.id.caption);
	    	    
	    	    try {
	    	    	String url = "http://128.61.107.111:56788/media/" + result.getString("image");
		    	    
					ItemListActivity.imageLoader.DisplayImage(url, imageView);
	    	    	distanceTextView.setText(result.getString("image_text"));
	    	    	captionTextView.setText(result.getString("distance"));
	    	    } catch (Exception e) {
	    	    	e.printStackTrace();
	    	    }
			} else {
				Toast.makeText(getBaseContext(), "There has been an internal server error", Toast.LENGTH_LONG).show();
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
}
