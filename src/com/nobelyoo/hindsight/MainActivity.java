package com.nobelyoo.hindsight;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.nobelyoo.hindsight.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                	// Create a new HttpClient and Post Header
                	HttpClient httpClient = new DefaultHttpClient();
                	HttpGet httpGet = new HttpGet("http://128.61.107.111:56788/users/provide_csrf/");
                	HttpResponse getResponse = httpClient.execute(httpGet);
                	
                	HeaderElement CSRFTOKEN = getResponse.getFirstHeader("Set-Cookie").getElements()[0];
                    HttpPost httpPost = new HttpPost("http://128.61.107.111:56788/users/verify/");
                    httpPost.setHeader("X-CSRFToken", CSRFTOKEN.getValue());
                	// Execute HTTP Post Request
                    HttpResponse response = httpClient.execute(httpPost);

                    String result = EntityUtils.toString(response.getEntity());
                    
                    System.out.println(result);
                    if (result == "True") {
                    	Intent intent = new Intent(this, DisplayMemoriesActivity.class);
                    	startActivity(intent);
                    } else {
                    	Intent intent = new Intent(this, LoginActivity.class);
                    	startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start(); 
	}
}
