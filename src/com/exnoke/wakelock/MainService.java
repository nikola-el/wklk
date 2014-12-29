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
	public AlarmManager am;
	public long test;
	public String msg;
	public PendingIntent aInt;

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
		if (pm.isScreenOn())
		{
			try
			{
				am.cancel(aInt);
			}
			catch (Exception e)
			{}

			Toast.makeText(this, ((Long)test).toString(), Toast.LENGTH_LONG).show();
			//Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

			return START_STICKY;

		}
		else
		{
			test = -2;
			// break code due to unwanted conditions

			Intent p2 = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
			Integer status = p2.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
			if (status > 0)
			{
				SharedPreferences.Editor sharedPref = getSharedPreferences(getString(R.string.settings), Context.MODE_PRIVATE).edit();
				sharedPref.putBoolean("power", true);
				sharedPref.commit();
				stopSelf();
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
			{
				notifyError(8);
			}

			if (waitFor == 2)
			{
				if (offTime > -1)
				{
					if (wakelockDetected(offTime) && getAlarm())
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
					if (wakelockDetected(offTime) && getAlarm())
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

	public void notifyError(Integer iError)
	{
		iError += 100;
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "exnoke@gmail.com", null));
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Wakelock Error: " + iError.toString());

		PendingIntent notifyPIntent = PendingIntent.getActivity(this, 2, Intent.createChooser(emailIntent, "Send email..."), PendingIntent.FLAG_UPDATE_CURRENT);

		Notification noti = new Notification.Builder(this)
			.setContentTitle("Error")
			.setContentText("Code: " + iError.toString() + ". Press to send e-mail to developer.")
			.setSmallIcon(R.drawable.ic_dialog_alert)
			.setPriority(Notification.PRIORITY_MIN)
			.setContentIntent(notifyPIntent)
			.setAutoCancel(true)
			.setDefaults(Notification.DEFAULT_ALL)
			.build();

		NotificationManager note = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		note.notify("error", 0, noti);
	}

	public void notifyWakelock()
	{
		PendingIntent wakePIntent;
		CharSequence wakeInfo;

		if (Build.VERSION.SDK_INT >= 18)
		{
			Intent wakeIntent = new Intent(this, AlertActivity.class);
			wakeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			wakePIntent = PendingIntent.getActivity(this, 1, wakeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			wakeInfo = "Press to open App Ops to find wakelock.";
		}
		else
		{
			wakePIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);
			wakeInfo = "Some wakelock is present.";
		}

		Notification noti = new Notification.Builder(this)
			.setContentTitle("Wakelock")
			.setContentText(wakeInfo)
			.setSmallIcon(R.drawable.ic_dialog_alert)
			.setContentIntent(wakePIntent)
			.setDefaults(Notification.DEFAULT_ALL)
			.setAutoCancel(true)
			.build();

		NotificationManager note = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		note.notify("alert", 1, noti);
	}

	public void waitFiveMins(int waitStatus)
	{
		am = (AlarmManager)getSystemService(ALARM_SERVICE);
		Intent alarmIntent = new Intent(this, MainService.class);
		alarmIntent.putExtra("offTime", SystemClock.uptimeMillis());
		alarmIntent.putExtra("waitFor", waitStatus);
		aInt = PendingIntent.getService(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 300000, aInt);
	}

	public void setWakelockAlarm()
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
		alInt.putExtra(AlarmClock.EXTRA_MESSAGE, "Wakelock Alarm");
		alInt.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
		startActivity(alInt);
	}

	public boolean getAlarm()
	{
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.settings), Context.MODE_PRIVATE);
		return sharedPref.getBoolean("alarm", true);
	}

	public boolean wakelockDetected(long diff)
	{
		test = (SystemClock.uptimeMillis() - diff) / 1000;
		return test > 200;
	}

	public boolean getCurrents()
	{

		ActivityManager acm =(ActivityManager)getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> rs = acm.getRunningServices(Integer.MAX_VALUE);
		String rsl = "";

		for (int i=0; i < rs.size(); i++)
		{
			ActivityManager.RunningServiceInfo rsi = rs.get(i);
			rsl = rsl + " " + rsi.service.getClassName() ;
		}

		String[] services = 
		{
			"com.facebook.fbservice.service.DefaultBlueService",
			"com.google.android.picasasync.PicasaUploadService",
			"com.google.android.apps.docs.sync.syncadapter.ContentSyncService"
		};

		for (String srv: services)
		{
			if (rsl.contains(srv))
			{return true;}
		}

		List<ActivityManager.RunningTaskInfo> RunningTask = acm.getRunningTasks(1);
		ActivityManager.RunningTaskInfo ar = RunningTask.get(0);
		String ra = ar.topActivity.getClassName().toString();
		String[] tasks = 
		{
			"com.skype.android.app.calling.",
			"com.viber.voip.phone.",
			"com.google.android.apps.hangouts.hangout."
		};

		for (String act:tasks)
		{
			if (ra.contains(act))
			{return true;}
		}

		return false;
	}

}
