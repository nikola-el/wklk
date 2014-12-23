package com.exnoke.wakelock;

import android.app.*;
import android.content.*;
import android.media.*;
import android.os.*;
import android.provider.*;
import android.telephony.*;
import android.widget.*;
import java.util.*;

public class MainService extends Service
{
	public Integer count;
	public Integer waitFor;
	public AlarmManager am;
	public Long offTime;
	public Boolean alarm;
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

		count = 0;
		waitFor = 0;
		alarm = false;
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
		Vibrator vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);
		AudioManager aud = (AudioManager)getSystemService(AUDIO_SERVICE);
		if (aud.isMusicActive())
		{return START_STICKY;}

		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = registerReceiver(null, ifilter);
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		Boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL);
		if (isCharging)
		{return START_STICKY;}

		TelephonyManager tl = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		Boolean inCall = ((Integer)tl.getCallState() > 0);
		if (inCall)
		{return START_STICKY;}

		ActivityManager acm =(ActivityManager)getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> rs = acm.getRunningServices(Integer.MAX_VALUE);
		String rsl = "";

        for (int i=0; i < rs.size(); i++)
		{
			ActivityManager.RunningServiceInfo rsi = rs.get(i);
			rsl = rsl + " " + rsi.service.getClassName() ;
		}

		if (rsl.contains("com.facebook.fbservice.service.DefaultBlueService"))
		{return START_STICKY;}

		ContentResolver cr = getContentResolver();
		Boolean isPolicy = (Integer.parseInt(Settings.Global.getString(cr, Settings.Global.WIFI_SLEEP_POLICY)) < 2);
		Boolean isWifiOff = (Integer.parseInt(Settings.Global.getString(cr, Settings.Global.WIFI_ON)) == 0);

		if (isPolicy || isWifiOff)
		{
			if (!pm.isScreenOn())
			{
				count++;

				if (waitFor == 0)
				{
					waitFor = 1;

					am = (AlarmManager)getSystemService(ALARM_SERVICE);
					Intent alarmIntent = new Intent(this, MainService.class);
					alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
					alarmIntent.putExtra("offTime", SystemClock.uptimeMillis());
					aInt = PendingIntent.getService(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
					am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 600000, aInt);

					vib.vibrate(200);
				}

				else
				{
					waitFor = 0;
					offTime = intent.getLongExtra("offTime", -1);
					if (offTime > -1)
					{
						offTime = (SystemClock.uptimeMillis() - offTime) / 1000;
						if (offTime > 550)
						{
							//notifyWakelock();
						}
					}
					else
					{
						//notifyError();
					}
					alarm = true;
				}
			}
			else
			{
				if (waitFor == 1)
				{
					waitFor = 0;
					am.cancel(aInt);
				}
				Toast.makeText(this, count.toString(), Toast.LENGTH_LONG).show();
				alarm = false;
			}
		}
		return START_STICKY;
	}

	public void notifyError()
	{
		PendingIntent notifyPIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);     
		Notification noti = new Notification.Builder(this)
			.setContentTitle("Error")
			.setContentText("Can't get system time.")
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentIntent(notifyPIntent)
			.setAutoCancel(true)
			.setDefaults(Notification.DEFAULT_ALL)
			.build();

		NotificationManager note = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		note.notify("error", 0, noti);
	}

	public void notifyWakelock()
	{
		Intent wakeIntent = new Intent(this, AlertActivity.class);
		wakeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent wakePIntent = PendingIntent.getActivity(this, 1, wakeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification noti = new Notification.Builder(this)
			.setContentTitle("Wakelock")
			.setContentText("Press to open App Ops to find wakelock.")
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentIntent(wakePIntent)
			.build();

		NotificationManager note = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		note.notify("alert", 1, noti);
	}

	public void setWakelockAlarm()
	{}
}
