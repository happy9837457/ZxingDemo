package com.palm.zxing.zxing.decoding;

import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.palm.zxing.activity.CaptureActivity;
import com.palm.zxing.zxing.camera.CameraManager;
import com.palm.zxing.zxing.view.ViewfinderResultPointCallback;

/**
 * This class handles all the messaging which comprises the state machine for
 * capture.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureActivityHandler extends Handler {
	private static final String TAG = CaptureActivityHandler.class
			.getSimpleName();
	public static final int id_auto_focus = 1;
	public static final int id_restart_preview = 2;
	public static final int id_decode_succeeded = 3;
	public static final int id_decode_failed = 4;
	public static final int id_return_scan_result = 5;
	public static final int id_decode = 6;
	public static final int id_launch_product_query = 7;
	public static final int id_quit = 8;
	private final CaptureActivity activity;
	private final DecodeThread decodeThread;
	private State state;

	private enum State {
		PREVIEW, SUCCESS, DONE
	}

	public CaptureActivityHandler(CaptureActivity activity,
			Vector<BarcodeFormat> decodeFormats, String characterSet) {
		this.activity = activity;
		decodeThread = new DecodeThread(activity, decodeFormats, characterSet,
				new ViewfinderResultPointCallback(activity.getViewfinderView()));
		decodeThread.start();
		state = State.SUCCESS;
		CameraManager.get().startPreview();
		restartPreviewAndDecode();
	}

	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
		case id_auto_focus:
			// Log.d(TAG, "Got auto-focus message");
			// When one auto focus pass finishes, start another. This is the
			// closest thing to
			// continuous AF. It does seem to hunt a bit, but I'm not sure what
			// else to do.
			if (state == State.PREVIEW) {
				CameraManager.get().requestAutoFocus(this, id_auto_focus);
			}
			break;
		case id_restart_preview:
			Log.d(TAG, "Got restart preview message");
			restartPreviewAndDecode();
			break;
		case id_decode_succeeded:
			Log.d(TAG, "Got decode succeeded message");
			state = State.SUCCESS;
			Bundle bundle = message.getData();
			Bitmap barcode = bundle == null ? null : (Bitmap) bundle
					.getParcelable(DecodeThread.BARCODE_BITMAP);
			activity.handleDecode((Result) message.obj, barcode);
			break;
		case id_decode_failed:
			// We're decoding as fast as possible, so when one decode fails,
			// start another.
			state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
					id_decode);
			break;
		case id_return_scan_result:
			Log.d(TAG, "Got return scan result message");
			activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
			activity.finish();
			break;
		case id_launch_product_query:
			Log.d(TAG, "Got product query message");
			String url = (String) message.obj;
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			activity.startActivity(intent);
			break;
		}
	}

	public void quitSynchronously() {
		state = State.DONE;
		CameraManager.get().stopPreview();
		Message quit = Message.obtain(decodeThread.getHandler(), id_quit);
		quit.sendToTarget();
		try {
			decodeThread.join();
		} catch (InterruptedException e) {
			// continue
		}

		// Be absolutely sure we don't send any queued up messages
		removeMessages(id_decode_succeeded);
		removeMessages(id_decode_failed);
	}

	private void restartPreviewAndDecode() {
		if (state == State.SUCCESS) {
			state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
					id_decode);
			CameraManager.get().requestAutoFocus(this, id_auto_focus);
			activity.drawViewfinder();
		}
	}

}
