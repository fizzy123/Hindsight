package com.nobelyoo.hindsight;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class UploadActivity extends Activity {

	private static final String TAG = "CallCamera";
	private static final int CAPTURE_IMAGE_ACTIVITY_REQ = 0;		  
	Uri fileUri = null;
	ImageView photoImage = null;
	private EditText mCaptionView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);
		
		// Grab views
		photoImage = (ImageView) findViewById(R.id.photo_image);
		mCaptionView = (EditText) findViewById(R.id.caption);

		// Start Camera Activity
		Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = Uri.fromFile(getOutputPhotoFile());
        i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQ );
        
        // Set listener for upload button
		Button callCameraButton = (Button) findViewById(R.id.button_upload);
		callCameraButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				new UploadMemoryTask().execute();
		      }
		});
	}
	
	// Grab space and name to save photo.
	private File getOutputPhotoFile() {
		  File directory = new File(Environment.getExternalStoragePublicDirectory(
		                Environment.DIRECTORY_PICTURES), getPackageName());
		  if (!directory.exists()) {
		    if (!directory.mkdirs()) {
		      Log.e(TAG, "Failed to create storage directory.");
		      return null;
		    }
		  }
		  // Grab timeStamp and name file based on timeStamp
		  String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss", Locale.US).format(new Date());
		  return new File(directory.getPath() + File.separator + "IMG_"  
		                    + timeStamp + ".jpg");
		}
	
	// Runs when camera activity returns.
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		  if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQ) {
		    if (resultCode == RESULT_OK) {
		      Uri photoUri = null;
		      if (data == null) {
		        // A known bug with certain phones here! The image should have saved in fileUri
		        Toast.makeText(this, "Image saved successfully", 
		                       Toast.LENGTH_LONG).show();
		        photoUri = fileUri;
		      } else {
		        photoUri = data.getData();
		        Toast.makeText(this, "Image saved successfully in: " + data.getData(), 
		                       Toast.LENGTH_LONG).show();
			    fileUri = photoUri;
		      }
		      showPhoto(photoUri);
		    } else if (resultCode == RESULT_CANCELED) {
		      Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
		    } else {
		      Toast.makeText(this, "Callout for image capture failed!", 
		                     Toast.LENGTH_LONG).show();
		    }
		  }
		}
	
	// Set photoImage Drawable to the picture that was just taken. 
	private void showPhoto(Uri photoUri) {
		String filePath = photoUri.getEncodedPath(); 
		File imageFile = new File(filePath);
		
		  if (imageFile.exists()){
			 Drawable oldDrawable = photoImage.getDrawable();
			 if (oldDrawable != null) { ((BitmapDrawable)oldDrawable).getBitmap().recycle(); }
		     Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
		     BitmapDrawable drawable = new BitmapDrawable(this.getResources(), bitmap);
		     photoImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
		     photoImage.setImageDrawable(drawable);
		  }       
		}
	
	/**
	 * Uploads memory data in separate thread
	 */
	private class UploadMemoryTask extends AsyncTask<Void, Void, String> {
	    	
		protected String doInBackground(Void... args) {
			Intent intent = getIntent();
			
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
				
			    // Set up Post Request
			    HttpPost httpPost = new HttpPost("http://128.61.107.111:56788/memories/add/");
			    
			    // Adds CSRF Token
				httpPost.setHeader("X-CSRFToken", CSRFTOKEN);
				
				// Add file and text arguments to multipart entity
			    File file = new File(fileUri.getEncodedPath());
			    MultipartEntityBuilder mpEntityBuilder = MultipartEntityBuilder.create();  
			    ContentBody cbFile = new FileBody(file);
			    mpEntityBuilder.addPart("memory", cbFile); 
			    mpEntityBuilder.addTextBody("longitude", intent.getStringExtra(ItemListActivity.LONGITUDE));
			    mpEntityBuilder.addTextBody("latitude", intent.getStringExtra(ItemListActivity.LATITUDE));
			    String caption = mCaptionView.getText().toString();
			    if (TextUtils.isEmpty(caption)) {
				    mpEntityBuilder.addTextBody("caption", caption);
			    }
			    
			    // Build entity and add to Post Request
			    final HttpEntity mpEntity = mpEntityBuilder.build();
			    httpPost.setEntity(mpEntity);
			    
			    // execute post request
			    System.out.println("executing request " + httpPost.getRequestLine());
			    HttpResponse httpResponse = httpClient.execute(httpPost, localContext);
			    if (httpResponse.getStatusLine().getStatusCode() == 200) {
			    	return "SUCCESS";
			    } else {
			    	return null;
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
			// If successful, populate view
			if (status.equals("SUCCESS")) {
				Intent intent = new Intent(getBaseContext(), ItemListActivity.class);
				startActivity(intent);
				finish();
			} else {
				Toast.makeText(getBaseContext(), "There has been an internal server error", Toast.LENGTH_LONG).show();
			}
		}
	}
}
