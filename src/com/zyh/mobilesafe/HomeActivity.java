package com.zyh.mobilesafe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zyh.mobilesafe.utils.MD5Utils;

public class HomeActivity extends Activity {

	private GridView gridView;
	private SharedPreferences sp;
	private static final String[] names = { "手机防盗", "通讯卫士", "软件管理", "进程管理", "流量统计", "手机杀毒", "缓存清理", "高级工具", "设置中心" };
	private static final int[] ids = { R.drawable.safe, R.drawable.callmsgsafe, R.drawable.app, R.drawable.taskmanager,
			R.drawable.netmanager, R.drawable.trojan, R.drawable.sysoptimize, R.drawable.atools, R.drawable.settings };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		sp = getSharedPreferences("config", MODE_PRIVATE);

		gridView = (GridView) findViewById(R.id.gv_home);
		gridView.setAdapter(new Myadpater());
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
					case 0:// 进入手机防盗
						showLostDialog();
						break;
					case 1:// 进入通讯卫士
						startActivity(new Intent(HomeActivity.this, CallSmsSafeActivity.class));
						break;
					case 2:// 进入应用管理
						startActivity(new Intent(HomeActivity.this, AppManagerActivity.class));
						break;
					case 3:// 进入进程管理
						startActivity(new Intent(HomeActivity.this, TaskManagerActivity.class));
						break;
					case 7:// 进入高级工具
						startActivity(new Intent(HomeActivity.this, AtoolsActivity.class));
						break;
					case 8:// 进入设置中心
						startActivity(new Intent(HomeActivity.this, SettingActivity.class));
						break;

					default:
						break;
				}
			}
		});
	}

	protected void showLostDialog() {
		// 判断是否设置过密码
		if (isSetupPwd()) {
			// 已经设置过密码，弹出输入对话框
			showEnterDialog();
		} else {
			// 没有设置过密码，弹出设置密码对话框
			showSetupPwdDialog();
		}
	}

	private EditText et_setup_pwd;
	private EditText et_setup_confirm;
	private Button ok;
	private Button cancel;
	private AlertDialog dialog;

	/**
	 * 设置密码对话框
	 */
	private void showSetupPwdDialog() {
		AlertDialog.Builder builder = new Builder(this);
		View view = View.inflate(this, R.layout.dialog_setup_password, null);
		et_setup_pwd = (EditText) view.findViewById(R.id.et_setup_pwd);
		et_setup_confirm = (EditText) view.findViewById(R.id.et_setup_confirm);
		ok = (Button) view.findViewById(R.id.bt_ok);
		cancel = (Button) view.findViewById(R.id.bt_cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 取消对话框
				dialog.dismiss();
			}
		});
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 取出密码
				String password = et_setup_pwd.getText().toString().trim();
				String password_confirm = et_setup_confirm.getText().toString().trim();
				if (TextUtils.isEmpty(password) || TextUtils.isEmpty(password_confirm)) {
					Toast.makeText(HomeActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				// 判断是否一致
				if (password.equals(password_confirm)) {
					// 一致，取消对话框，保存密码，进入手机防盗界面
					Editor editor = sp.edit();
					editor.putString("password", MD5Utils.getMd5String(password));
					editor.commit();
					dialog.dismiss();

					startActivity(new Intent(HomeActivity.this, LostFindActivity.class));
				} else {
					et_setup_confirm.setText("");
					Toast.makeText(HomeActivity.this, "两次密码不一致", Toast.LENGTH_SHORT).show();
					return;
				}
			}
		});

		dialog = builder.create();
		dialog.setView(view, 0, 0, 0, 0);
		dialog.show();
	}

	/**
	 * 输入密码对话框
	 */
	private void showEnterDialog() {
		AlertDialog.Builder builder = new Builder(this);
		View view = View.inflate(this, R.layout.dialog_enter_password, null);
		et_setup_pwd = (EditText) view.findViewById(R.id.et_setup_pwd);
		et_setup_confirm = (EditText) view.findViewById(R.id.et_setup_confirm);
		ok = (Button) view.findViewById(R.id.bt_ok);
		cancel = (Button) view.findViewById(R.id.bt_cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 取消对话框
				dialog.dismiss();
			}
		});
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 取出密码
				String password = et_setup_pwd.getText().toString().trim();
				if (TextUtils.isEmpty(password)) {
					Toast.makeText(HomeActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				String savepassword = sp.getString("password", "");
				// 判断是否一致
				if (savepassword.equals(MD5Utils.getMd5String(password))) {
					// 一致，取消对话框 ，进入手机防盗界面
					dialog.dismiss();
					startActivity(new Intent(HomeActivity.this, LostFindActivity.class));
				} else {
					et_setup_pwd.setText("");
					Toast.makeText(HomeActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
					return;
				}
			}
		});
		dialog = builder.create();
		dialog.setView(view, 0, 0, 0, 0);
		dialog.show();
	}

	/**
	 * 判断是否设置过密码
	 * 
	 * @return
	 */
	protected boolean isSetupPwd() {
		String password = sp.getString("password", null);
		return !TextUtils.isEmpty(password);
	}

	/**
	 * GridView 的适配器
	 * 
	 * @author zyh
	 * 
	 */
	class Myadpater extends BaseAdapter {

		@Override
		public int getCount() {
			return names.length;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = View.inflate(HomeActivity.this, R.layout.list_item_home, null);
			ImageView imageView = (ImageView) view.findViewById(R.id.iv_home_item);
			TextView textView = (TextView) view.findViewById(R.id.tv_home_item);
			textView.setText(names[position]);
			imageView.setImageResource(ids[position]);
			return view;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

	}
}
