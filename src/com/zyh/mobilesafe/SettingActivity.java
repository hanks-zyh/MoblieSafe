package com.zyh.mobilesafe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.zyh.mobilesafe.services.AddressService;
import com.zyh.mobilesafe.services.CallSmsSafeService;
import com.zyh.mobilesafe.ui.SettingClickView;
import com.zyh.mobilesafe.ui.SettingItemView;
import com.zyh.mobilesafe.utils.ServiceUtils;

public class SettingActivity extends Activity {

	private SettingItemView siv_update;// 设置开启自动更新
	private SettingItemView siv_show_address;// 设置显示号码归属地
	private SettingItemView siv_callsms_safe;// 设置黑名单拦截
	private SettingClickView scv_address_style;// 设置归属地悬浮窗样式
	private Intent showAddress;
	private SharedPreferences sp;
	private Intent callSmsSafeIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		siv_update = (SettingItemView) findViewById(R.id.siv_update);
		siv_show_address = (SettingItemView) findViewById(R.id.siv_show_address);
		siv_callsms_safe = (SettingItemView) findViewById(R.id.siv_callsms_safe);
		scv_address_style = (SettingClickView) findViewById(R.id.scv_address_style);
		showAddress = new Intent(this, AddressService.class);
		boolean isRunningService = ServiceUtils.isServiceRunning(this, "com.zyh.mobilesafe.services.AddressService");
		if (isRunningService) {
			siv_show_address.setChecked(true);
		} else {
			siv_show_address.setChecked(false);
		}
		sp = getSharedPreferences("config", MODE_PRIVATE);
		boolean autoUpdate = sp.getBoolean("autoUpdate", true);
		if (autoUpdate) {
			// 自动升级开启
			siv_update.setChecked(true);
		} else {
			// 自动升级关闭
			siv_update.setChecked(false);
		}

		// 设置开启自动更新
		siv_update.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Editor editor = sp.edit();
				// 判断是否有选中
				// 已经打开自动升级了
				if (siv_update.isChecked()) {
					siv_update.setChecked(false);
					editor.putBoolean("autoUpdate", false);

				} else {
					// 没有打开自动升级
					siv_update.setChecked(true);
					editor.putBoolean("autoUpdate", true);
				}
				editor.commit();
			}
		});
		// 设置开启自动更新
		siv_show_address.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 判断是否有选中
				if (siv_show_address.isChecked()) {
					siv_show_address.setChecked(false);
					stopService(showAddress);
				} else {
					// 没有打开自动升级
					siv_show_address.setChecked(true);
					startService(showAddress);
				}
			}
		});

		// 归属地悬浮样式
		final String[] items = { "半透明", "活力橙", "卫士蓝", "金属灰", "苹果绿" };
		scv_address_style.setTitle("归属地提示框风格");
		final int which = sp.getInt("which", 0);
		scv_address_style.setDesc(items[which]);
		scv_address_style.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 谈一个对话框
				AlertDialog.Builder builder = new Builder(SettingActivity.this);
				builder.setTitle("归属地提示框风格");
				builder.setSingleChoiceItems(items, which, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 保存选择参数
						sp.edit().putInt("which", which).commit();
						scv_address_style.setDesc(items[which]);
						// 取消对话框
						dialog.dismiss();
					}
				});
				builder.setNegativeButton("取消", null);
				builder.show();
			}
		});
		// 设置黑名单拦截
		callSmsSafeIntent = new Intent(this, CallSmsSafeService.class);
		siv_callsms_safe.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (siv_callsms_safe.isChecked()) {
					siv_callsms_safe.setChecked(false);
					stopService(callSmsSafeIntent);
				} else {
					siv_callsms_safe.setChecked(true);
					startService(callSmsSafeIntent);
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		siv_show_address.setChecked(ServiceUtils.isServiceRunning(this, "com.zyh.mobilesafe.services.AddressService"));
		siv_callsms_safe.setChecked(ServiceUtils.isServiceRunning(this, "com.zyh.mobilesafe.services.CallSmsSafeService"));
	}
}
