package com.nobelyoo.hindsight;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ProgressBar;
import android.widget.Toast;

public class LoadActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_load);

		// Check for network availability
		if (isNetworkAvailable() == true) {
			// Start a new thread that will download all the data
			new LoadDataTask().execute();
		} else {
			// No Internet connection found
			Toast.makeText(getBaseContext(), "No Internet Connection found", Toast.LENGTH_LONG).show();
			findViewById(R.id.progressBarLoad).setVisibility(ProgressBar.INVISIBLE);
			findViewById(R.id.textViewLoad).setVisibility(ProgressBar.INVISIBLE);
		}

	}

	/*
	 * Check for Internet connectivity
	 */
	private boolean isNetworkAvailable() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	/*
	 * Goes to the LoginActivity
	 */
	private void goLogin() {
		Intent intent = new Intent(getBaseContext(), LoginActivity.class);
		startActivity(intent);
		finish();
	}

	/*
	 * Goes to the home screen
	 */
	private void goHome() {
		Intent intent = new Intent(getBaseContext(), ItemListActivity.class);
		startActivity(intent);
		finish();
	}

	/*
	 * Loads data in separate thread
	 */
	private class LoadDataTask extends AsyncTask<String, Void, Object> {

		private int result = 0;

		protected Object doInBackground(String... args) {
			try {
				// Create a new HttpClient
				HttpClient httpClient = new DefaultHttpClient();
				
				// Get CSRF token
				HttpGet httpGet = new HttpGet("http://128.61.107.111:56788/users/provide_csrf/");
				HttpResponse getResponse = httpClient.execute(httpGet);
				HeaderElement[] headerElements = getResponse.getFirstHeader("Set-Cookie").getElements();
				String CSRFTOKEN = "";
				for (HeaderElement element:headerElements){
					if (element.getName().equals("csrftoken")){
						CSRFTOKEN = element.getValue();
					}
				}
				
				// check session id for user
				// Get saved preferences for current user
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				String sessionid = prefs.getString("sessionid", null);
				
				// Verify if user has a valid session id
				HttpPost httpPost = new HttpPost("http://128.61.107.111:56788/users/verify/");
				httpPost.setHeader("sessionid", sessionid);
				httpPost.setHeader("X-CSRFToken", CSRFTOKEN);
				// Execute HTTP Post Request
				HttpResponse response = httpClient.execute(httpPost);
				result = response.getStatusLine().getStatusCode();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}

		/*
		 * Pass the result data back to the main activity
		 */
		protected void onPostExecute(Object result) {
			sendOnwards();
		}

		/*
		 * Send to the next activity
		 */
		private void sendOnwards() {
			if (result == 200) {
				// Has connection and credentials
				goHome();
			} else {
				// Needs to login
				goLogin();
			}
		}

	}

}
