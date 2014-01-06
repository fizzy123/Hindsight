package com.nobelyoo.hindsight;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

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
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class UploadActivity extends Activity {

	private static final String TAG = "CallCamera";
	private static final int CAPTURE_IMAGE_ACTIVITY_REQ = 0;		  
	Uri fileUri = null;
	ImageView photoImage = null;
	private Intent intent;
	private EditText mCaptionView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);
		photoImage = (ImageView) findViewById(R.id.photo_image);
		mCaptionView = (EditText) findViewById(R.id.caption);

		intent = getIntent();
		Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = getOutputPhotoFile();
        fileUri = Uri.fromFile(getOutputPhotoFile());
        i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQ );
        
		Button callCameraButton = (Button) findViewById(R.id.button_upload);
		callCameraButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				new UploadMemoryTask().execute();
		      }
		});
	}
	
	private File getOutputPhotoFile() {
		  File directory = new File(Environment.getExternalStoragePublicDirectory(
		                Environment.DIRECTORY_PICTURES), getPackageName());
		  if (!directory.exists()) {
		    if (!directory.mkdirs()) {
		      Log.e(TAG, "Failed to create storage directory.");
		      return null;
		    }
		  }
		  String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss", Locale.US).format(new Date());
		  return new File(directory.getPath() + File.separator + "IMG_"  
		                    + timeStamp + ".jpg");
		}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		  if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQ) {
		    if (resultCode == RESULT_OK) {
		      Uri photoUri = null;
		      if (data == null) {
		        // A known bug here! The image should have saved in fileUri
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
	 * Uploads data in separate thread
	 */
	private class UploadMemoryTask extends AsyncTask<Void, Void, Boolean> {
	    
		private int result = 0;
		
		protected Boolean doInBackground(Void... args) {
			try {
				// Create a new HttpClient
				
				HttpClient httpClient = new DefaultHttpClient();
			    //httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
				
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
				
				CookieStore cookieStore = new BasicCookieStore();
				BasicClientCookie cookie = new BasicClientCookie("sessionid", sessionid);
				cookie.setVersion(0);
				cookie.setDomain("128.61.107.111");
				cookie.setPath("/");
				cookieStore.addCookie(cookie);
				cookie = new BasicClientCookie("csrftoken", CSRFTOKEN);
				cookie.setVersion(0);
				cookie.setDomain("128.61.107.111");
				cookie.setPath("/");
				cookieStore.addCookie(cookie);
				// Create local HTTP context
			    HttpContext localContext = new BasicHttpContext();
			    // Bind custom cookie store to the local context
			    localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
				
			    HttpPost httpPost = new HttpPost("http://128.61.107.111:56788/memories/add/");
			    httpPost.setHeader("sessionid", sessionid);
				httpPost.setHeader("X-CSRFToken", CSRFTOKEN);
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
			    
			    final HttpEntity mpEntity = mpEntityBuilder.build();
			    httpPost.setEntity(mpEntity);
			    System.out.println("executing request " + httpPost.getRequestLine());
			    HttpResponse response = httpClient.execute(httpPost, localContext);
			    result = response.getStatusLine().getStatusCode();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}

		/*
		 * Populate view
		 */
		protected void onPostExecute(Boolean success) {
			// If successful, populate view
			if (success) {
				if (result == 200) {
					Intent intent = new Intent(getBaseContext(), ItemListActivity.class);
					startActivity(intent);
					finish();
				} else {
					
				}
			}
		}
	}
}