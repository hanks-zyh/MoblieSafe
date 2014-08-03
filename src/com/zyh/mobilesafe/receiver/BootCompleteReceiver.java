package com.zyh.mobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

public class BootCompleteReceiver extends BroadcastReceiver {

	private SharedPreferences sp;
	private TelephonyManager tm;

	@Override
	public void onReceive(Context context, Intent intent) {
		// 读取之前保存的sim
		sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String saveSim = sp.getString("sim", "");
		// 当前的sim卡信息
		String realSim = tm.getSimSerialNumber();
		// 比较
		if (saveSim.equals(realSim)) {
			// sim卡没有变更

		} else {
			// sim卡已经便更，发一个短信给安全号码
			String safenumber = sp.getString("safenumber", "");
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(safenumber, null, "sim card change!", null, null);

		}

	}
}
