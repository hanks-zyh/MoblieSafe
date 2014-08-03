package com.zyh.mobilesafe;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Setup3Activity extends BaseSetupActivity {

	private EditText et_setup3_phone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup3);
		et_setup3_phone = (EditText) findViewById(R.id.et_setup3_phone);
		et_setup3_phone.setText(sp.getString("safenumber", ""));
	}

	@Override
	public void showNext() {
		String phone = et_setup3_phone.getText().toString().trim();
		if (TextUtils.isEmpty(phone)) {
			Toast.makeText(this, "安全号码还没有设置", Toast.LENGTH_SHORT).show();
			return;
		}
		// 保存安全号码
		Editor editor = sp.edit();
		editor.putString("safenumber", phone);
		editor.commit();

		startActivity(new Intent(this, Setup4Activity.class));
		this.finish();
		overridePendingTransition(R.anim.base_slide_right_in, R.anim.base_slide_left_out);
	}

	@Override
	public void showPre() {
		startActivity(new Intent(this, Setup2Activity.class));
		this.finish();
		overridePendingTransition(R.anim.base_slide_left_in, R.anim.base_slide_right_out);
	}

	/**
	 * 选择联系人的点击事件
	 * 
	 * @param view
	 */
	public void selectContact(View view) {
		Intent intent = new Intent(this, SelectContactActivity.class);
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data == null) return;

		String phone = data.getStringExtra("phone").replace("-", "").replace(" ", "");
		et_setup3_phone.setText(phone);
	}
}
