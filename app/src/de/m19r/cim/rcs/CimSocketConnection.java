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
import java.util.Random;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import jibe.sdk.client.simple.SimpleConnectionStateListener;
import jibe.sdk.client.simple.session.DatagramSocketConnection;

public class CimSocketConnection extends DatagramSocketConnection {

	private static final boolean DEBUG = true;

	private static final String LOG_TAG = CimSocketConnection.class.getName();

	private boolean mIsSender = false;

	private boolean mDoSending = false;
	private Thread senderThread;

	private boolean mDoReceiving = false;
	private Thread receiverThread;

	private DatagramSocketConnection mConnection;

	public CimSocketConnection(Context cxt,
			SimpleConnectionStateListener listener) {
		super(cxt, listener);
	}

	public void stop() {
		mDoSending = false;
		mDoReceiving = false;
		mIsSender = false;
	}

	public void setIsSender(boolean isSender) {
		this.mIsSender = isSender;
	}

	public void sendText(final String txt) {

		if (DEBUG) {
			Log.d(LOG_TAG, "Start sending packets");
		}

		mDoSending = true;

		senderThread = new Thread(new Runnable() {

			@Override
			public void run() {

				if (mIsSender) {
					byte[] packet = txt.getBytes();
					Log.i(LOG_TAG, "Sending:" + txt);

					try {
						mConnection.send(packet, 0, packet.length);

					} catch (IOException iox) {
						stop();
					} catch (Exception e) {
						stop();
					}
				}
			}
		});
		senderThread.start();
	}

	public void startReceivingPackets() {

		Log.d(LOG_TAG, "Start receiving packets");
		mDoReceiving = true;
		receiverThread = new Thread(new Runnable() {

			@Override
			public void run() {

				BufferedWriter writer = null;
				try {

					writer = new BufferedWriter(new OutputStreamWriter(
							new FileOutputStream(new File(Environment
									.getExternalStorageDirectory(),
									"timing.txt)"))));

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
							Log.i(LOG_TAG, "Received pin holing package");
							continue;
						}

						Log.i(LOG_TAG,
								"Packet received! Payload: payload size: " + i);

						if (mIsSender) {
							ByteArrayInputStream bis = new ByteArrayInputStream(
									receiveBuffer);
							DataInputStream dis = new DataInputStream(bis);
							try {
								long timeStamp = dis.readLong();
								long current = System.currentTimeMillis();
								long roundtripTime = current - timeStamp;

								Log.i(LOG_TAG, "Roundtrip time was "
										+ roundtripTime);

								writer.write("" + roundtripTime);

							} catch (IOException e) {
								e.printStackTrace();
							}

						} else {
							Log.i(LOG_TAG,
									"Sending echo packet! Payload: payload size: "
											+ i);
							try {
								mConnection.send(receiveBuffer, 0, i);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						writer.flush();
						writer.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}
		});
		receiverThread.start();
	}

	private static byte[] generatePacket(int size) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
		DataOutputStream dos = new DataOutputStream(bos);

		byte[] padding = new byte[size - 8];
		Random random = new Random(System.nanoTime());
		random.nextBytes(padding);

		try {
			dos.writeLong(System.currentTimeMillis());
			dos.write(padding);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bos.toByteArray();
	}
}
