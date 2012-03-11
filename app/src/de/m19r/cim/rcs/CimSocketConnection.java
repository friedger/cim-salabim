package de.m19r.cim.rcs;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.m19r.cim.ctrl.impl.CimController;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import jibe.sdk.client.simple.SimpleConnectionStateListener;
import jibe.sdk.client.simple.session.DatagramSocketConnection;

public class CimSocketConnection extends DatagramSocketConnection {

	private static final boolean DEBUG = true;

	private static final String LOG_TAG = CimSocketConnection.class.getName();

	private boolean mDoSending = false;	

	private boolean mDoReceiving = false;
	private Thread receiverThread;

	private DatagramSocketConnection mConnection;

	BlockingQueue<String> mQueue = new LinkedBlockingQueue<String>();
	Object mSyncRoot = new Object();

	Thread mSenderThread;

	protected CimCommandListener mListener;

	Thread mReceiverThread = new Thread(new Runnable() {

		@Override
		public void run() {

			try {

				byte[] receiveBuffer = new byte[8192];

				while (mDoReceiving) {

					int i = 0;
					try {
						i = mConnection.receive(receiveBuffer, 0,
								receiveBuffer.length);

					} catch (Exception e) {
						mDoReceiving = false;
						e.printStackTrace();
						break;
					}

					if (i == 2) {
						Log.i(LOG_TAG, "Received ack msg");
						synchronized (mSyncRoot) {
							mSyncRoot.notify();
						}
					} else {

						Log.i(LOG_TAG,
								"Request received! Payload: payload size: " + i);

						ByteArrayInputStream bis = new ByteArrayInputStream(
								receiveBuffer);
						String txt = new String(receiveBuffer);

						// maybe mListener is blocking
						if (mListener != null) {
							mListener.received(txt.substring(2));
						}

						Log.i(LOG_TAG, "Sending ack msg!");
						try {
							mConnection.send(new byte[] { 0, 0 }, 0, 2);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			
			}

		}
	});

	public CimSocketConnection(Context cxt,
			SimpleConnectionStateListener listener) {
		super(cxt, listener);

		mSenderThread = new Thread(new Runnable() {

			@Override
			public void run() {

				String txt;
				try {
					txt = mQueue.take();

					byte[] packet;
					try {
						packet = txt.getBytes("UTF-8");

						Log.i(LOG_TAG, "Sending:" + txt);

						mConnection.send(packet, 0, packet.length);

						synchronized (mSyncRoot) {
							mSyncRoot.wait(5000);
							// TOOD check msg as "failed"
						}

					} catch (Exception e) {
						e.printStackTrace();
						stop();
					}

				} catch (InterruptedException e1) {
					e1.printStackTrace();
					stop();
				}

			}
		});
		
		mSenderThread.start();
	}

	public void stop() {
		mDoSending = false;
		mDoReceiving = false;
	}

	public void sendText(final String txt) {

		if (DEBUG) {
			Log.d(LOG_TAG, "Start sending packets");
		}
		mQueue.offer("RE" + txt);

	}

	public void startReceivingPackets() {

		Log.d(LOG_TAG, "Start receiving packets");
		mDoReceiving = true;

		receiverThread.start();
	}

	public void setCimListener(CimCommandListener cimCommandListener) {
		mListener = cimCommandListener;
		
	}

}
