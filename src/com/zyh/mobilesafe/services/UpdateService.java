package com.zyh.mobilesafe.services;

import java.io.File;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.zyh.mobilesafe.R;

public class UpdateService extends Service {

	private NotificationManager notificationManager;
	private Notification notification;
	private File updateFile;
	private RemoteViews remoteViews;// 状态栏通知显示的view

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		String apkurl = intent.getStringExtra("apkurl");
		// 创建通知栏
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notification = new Notification();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		// 设置通知栏显示内容
		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = "开始下载";
		// 通知栏视图
		remoteViews = new RemoteViews(getPackageName(), R.layout.notification_update);
		remoteViews.setImageViewResource(R.id.iv_update_icon, R.drawable.ic_launcher);
		notification.contentView = remoteViews;
		// 发出通知
		notificationManager.notify(0, notification);

		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			updateFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/update.apk");

			FinalHttp finalHttp = new FinalHttp();
			finalHttp.download(apkurl, updateFile.getAbsolutePath(), new AjaxCallBack<File>() {
				@Override
				public void onFailure(Throwable t, int errorNo, String strMsg) {
					// 下载失败
					remoteViews.setTextViewText(R.id.tv_update_text, "下载失败");
					remoteViews.setProgressBar(R.id.pb_update_progress, 100, 30, false);
					notification.contentView = remoteViews;
					notificationManager.notify(0, notification);
					// 停止服务
					stopSelf();
					super.onFailure(t, errorNo, strMsg);
				}

				@Override
				public void onLoading(long count, long current) {
					int progress = (int) (current * 100 / count);
					remoteViews.setTextViewText(R.id.tv_update_text, "已下载" + progress + "%");
					remoteViews.setProgressBar(R.id.pb_update_progress, 100, (int) progress, false);
					notification.contentView = remoteViews;
					notificationManager.notify(0, notification);
					super.onLoading(count, current);
				}

				@Override
				public void onSuccess(File t) {

					// 点击安装PendingIntent
					Uri uri = Uri.fromFile(t);
					Intent installIntent = new Intent(Intent.ACTION_VIEW);
					installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
					PendingIntent updatePendingIntent = PendingIntent.getActivity(UpdateService.this, 0, installIntent, 0);

					notification.defaults = Notification.DEFAULT_SOUND;// 铃声提醒
					notification
							.setLatestEventInfo(getApplicationContext(), getString(R.string.app_name), "下载完成，点击安装", updatePendingIntent);
					notificationManager.notify(0, notification);

					// 停止服务
					stopSelf();
					super.onSuccess(t);
				}
			});
		} else {
			remoteViews.setTextViewText(R.id.tv_update_text, "下载失败，sdcard不存在");
			remoteViews.setProgressBar(R.id.pb_update_progress, 100, 0, false);
			notification.contentView = remoteViews;
			notificationManager.notify(0, notification);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
