package com.exnoke.wakelock;

import android.app.*;
import android.content.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.telephony.*;
import android.widget.*;
import java.util.*;

public class MainService extends Service
{
	private AlarmManager am;
	private long test;
	//private String msg;
	private PendingIntent aInt;

	@Override
	public IBinder onBind(Intent p1)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		BroadcastReceiver mReceiver = new MainReceiver();
		registerReceiver(mReceiver, filter);

		test = -1;
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
		boolean isScreenOn = V.isScreenOn(pm);

		try
		{
			isScreenOn = intent.getBooleanExtra("screen", pm.isScreenOn());
		}
		catch (Exception e)
		{}

		if (isScreenOn)
		{
			try
			{
				am.cancel(aInt);
			}
			catch (Exception e)
			{}

			if (test != -1)Toast.makeText(this, ((Long)test).toString(), Toast.LENGTH_LONG).show();
			//Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

			return START_STICKY;

		}
		else
		{
			test = -2;
			// break code due to unwanted conditions


			if (V.getPower(this))
			{
				return START_STICKY;
			}

			AudioManager aud = (AudioManager)getSystemService(AUDIO_SERVICE);
			if (aud.isMusicActive())
			{return START_STICKY;}

			TelephonyManager tl = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
			Boolean inCall = ((Integer)tl.getCallState() > 0);
			if (inCall)
			{return START_STICKY;}

			if (getCurrents())
			{
				waitFiveMins(0);
				test = -3;
				return START_STICKY;
			}

			ContentResolver cr = getContentResolver();
			Boolean isPolicy = (Integer.parseInt(Settings.Global.getString(cr, Settings.Global.WIFI_SLEEP_POLICY)) == 2);
			Boolean isWifiOn = (Integer.parseInt(Settings.Global.getString(cr, Settings.Global.WIFI_ON)) == 1);

			if (isPolicy && isWifiOn)
			{return START_STICKY;}


			int waitFor = 0;
			long offTime = -1;
			try
			{
				waitFor = intent.getIntExtra("waitFor", 0);
				offTime = intent.getLongExtra("offTime", -1);
			}
			catch (Exception e)
			{}

			if (waitFor == 2)
			{
				if (offTime > -1)
				{
					if (wakelockDetected(offTime) && V.get(this, "alarm", R.bool.alarm))
					{
						setWakelockAlarm();
					}
				}
				else
				{
					notifyError(waitFor);
				}
			}

			else if (waitFor == 1)
			{
				if (offTime > -1)
				{
					if (wakelockDetected(offTime) && V.get(this, "alarm", R.bool.alarm))
					{
						waitFiveMins(2);
						notifyWakelock();
					}
				}
				else
				{
					notifyError(waitFor);
				}
			}
			else if (waitFor == 0)
			{
				waitFiveMins(1);
				test = -1;
			}

			else
			{
				notifyError(waitFor);
			}
		}
		return START_STICKY;
	}

	private void notifyError(Integer iError)
	{
		iError += 100;
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "exnoke@gmail.com", null));
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Wakelock Error: " + iError.toString());

		PendingIntent notifyPIntent = PendingIntent.getActivity(this, 2, Intent.createChooser(emailIntent, getString(R.string.mail_chooser)), PendingIntent.FLAG_UPDATE_CURRENT);

		Notification noti = new Notification.Builder(this)
			.setContentTitle(getString(R.string.not_title))
			.setContentText(getString(R.string.not_text_code) + iError.toString() + getString(R.string.not_text_sugg))
			.setSmallIcon(R.drawable.ic_dialog_alert)
			.setPriority(Notification.PRIORITY_MIN)
			.setContentIntent(notifyPIntent)
			.setAutoCancel(true)
			.setDefaults(Notification.DEFAULT_ALL)
			.build();

		NotificationManager note = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		note.notify("error", 0, noti);
	}

	private void notifyWakelock()
	{
		PendingIntent wakePIntent;
		CharSequence wakeInfo;

		Intent wakeIntent = new Intent(this, AlertActivity.class);
		wakeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		wakePIntent = PendingIntent.getActivity(this, 1, wakeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		wakeInfo = V.get(this, "ops", R.bool.ops) ?getString(R.string.wake_ops): getString(R.string.wake_battery);

		Notification noti = new Notification.Builder(this)
			.setContentTitle("Wakelock")
			.setContentText(wakeInfo)
			.setSmallIcon(R.drawable.ic_dialog_alert)
			.setContentIntent(wakePIntent)
			.setDefaults(V.get(this, "not", true) ?Notification.DEFAULT_ALL: (Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS))
			.setAutoCancel(true)
			.build();

		NotificationManager note = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		note.notify("alert", 1, noti);
	}

	private void waitFiveMins(int waitStatus)
	{
		am = (AlarmManager)getSystemService(ALARM_SERVICE);
		Intent alarmIntent = new Intent(this, MainService.class);
		alarmIntent.putExtra("offTime", SystemClock.uptimeMillis());
		alarmIntent.putExtra("waitFor", waitStatus);
		aInt = PendingIntent.getService(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 300000, aInt);
	}

	private void setWakelockAlarm()
	{
		Calendar now = Calendar.getInstance();
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minute = now.get(Calendar.MINUTE);

		minute += 2;
		if (minute > 59)
		{
			minute %= 60;
			hour ++;
		}
		hour %= 24;

		Intent alInt = new Intent(AlarmClock.ACTION_SET_ALARM);
		alInt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		alInt.putExtra(AlarmClock.EXTRA_HOUR, hour);
		alInt.putExtra(AlarmClock.EXTRA_MINUTES, minute);
		alInt.putExtra(AlarmClock.EXTRA_MESSAGE, getString(R.string.alarm_extra_msg));
		alInt.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
		startActivity(alInt);
	}

	private boolean wakelockDetected(long diff)
	{
		test = (SystemClock.uptimeMillis() - diff) / 1000;
		return test > 200;
	}

	private boolean getCurrents()
	{
		ActivityManager acm =(ActivityManager)getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> rs = acm.getRunningServices(Integer.MAX_VALUE);
		String rsl = "";

		for (int i=0; i < rs.size(); i++)
		{
			ActivityManager.RunningServiceInfo rsi = rs.get(i);
			rsl = rsl + " " + rsi.service.getClassName() ;
		}

		String[] services = getResources().getStringArray(R.array.services);

		for (String srv: services)
		{
			if (rsl.contains(srv))
			{return true;}
		}

		String ra = V.getTaskInfo(this);
		String[] tasks = getResources().getStringArray(R.array.tasks);

		for (String act:tasks)
		{
			if (ra.contains(act))
			{return true;}
		}

		return V.get(this, "listener", false)?V.get(this, "pkg" , false):false;
	}

}
