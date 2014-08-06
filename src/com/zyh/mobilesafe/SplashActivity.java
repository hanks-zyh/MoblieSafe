package com.zyh.mobilesafe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.zyh.mobilesafe.services.UpdateService;
import com.zyh.mobilesafe.utils.StreamTool;

public class SplashActivity extends Activity {

	private String description, apkurl;
	private TextView tv_splash_version;
	protected static final int ENTER_HOME = 0;
	protected static final int SHOW_UPDATE_DIALOG = 1;
	protected static final int URL_ERROR = 2;
	protected static final int NETWORK_ERROR = 3;
	protected static final int JSON_ERROR = 4;
	private SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		tv_splash_version = (TextView) findViewById(R.id.tv_splash_version);
		tv_splash_version.setText("版本号：" + getAppVersion());

		// 拷贝数据库
		copyDB();

		sp = getSharedPreferences("config", MODE_PRIVATE);
		boolean autoUpdate = sp.getBoolean("autoUpdate", true);
		if (autoUpdate) {
			// 检查版本
			checkVersion();
		} else {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					// 进入主界面
					enterHome();
				}
			}, 2000);
		}
		// 进入动画
		AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(500);
		findViewById(R.id.ll_splash_root).startAnimation(animation);

	}

	/**
	 * 把address.db这个数据库拷贝到 data/data/com.zyh.mobilesafe/files/address.db
	 */
	private void copyDB() {

		// 已经拷贝了，不再拷贝
		try {
			File file = new File(getFilesDir(), "address.db");
			if (file.exists() && file.length() > 0) return; // 正常，不再拷贝

			InputStream is = getAssets().open("address.db");
			FileOutputStream fos = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = is.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			fos.close();
			is.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case ENTER_HOME:// 进入主界面
					enterHome();
					break;
				case SHOW_UPDATE_DIALOG:// 显示升级对话框
					showUpdateDialog();
					break;
				case URL_ERROR:// URL错误
					enterHome();
					Toast.makeText(SplashActivity.this, "URL错误", Toast.LENGTH_SHORT).show();
					break;
				case NETWORK_ERROR:// 网络错误
					enterHome();
					Toast.makeText(SplashActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
					break;
				case JSON_ERROR:// 网络错误
					enterHome();
					Toast.makeText(SplashActivity.this, "Json错误", Toast.LENGTH_SHORT).show();
					break;
			}
		}
	};

	private void showUpdateDialog() {
		AlertDialog.Builder builder = new Builder(this);
		// builder.setCancelable(false);//不让点击返回
		builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				enterHome();
			}
		});
		builder.setTitle("版本升级");
		builder.setMessage(description);
		builder.setNegativeButton("立刻升级", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				enterHome();
				// 开启通知栏下载服务
				Intent intent = new Intent(SplashActivity.this, UpdateService.class);
				intent.putExtra("apkurl", apkurl);
				startService(intent);

			}
		});
		builder.setPositiveButton("取消", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				enterHome();
			}
		});
		builder.show();
	}

	/**
	 * 进入主页
	 */
	private void enterHome() {
		startActivity(new Intent(this, HomeActivity.class));
		this.finish();
	};

	/**
	 * 检查新版本
	 */
	private void checkVersion() {
		new Thread() {
			public void run() {
				Message msg = Message.obtain();

				// 开始时间
				long startTime = System.currentTimeMillis();
				try {
					URL url = new URL(getString(R.string.serviceurl));
					HttpURLConnection conn;
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(3000);
					if (conn.getResponseCode() == 200) {
						InputStream is = conn.getInputStream();
						String result = StreamTool.readFromStream(is);
						JSONObject json;
						json = new JSONObject(result);
						// 服务器的版本
						String version = json.getString("version");
						description = json.getString("description");
						apkurl = json.getString("apkurl");

						if (version.equals(getAppVersion())) {
							// 版本一致，进入界面
							msg.what = ENTER_HOME;
						} else {
							// 升级，弹出
							msg.what = SHOW_UPDATE_DIALOG;
						}

					}
				} catch (MalformedURLException e) {
					msg.what = URL_ERROR;
					e.printStackTrace();
				} catch (IOException e) {
					msg.what = NETWORK_ERROR;
					e.printStackTrace();
				} catch (JSONException e) {
					msg.what = JSON_ERROR;
					e.printStackTrace();
				} finally {

					long endTime = System.currentTimeMillis();
					long dtime = endTime - startTime;
					if (dtime < 2000) {
						try {
							Thread.sleep(2000 - dtime);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					handler.sendMessage(msg);
				}

			};
		}.start();
	}

	/**
	 * 得到应用的版本号
	 */
	public String getAppVersion() {
		PackageManager pm = getPackageManager();
		try {
			// 功能清单文件
			PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
			return info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			// 不会发生
			return "";
		}
	}

}
