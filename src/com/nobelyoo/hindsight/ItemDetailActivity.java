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
import android.graphics.Typeface;
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
		
		TextView textView = (TextView) findViewById(R.id.distance);
		TextView kmView = (TextView) findViewById(R.id.km);
		TextView ownerView = (TextView) findViewById(R.id.owner);
		TextView captionView = (TextView) findViewById(R.id.caption);
		
		//set font
		Typeface font=Typeface.createFromAsset(getAssets(),"font/BEBASNEUE.OTF");
		textView.setTypeface(font);
		kmView.setTypeface(font);
		ownerView.setTypeface(font);
		
		font=Typeface.createFromAsset(getAssets(),"font/TRADITIONELLSANS-BOLD.TTF");
		captionView.setTypeface(font);
	}
	
	/**
	 * Loads data in separate thread
	 */
	private class LoadMemoryTask extends AsyncTask<Void, Void, String> {
	    
		private JSONObject result;
		protected String doInBackground(Void... args) {
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
				HttpResponse httpResponse = httpClient.execute(httpGet);
				
				JSONObject json = new JSONObject(convertStreamToString(httpResponse.getEntity().getContent()));
				result = json.getJSONObject("memory");
				
				// If successful, populate view
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					return "SUCCESS";
				} else if (httpResponse.getStatusLine().getStatusCode() == 500) {
					return "SERVER_ERROR";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		/*
		 * Populate view
		 */
		protected void onPostExecute(String status) {
			if (status.equals("SUCCESS")) {
				ImageView imageView = (ImageView) findViewById(R.id.image);
	    	    TextView distanceTextView = (TextView) findViewById(R.id.distance);
	    	    TextView captionTextView = (TextView) findViewById(R.id.caption);
	    	    TextView ownerTextView = (TextView) findViewById(R.id.owner);
	    	    
	    	    try {
	    	    	String url = "http://128.61.107.111:56788/media/" + result.getString("image");
		    	    
					ItemListActivity.imageLoader.DisplayImage(url, imageView);
	    	    	captionTextView.setText(result.getString("caption"));
	    	    	distanceTextView.setText(result.getString("distance")); 
	    	    	ownerTextView.setText(result.getString("owner"));
	    	    } catch (Exception e) {
	    	    	e.printStackTrace();
	    	    }
			} else if (status.equals("SERVER_ERROR")){
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
