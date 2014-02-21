package com.nobelyoo.hindsight;

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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
public class RegisterActivity extends Activity {

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserRegistrationTask mRegistrationTask = null;

	// Values for email and password at the time of the login attempt.
	private String mUsername;
	private String mEmail;
	private String mPassword;
	private String mPasswordConfirm;

	// UI references.
	private EditText mUsernameView;
	private EditText mEmailView;
	private EditText mPasswordView;
	private EditText mPasswordConfirmView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_register);

		// Set up the login form.
		mUsernameView = (EditText) findViewById(R.id.username);
		
		mEmailView = (EditText) findViewById(R.id.email);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptRegister();
					return true;
				}
				return false;
			}
		});

		mPasswordConfirmView = (EditText) findViewById(R.id.passwordConfirm);
		mPasswordConfirmView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptRegister();
					return true;
				}
				return false;
			}
		});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
		
		Button mRegisterButton = (Button) findViewById(R.id.register_button);
		//set font
		Typeface font=Typeface.createFromAsset(getAssets(),"font/BEBASNEUE.OTF");
		mUsernameView.setTypeface(font);
		mPasswordView.setTypeface(font);
		mRegisterButton.setTypeface(font);

		findViewById(R.id.register_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptRegister();
			}
		});
	}

	/**
	 * Attempts to register the account specified by the register form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual register attempt is made.
	 */
	public void attemptRegister() {
		if (mRegistrationTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);
		mPasswordConfirmView.setError(null);

		// Store values at the time of the login attempt.
		mUsername = mUsernameView.getText().toString();
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		mPasswordConfirm = mPasswordConfirmView.getText().toString();

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

		// Check that the passwords are equal.
		if (!mPasswordConfirm.equals(mPassword)) {
			mPasswordConfirmView.setError(getString(R.string.error_not_identical));
			focusView = mPasswordConfirmView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
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
			mRegistrationTask = new UserRegistrationTask();
			mRegistrationTask.execute((Void) null);
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
	
	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserRegistrationTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {

			try {
				// Connect to the server and try to login
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
				
				// Build POST request
				HttpPost httpPost = new HttpPost("http://108.234.92.163:56788/users/create/");
				httpPost.setHeader("X-CSRFToken", CSRFTOKEN); //Attach CSRF token to POST request
				
				// Request parameters and other properties.
				List<NameValuePair> context = new ArrayList<NameValuePair>(2);
				context.add(new BasicNameValuePair("username", mUsername));
				context.add(new BasicNameValuePair("email", mEmail));
				context.add(new BasicNameValuePair("password", mPassword));
				httpPost.setEntity(new UrlEncodedFormEntity(context, "UTF-8"));
				
				// Executes POST request
				HttpResponse response = httpClient.execute(httpPost);
				int status = response.getStatusLine().getStatusCode();
				if (status == 200) {
					return "SUCCESS";
				} else if (status == 403) {
					return response.getEntity().getContent().toString();
				} else if (status == 500) {
					return "SERVER_ERROR";
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(final String status) {
			// If server returns successful, go to home screen.
			if (status.equals("SUCCESS")) {
				goHome();
			} else if (status.equals("EMAIL_TAKEN")) {
				Toast.makeText(getApplicationContext(), "That email address has already been taken", Toast.LENGTH_LONG).show();
			} else if (status.equals("USERNAME_TAKEN")) {
				Toast.makeText(getApplicationContext(), "That username has already been taken", Toast.LENGTH_LONG).show();
			} else if (status.equals("SERVER_ERROR")) {
				Toast.makeText(getApplicationContext(), "There was a server error", Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected void onCancelled() {
			mRegistrationTask = null;
			showProgress(false);
		}
		
	} // end inner class
	
} // end class
