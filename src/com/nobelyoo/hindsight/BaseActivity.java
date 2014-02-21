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

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.widget.PopupMenu.OnMenuItemClickListener;

public class BaseActivity extends Activity {
	public final static String LONGITUDE = "com.nobelyoo.hindsight.LONGITUDE";
	public final static String LATITUDE = "com.nobelyoo.hindsight.LATITUDE";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_base);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return super.onCreateOptionsMenu(menu);
	}

	private class LogoutTask extends AsyncTask<Void, Void, String> {

		protected String doInBackground(Void... args) {
		//	Intent intent = getIntent();
			
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
				
			    // Set up Post Request
			    HttpPost httpPost = new HttpPost("http://108.234.92.163:56788/users/logout/");
			    
			    // Adds CSRF Token
				httpPost.setHeader("X-CSRFToken", CSRFTOKEN);
			    
			    // execute post request
			    System.out.println("executing request " + httpPost.getRequestLine());
			    HttpResponse httpResponse = httpClient.execute(httpPost, localContext);
			    if (httpResponse.getStatusLine().getStatusCode() == 200) {
			    	return "SUCCESS";
			    } else if (httpResponse.getStatusLine().getStatusCode() == 500){
			    	return "SERVER_ERROR";
			    }
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		/*
		 * Populate list view
		 */
		protected void onPostExecute(String status) {
			// If successful, refresh list view
			if (status.equals("SUCCESS")) {
				// Save sessionid in preferences
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				Editor edit = prefs.edit();
				edit.putString("username", null);
				edit.apply();
				Toast.makeText(getBaseContext(), "You've successfully been logged out.", Toast.LENGTH_LONG).show();
			} else if(status.equals("SERVER_ERROR")){
				Toast.makeText(getBaseContext(), "There has been an internal server error", Toast.LENGTH_LONG).show();
			}
		}
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
		// Get saved preferences for current user
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		// Check session id for user
		String username = prefs.getString("username", null);
		Intent intent;
	    switch (item.getItemId()) {
	    	case android.R.id.home:
	        	intent = new Intent(BaseActivity.this, ItemListActivity.class);
	    		startActivity(intent);
	    		return true;
	        case R.id.camera:
	        	if (username != null) {
		        	intent = new Intent(BaseActivity.this, UploadActivity.class);

		        	startActivity(intent);
	        	} else {
					Toast.makeText(getBaseContext(), "Sorry. You need to be logged in to add memories", Toast.LENGTH_LONG).show();
	        	} 
	        	return true;
	        case R.id.menu:
	        	View menuView = findViewById(R.id.menu); 
	        	PopupMenu popup = new PopupMenu(this, menuView);
				
	        	OnMenuItemClickListener listener = new OnMenuItemClickListener() {
	        		@Override
	        		public boolean onMenuItemClick(MenuItem item){
	        			Intent intent;
	        			switch(item.getItemId()) {
		        			case R.id.login:
		        				intent = new Intent(getBaseContext(), LoginActivity.class);
		        				startActivity(intent);
		        				finish();
		        				return true;
		        			case R.id.logout:
		        				LogoutTask logoutTask = new LogoutTask();
		        				logoutTask.execute();
		        				return true;
		        			case R.id.view_owned:
		        				intent = new Intent(getBaseContext(), OwnedItemListActivity.class);
		        				startActivity(intent);
		        				finish();
		        				return true;
		        			case R.id.register:	
		        				intent = new Intent(getBaseContext(), RegisterActivity.class);
		        				startActivity(intent);
		        				finish();
		        				return true;
		        			default:
		        				return false;
	        			}
	        		}
	        	};
	        	popup.setOnMenuItemClickListener(listener);
	            MenuInflater inflater = popup.getMenuInflater();
	            if (username != null){
		            inflater.inflate(R.menu.authenticated_actions, popup.getMenu());
				} else {
		            inflater.inflate(R.menu.unauthenticated_actions, popup.getMenu());
				}
	            popup.show();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
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
