package com.zyh.mobilesafe;

import android.content.Intent;
import android.os.Bundle;

public class Setup1Activity extends BaseSetupActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup1);
	}

	@Override
	public void showNext() {
		startActivity(new Intent(this, Setup2Activity.class));
		this.finish();
		overridePendingTransition(R.anim.base_slide_right_in, R.anim.base_slide_left_out);

	}

	@Override
	public void showPre() {
	}
}
