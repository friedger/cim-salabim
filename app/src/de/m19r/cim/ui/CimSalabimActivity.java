package de.m19r.cim.ui;

import java.io.IOException;

import jibe.sdk.client.JibeIntents;
import jibe.sdk.client.simple.authentication.AuthenticationHelper;
import jibe.sdk.client.simple.authentication.AuthenticationHelperListener;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import de.m19r.cim.CimIntents;
import de.m19r.cim.CimSalabimApplication;
import de.m19r.cim.R;
import de.m19r.cim.ctrl.ImageCommand;
import de.m19r.cim.ctrl.TextCommand;
import de.m19r.cim.ctrl.impl.CimController;
import de.m19r.cim.rcs.CimSocketConnection;
import de.m19r.cim.ui.widget.ImageEditView;

public class CimSalabimActivity extends Activity {

	private static final String LOG_TAG = CimSalabimActivity.class.getName();

	private CimSocketConnection mConnection;
	private AuthenticationHelper mAuthHelper;

	private ImageEditView mImage;
	private ImageButton mAddButton;
	private CimController mCimController;

	private EditText mTextButton;
	private final static int AUTHENTICATING_DIALOG = 1;

	private static final int REQUEST_FRIEND = 1000;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mCimController = new CimController();
		
		mImage = (ImageEditView) findViewById(R.id.surfaceview);		
		mImage.setCimController(mCimController);
		
		mAddButton = (ImageButton) findViewById(R.id.imageButton1);
		mTextButton = (EditText)findViewById(R.id.editText1);
		mTextButton.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				mCimController.popCommand();
				mCimController.pushCommand(new TextCommand(mTextButton.getText().toString(), 40, 40));
				mImage.invalidate();
				return true;
			}
		});
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

	public void onAddClick(View target) {
		startActivityForResult(new Intent(this, FindFriendsActivity.class),
				REQUEST_FRIEND);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_FRIEND) {
				openConnection(data.getStringExtra(CimIntents.EXTRA_MSISDN));
			}
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
				// disableUiButtons();
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

		mConnection = new CimSocketConnection(this, null);
		mConnection.setAutoAccept(true);
		mCimController.pushCommand(new ImageCommand("http://de.droidcon.com/dc2011/images/logos/2d/droid_con500.jpg"));
		mCimController.pushCommand(new TextCommand("", 20, 20));
	}

	private void openConnection(String remoteUserId) {

		try {
			mConnection.open(remoteUserId);
		} catch (Exception e) {
			Log.w(LOG_TAG, "Failed to open connection.");
			e.printStackTrace();
			showMessage(e.getMessage());
			resetConnection();
		}
	}

	private void acceptIncomingConnection() {
		try {
			mConnection.accept();
		} catch (IOException e) {
			Log.w(LOG_TAG, "Failed to accept connection.");
			e.printStackTrace();
			showMessage(e.getMessage());
			resetConnection();
		}
	}

	private void rejectIncomingConnection() {
		try {
			mConnection.reject();
		} catch (IOException e) {
			Log.w(LOG_TAG, "Failed to reject connection.");
			e.printStackTrace();
		} finally {
			resetConnection();
		}
	}

	private boolean incomingSession(Intent intent) {
		try {
			// Attach incoming intent to connection.
			mConnection.attachIncomingSession(intent);
			mConnection.startReceivingPackets();
			mAddButton.setEnabled(false);
			// setUiButtonsForIncomingConnection();
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
				// should never get here since calling startJibeAuthentication()
				// inside onReady()
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