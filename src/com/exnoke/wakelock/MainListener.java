package com.exnoke.wakelock;

import android.app.*;
import android.os.*;
import android.service.notification.*;

public class MainListener extends NotificationListenerService
{

	@Override
	public void onCreate()
	{
		super.onCreate();
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
		String[] notifs = getResources().getStringArray(R.array.notifications);

		if (text.contains("call"))
		{
			for (String pkg:notifs)
			{
				if (sbn.toString().contains(pkg))
				{
					V.set(this, "pkg", false);
				}
			}
		}
	}
}
