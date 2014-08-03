package com.zyh.mobilesafe.receiver;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

import com.zyh.mobilesafe.R;
import com.zyh.mobilesafe.services.GPSService;

public class SMSReceiver extends BroadcastReceiver {

	private static final String tag = "SMSReceiver";
	// 设备管理器
	private DevicePolicyManager dpm;

	@Override
	public void onReceive(Context context, Intent intent) {

		SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		String safenumber = sp.getString("safenumber", "");
		dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);

		// 接收短信
		Object[] objs = (Object[]) intent.getExtras().get("pdus");
		for (Object b : objs) {
			// 具体的某一条短信
			SmsMessage sms = SmsMessage.createFromPdu((byte[]) b);
			// 发送者
			String sender = sms.getOriginatingAddress();
			String body = sms.getMessageBody();

			Log.v(tag, sender);
			Log.v(tag, body);
			if (sender.contains(safenumber) || safenumber.contains(sender)) {
				if (body.contains("#*location*#")) {
					abortBroadcast();
					Log.i(tag, "得到手机GPS");
					context.startService(new Intent(context, GPSService.class));
					SharedPreferences sp2 = context.getSharedPreferences("config", Context.MODE_PRIVATE);
					String lastlocation = sp2.getString("lastlocation", null);
					if (TextUtils.isEmpty(lastlocation)) {
						SmsManager.getDefault().sendTextMessage(sender, null, "getting lastlocation....", null, null);
					} else {
						System.out.println(lastlocation);
						SmsManager.getDefault().sendTextMessage(sender, null, lastlocation, null, null);
					}
				} else if (body.contains("#*alarm*#")) {
					abortBroadcast();
					Log.i(tag, "播放报警音乐");
					MediaPlayer player = MediaPlayer.create(context, R.raw.alarm);
					player.setVolume(1.0f, 1.0f);// 音量
					player.setLooping(true);// 循环播放
					player.start();
				} else if (body.contains("#*wipedata*#")) {
					abortBroadcast();
					Log.i(tag, "远程清除数据");
					// 清除sdcard数据
					dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
					// 恢复出厂设置
					dpm.wipeData(0);
				} else if (body.contains("#*lockscreen*#")) {
					abortBroadcast();
					Log.i(tag, "远程锁屏");
					dpm.lockNow();
					// dpm.resetPassword("", 0);// 设置锁屏密码
				}
			}
		}
	}

}
