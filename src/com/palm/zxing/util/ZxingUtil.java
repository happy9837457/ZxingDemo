package com.palm.zxing.util;

import java.util.Hashtable;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

/**
 * zxing二维码工具类 data/core.jar
 * 
 * @author weixiang.qin
 * 
 */
public class ZxingUtil {
	public static String TAG = "QrcodeUtil";
	public static final float WIDTH_RATE = 0.8f;// 屏幕宽度比例
	public static final String CHARSET = "UTF-8";// 编码

	/**
	 * 生成二维码
	 * 
	 * @param content
	 * @return
	 * @throws WriterException
	 */
	public static Bitmap createQrcode(Activity mActivity, String content) {
		int screenWidth = getScreenWidth(mActivity);
		int imageWidth = (int) ((int) screenWidth * WIDTH_RATE);
		int imageHeight = imageWidth;
		try {
			Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
			hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
			hints.put(EncodeHintType.MARGIN, 1);// 边框宽度
			BitMatrix matrix = new MultiFormatWriter().encode(content,
					BarcodeFormat.QR_CODE, imageWidth, imageHeight, hints);
			int width = matrix.getWidth();
			int height = matrix.getHeight();
			int[] pixels = new int[width * height];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (matrix.get(x, y)) {
						pixels[y * width + x] = 0xff000000;
					}
				}
			}
			Bitmap bitmap = Bitmap.createBitmap(width, height,
					Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			return bitmap;
		} catch (Exception ex) {
			Log.d(TAG, ex.toString());
		}
		return null;
	}

	/**
	 * 获取屏幕宽度
	 * 
	 * @param ctx
	 * @return
	 */
	public static int getScreenWidth(Context mContext) {
		return mContext.getResources().getDisplayMetrics().widthPixels;
	}
}
