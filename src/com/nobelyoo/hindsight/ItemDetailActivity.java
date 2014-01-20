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
		
		TextView distanceView = (TextView) findViewById(R.id.distance);
		TextView kmView = (TextView) findViewById(R.id.km);
		TextView ownerView = (TextView) findViewById(R.id.owner);
		TextView captionView = (TextView) findViewById(R.id.caption);
		ImageView imageView = (ImageView) findViewById(R.id.image);
		
		Intent intent = getIntent();
		distanceView.setText(intent.getStringExtra(ItemListActivity.DISTANCE));
		ownerView.setText(intent.getStringExtra(ItemListActivity.OWNER));
		captionView.setText(intent.getStringExtra(ItemListActivity.CAPTION));

    	String url = "http://128.61.107.111:56788/media/" + intent.getStringExtra(ItemListActivity.IMAGE);
	    
		ItemListActivity.imageLoader.DisplayImage(url, imageView);
		//set font
		Typeface font=Typeface.createFromAsset(getAssets(),"font/BEBASNEUE.OTF");
		distanceView.setTypeface(font);
		kmView.setTypeface(font);
		ownerView.setTypeface(font);
		
		font=Typeface.createFromAsset(getAssets(),"font/TRADITIONELLSANS-BOLD.TTF");
		captionView.setTypeface(font);
	}
}
