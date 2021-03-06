package com.nobelyoo.hindsight;

import java.io.Console;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends BaseActivity {

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_USERNAME = "com.example.android.authenticatordemo.extra.EMAIL";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mUsername;
	private String mPassword;

	// UI references.
	private EditText mUsernameView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		// Set up the login form.
		mUsername = getIntent().getStringExtra(EXTRA_USERNAME);
		mUsernameView = (EditText) findViewById(R.id.username);
		mUsernameView.setText(mUsername);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
		
		Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
		Button mRegisterButton = (Button) findViewById(R.id.register_button);
		
		//set font
		Typeface font=Typeface.createFromAsset(getAssets(),"font/BEBASNEUE.OTF");
		mUsernameView.setTypeface(font);
		mPasswordView.setTypeface(font);
		mSignInButton.setTypeface(font);
		mRegisterButton.setTypeface(font);
		
		// The sign in button
		mSignInButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});

		// The register button
		mRegisterButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				goRegister();
			}
		});

	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mUsername = mUsernameView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mUsername)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
				}
			});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
				}
			});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
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
	 * Goes to the register screen
	 */
	private void goRegister() {
		Intent intent = new Intent(getBaseContext(), RegisterActivity.class);
		startActivity(intent);
		finish();
	}
	
	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, String> {
		
		@Override
		protected String doInBackground(Void... params) {

			try {
				// Connect to the server and try to login
				HttpClient httpClient = new DefaultHttpClient();
				
				// Get CSRF token
				HttpGet httpGet = new HttpGet("http://108.234.92.163:56788/users/provide_csrf/");
				HttpResponse getResponse = httpClient.execute(httpGet);
				HeaderElement[] headerElements = getResponse.getFirstHeader("Set-Cookie").getElements();
				String CSRFTOKEN = "";
				for (HeaderElement element:headerElements){
					if (element.getName().equals("csrftoken")){
						CSRFTOKEN = element.getValue();
					}
				}
				
				// Build Post Request
				HttpPost httpPost = new HttpPost("http://108.234.92.163:56788/users/login/");
				httpPost.setHeader("X-CSRFToken", CSRFTOKEN); //Attach CSRF token to POST request
				
				// Request parameters and other properties.
				List<NameValuePair> context = new ArrayList<NameValuePair>(2);
				context.add(new BasicNameValuePair("username", mUsername));
				context.add(new BasicNameValuePair("password", mPassword));
				httpPost.setEntity(new UrlEncodedFormEntity(context, "UTF-8"));
				
				//Execute POST Request
				HttpResponse httpResponse = httpClient.execute(httpPost);
		
				// Grabs status code and session_id
				int result = httpResponse.getStatusLine().getStatusCode();
				JSONObject json = new JSONObject(convertStreamToString(httpResponse.getEntity().getContent()));
				String username = json.getString("username");
				// If everything is successful
				if (result == 200) {
					// Get sessionid from headers
					Header[] headers = httpResponse.getHeaders("Set-Cookie");
					String sessionid = "";
					for (Header header:headers) {
						for (HeaderElement element:header.getElements()){
							if (element.getName().equals("sessionid")){
								sessionid = element.getValue();
							}
						}
					}
					
					// Save sessionid in preferences
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					Editor edit = prefs.edit();
					edit.putString("sessionid", sessionid);
					edit.putString("username", username);
					edit.apply();
					//Go to home screen
					return "SUCCESS";
				} else if (result == 403){
					if (httpResponse.getEntity().getContent().toString().equals("NOT_ACTIVE")) {
						return "NOT_ACTIVE";
					}
					return "FORBIDDEN";
				} else if (result == 500){
					return "SERVER_ERROR";
				}	
				
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(final String status) {
			mAuthTask = null;
			showProgress(false);
			if (status.equals("SUCCESS")) {
				goHome();
			} else if (status.equals("NOT_ACTIVE")) {
				Toast.makeText(getBaseContext(), "Your account has not been activated. Please check your email.", Toast.LENGTH_LONG).show();
			} else if (status.equals("FORBIDDEN")) {
				Toast.makeText(getBaseContext(), "No account with that username and password has been found", Toast.LENGTH_LONG).show();
			} else if (status.equals("SERVER_ERROR")){
				Toast.makeText(getBaseContext(), "There has been an internal server error", Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
		
	} // end inner class
	
} // end class
