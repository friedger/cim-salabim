package de.m19r.cim.ui;

import java.io.IOException;

import jibe.sdk.client.simple.authentication.AuthenticationHelper;
import jibe.sdk.client.simple.session.DatagramSocketConnection;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import de.m19r.cim.R;

public class CimSalabimActivity extends Activity {
	
	private static final String LOG_TAG = CimSalabimActivity.class
			.getName();
	
	private DatagramSocketConnection mConnection;
	private AuthenticationHelper mAuthHelper;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// need to make sure that the Jibe Realtime Engine has successfully
		// authenticated the user with the Jibe Cloud
		triggerJibeAuthentication();

	}

	@Override
	protected void onDestroy() {
		try {
			mConnection.close();
		} catch (IOException e) {
			Log.w(LOG_TAG, "Failed to close connection.");
			e.printStackTrace();
		}

		unregisterReceiver(mReceiver);

		mAuthHelper.close();
		super.onDestroy();
	}

	private void triggerJibeAuthentication() {
		// first time start
		if (mAuthHelper == null) {
			// create AuthHelper and show up dialog
			showDialog(AUTHENTICATING_DIALOG);
			mAuthHelper = new AuthenticationHelper(this, mAuthListener);
		} else if (mAuthHelper.isAuthenticated()) {
			// app was in the background when authentication succeeded and event
			// may not have been received.
			removeDialog(AUTHENTICATING_DIALOG);
			if (mConnection == null) {
				createConnection();
			}
		} else if (!mAuthHelper.isAuthenticated()) {
			// Authentication did not succeed while app was in the background.
			// Most likely the Jibe
			// Realtime Engine was not installed and had to be downloaded. Start
			// over authentication.
			mAuthHelper.close();
			mAuthHelper = new AuthenticationHelper(this, mAuthListener);
		}

	}

}