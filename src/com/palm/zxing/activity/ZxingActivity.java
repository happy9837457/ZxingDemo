package com.palm.zxing.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.palm.zxing.R;
import com.palm.zxing.util.Const;
import com.palm.zxing.util.ZxingUtil;

/**
 * zxing生成和扫描二维码功能
 * 
 * @author weixiang.qin
 * 
 */
public class ZxingActivity extends Activity implements OnClickListener {
	private Activity mActivity;
	private EditText contentEt;
	private Button createBtn;
	private Button scanBtn;
	private TextView resultTv;
	private ImageView resultIv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_zxing);
		mActivity = this;
		contentEt = (EditText) findViewById(R.id.content_et);
		createBtn = (Button) findViewById(R.id.create_btn);
		scanBtn = (Button) findViewById(R.id.scan_btn);
		resultTv = (TextView) findViewById(R.id.result_tv);
		resultIv = (ImageView) findViewById(R.id.result_iv);
		createBtn.setOnClickListener(this);
		scanBtn.setOnClickListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case Const.SCAN_REQ_CODE:
			if (resultCode == RESULT_OK) {
				String result = data.getStringExtra(Const.SCAN_RESULT);
				resultTv.setText(result);
			} else if (resultCode == RESULT_CANCELED) {
				resultTv.setText("扫描失败");
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == createBtn.getId()) {
			String content = contentEt.getText().toString();
			if ("".equals(content)) {
				return;
			}
			Bitmap bitmap = ZxingUtil.createQrcode(mActivity, content);
			if (bitmap != null) {
				resultIv.setImageBitmap(bitmap);
			}
		} else if (v.getId() == scanBtn.getId()) {
			Intent intent = new Intent(mActivity, CaptureActivity.class);
			startActivityForResult(intent, Const.SCAN_REQ_CODE);
		}
	}

}
