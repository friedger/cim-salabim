package de.m19r.cim.ui;

import java.io.IOException;

import jibe.sdk.client.JibeIntents;
import jibe.sdk.client.simple.authentication.AuthenticationHelper;
import jibe.sdk.client.simple.authentication.AuthenticationHelperListener;
import jibe.sdk.client.simple.session.DatagramSocketConnection;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;
import de.m19r.cim.CimSalabimApplication;
import de.m19r.cim.R;

public class CimSalabimActivity extends Activity {
	
	private static final String LOG_TAG = CimSalabimActivity.class
			.getName();
	
	private DatagramSocketConnection mConnection;
	private AuthenticationHelper mAuthHelper;

	private SurfaceView mImage;
	private final static int AUTHENTICATING_DIALOG = 1;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mImage = (SurfaceView) findViewById(R.id.surfaceview);
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		// need to make sure that the Jibe Realtime Engine has successfully
		// authenticated the user with the Jibe Cloud
		triggerJibeAuthentication();

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

		
	private void resetConnection() {
		/*
		 * This may be called by callbacks, and may therefore not necessarily be
		 * run within the UI context. In order to be able to update the screen,
		 * force this to run as part of the UI thread.
		 */
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//disableUiButtons();
			}
		});

		createConnection();
	}

	private void createConnection() {
		if (mConnection != null) {
			
			try {
				mConnection.close();
			} catch (IOException e) {
				Log.w(LOG_TAG, "Failed to close connection.");
				e.printStackTrace();
			}
			unregisterReceiver(mReceiver);
		}
		registerReceiver(mReceiver, new IntentFilter(
				JibeIntents.ACTION_INCOMING_SESSION + '.'
						+ CimSalabimApplication.APP_ID));

		mConnection = new DatagramSocketConnection(this, null);
		
	}
	
	private boolean incomingSession(Intent intent) {
		try {
			// Attach incoming intent to connection.
			mConnection.attachIncomingSession(intent);
			
			//setUiButtonsForIncomingConnection();
			return true;
		} catch (IllegalArgumentException iex) {
			Log.w(LOG_TAG, "Wrong intent");
		}
		return false;
	}
	
	
	private void showMessage(final String message) {
		if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					showMessage(message);
				}
			});

			return;
		}

		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, final Intent intent) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					incomingSession(intent);
				}
			});
		}
	};

	
	

	private AuthenticationHelperListener mAuthListener = new AuthenticationHelperListener() {
		@Override
		public void onReady() {
			try {
				mAuthHelper.startJibeAuthentication();
			} catch (IOException e) {
				// should never get here since calling startJibeAuthentication() inside onReady()
				// guarantees that the Jibe Realtime Engine is running already.
				e.printStackTrace();
			}
		}

		@Override
		public void onAuthenticationSuccessful() {
			Log.v(LOG_TAG, "authenticationSuccessful()");
			removeDialog(AUTHENTICATING_DIALOG);
			showMessage("Jibe Cloud authentication successful.");
			createConnection();
		}

		@Override
		public void onAuthenticationFailed(int failureInfo) {
			Log.v(LOG_TAG, "authenticationFailed(). Info:" + failureInfo);
			removeDialog(AUTHENTICATING_DIALOG);
			showMessage("Jibe Cloud authentication failed. Reason:"
					+ failureInfo);
		}
	};
}