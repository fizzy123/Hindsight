package com.nobelyoo.hindsight;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

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
				Header[] headers = getResponse.getHeaders("Set-Cookie");
				String CSRFTOKEN = "";
				for (Header header:headers) {
					for (HeaderElement element:header.getElements()){
						if (element.getName().equals("csrftoken")){
							CSRFTOKEN = element.getValue();
						}
					}
				}
				// check session id for user
				// Get saved preferences for current user
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				String sessionid = prefs.getString("sessionid", null);
				
				// Create CookieStore
				CookieStore cookieStore = new BasicCookieStore();
				
				// Set up cookie for sessionid
				BasicClientCookie cookie = new BasicClientCookie("sessionid", sessionid);
				cookie.setVersion(0);
				cookie.setDomain("128.61.107.111");
				cookie.setPath("/");
				cookieStore.addCookie(cookie);
				
				// Set up cookie for csrftoken
				cookie = new BasicClientCookie("csrftoken", CSRFTOKEN);
				cookie.setVersion(0);
				cookie.setDomain("128.61.107.111");
				cookie.setPath("/");
				cookieStore.addCookie(cookie);
				
				// Create local HTTP context
			    HttpContext localContext = new BasicHttpContext();
			    // Bind custom cookie store to the local context
			    localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
				
				// Verify if user has a valid session id
				HttpPost httpPost = new HttpPost("http://128.61.107.111:56788/users/verify/");
				
				// Set CSRF Header
				httpPost.setHeader("X-CSRFToken", CSRFTOKEN);
				
				// Execute HTTP Post Request
				HttpResponse response = httpClient.execute(httpPost, localContext);
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
