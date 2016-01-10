package com.exnoke.wakelock;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.service.notification.*;

public class MainListener extends NotificationListenerService
{
	private boolean posted;
	private Notification nig, day;
	private NotificationManager nm;

	@Override
	public void onCreate()
	{
		super.onCreate();
		posted = false;

		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		nig = new Notification.Builder(this)
			.setContentTitle("Clear Mode")
			.setContentText("Some sounds are stopped until 6:00")
			.setSmallIcon(R.drawable.cust_transparent)
			.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.not_night_big))
			.setOngoing(true)
			.setShowWhen(false)
			.setPriority(1)
			.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
			.build();

		nig.icon = R.drawable.not_night;

		day = new Notification.Builder(this)
			.setContentTitle("Clear Mode")
			.setContentText("Some sounds are stopped until 17:00")
			.setSmallIcon(R.drawable.cust_transparent)
			.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.not_day_big))
			.setOngoing(true)
			.setShowWhen(false)
			.setPriority(1)
			.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
			.build();

		day.icon = R.drawable.not_day;

		V.set(this, "listener", true);
	}

	@Override
	public void onDestroy()
	{
		if (V.Lollipop())
		{
			V.set(this, "alarm", false);
		}
		V.set(this, "pkg", false);
		V.set(this, "listener", false);
		super.onDestroy();
	}

	@Override
	public void onNotificationPosted(StatusBarNotification sbn)
	{
		setPkg(sbn, true);
		super.onNotificationPosted(sbn);
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn)
	{
		setPkg(sbn, false);
		super.onNotificationRemoved(sbn);
	}

	private void setPkg(StatusBarNotification sbn , boolean val)
	{
		Bundle bd = sbn.getNotification().extras;
		String text = bd.getString(Notification.EXTRA_TEXT);
		//String title = bd.getString(Notification.EXTRA_TITLE);
		String[] notifs = getResources().getStringArray(R.array.notifications);

		if (text.contains("call"))
		{
			for (String pkg:notifs)
			{
				if (sbn.toString().contains(pkg))
				{
					V.set(this, "pkg", val);
				}
			}
		}

		if (val & sbn.toString().contains("taskerm"))
		{
			boolean night = text.contains("Night Mode");
			if (night || text.contains("Day Dream"))
			{
				if (!posted)
				{
					nm.notify("night", 10, night ? nig: day);
					posted = true;
				}
			}
			else if (!text.contains("detected"))
			{
				posted = false;
				nm.cancel("night", 10);
			}
		}

		/*if (val & (
		 (sbn.toString().contains("avast") & !title.contains("Scanner") & !text.contains("problem") & !title.contains("Security") & !title.contains("Privacy"))
		 | (sbn.toString().contains("criminalcase")) & title.isEmpty()))
		 {
		 cancelNotification(sbn.getPackageName(), sbn.getTag(), sbn.getId());
		 }*/

		if (val & text.contains("DD-WRT"))
		{
			Intent restore = new Intent(getPackageName() + ".VPN_CONNECTED");
			restore.setPackage("com.exnoke.all");
			sendBroadcast(restore);
		}
	}
}
