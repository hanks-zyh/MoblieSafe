package com.zyh.mobilesafe;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class LostFindActivity extends Activity {

	private SharedPreferences sp;
	private TextView tv_safenumber;
	private ImageView iv_protecting;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		// 判断是否做过过设置向导
		boolean configed = sp.getBoolean("configed", false);
		if (configed) {
			// 留在手机防盗界面
			setContentView(R.layout.activity_lostfind);
			tv_safenumber = (TextView) findViewById(R.id.tv_safenumber);
			iv_protecting = (ImageView) findViewById(R.id.iv_protecting);
			// 得到安全号码
			String safenumber = sp.getString("safenumber", "");
			tv_safenumber.setText(safenumber);
			boolean protecting = sp.getBoolean("protecting", false);
			if (protecting) {
				// 已经开启防盗
				iv_protecting.setImageResource(R.drawable.lock);
			} else {
				// 没有开启防盗
				iv_protecting.setImageResource(R.drawable.unlock);
			}

		} else {
			// 还没有设置向导
			startActivity(new Intent(this, Setup1Activity.class));
			this.finish();
		}

	}

	/**
	 * 再次进入设置向导
	 * 
	 * @param view
	 */
	public void reEnterSetup(View view) {
		startActivity(new Intent(this, Setup1Activity.class));
		this.finish();
	}
}
