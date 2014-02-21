package com.nobelyoo.hindsight;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LoadActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_load);

		TextView myTextView=(TextView)findViewById(R.id.textViewTitle);
		Typeface font=Typeface.createFromAsset(getAssets(),"font/ORATORSTD.OTF");
		
		myTextView.setTypeface(font);
		 
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
	private class LoadDataTask extends AsyncTask<String, Void, String> {
		String username;

		protected String doInBackground(String... args) {
			try {
				// Create a new HttpClient
				HttpClient httpClient = new DefaultHttpClient();
				
				// Get CSRF token
				HttpGet httpGet = new HttpGet("http://108.234.92.163:56788/users/provide_csrf/");
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
				cookie.setDomain("108.234.92.163");
				cookie.setPath("/");
				cookieStore.addCookie(cookie);
				
				// Set up cookie for csrftoken
				cookie = new BasicClientCookie("csrftoken", CSRFTOKEN);
				cookie.setVersion(0);
				cookie.setDomain("108.234.92.163");
				cookie.setPath("/");
				cookieStore.addCookie(cookie);
				
				// Create local HTTP context
			    HttpContext localContext = new BasicHttpContext();
			    // Bind custom cookie store to the local context
			    localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
				
				// Verify if user has a valid session id
				HttpPost httpPost = new HttpPost("http://108.234.92.163:56788/users/verify/");
				
				// Set CSRF Header
				httpPost.setHeader("X-CSRFToken", CSRFTOKEN);
				
				// Execute HTTP Post Request
				HttpResponse response = httpClient.execute(httpPost, localContext); 
				
				int status = response.getStatusLine().getStatusCode();
				
				JSONObject json = new JSONObject(convertStreamToString(response.getEntity().getContent()));
				username = json.getString("username");
				if (status == 200) {
					return "SUCCESS";
				} else if (status == 500) {
					return "SERVER_ERROR";
				} else if (status == 403){
					return "FORBIDDEN";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		/*
		 * Pass the result data back to the main activity
		 */
		protected void onPostExecute(final String status) {
			if (status.equals("SERVER_ERROR")) {
				Toast.makeText(getBaseContext(), "Internal Server Error. Please try again later", Toast.LENGTH_LONG).show();
			} else {
				sendOnwards(status);
			}
		}

		/*
		 * Send to the next activity
		 */
		private void sendOnwards(final String status) {
			if (status.equals("SUCCESS")) {
				// Has connection and credentials
				// Save sessionid in preferences
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				Editor edit = prefs.edit();
				edit.putString("username", username);
				edit.apply();
				goHome();
			} else if (status.equals("FORBIDDEN")) {
				// Needs to login
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				Editor edit = prefs.edit();
				edit.putString("username", null);
				edit.apply();
				goHome();
			}
		}
	}
	
	/*
	 *  Converts Stream to String
	 */
	public static String convertStreamToString(InputStream is) {
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
