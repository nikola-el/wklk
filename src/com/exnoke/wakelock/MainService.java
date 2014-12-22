package com.exnoke.wakelock;

import android.app.*;
import android.content.*;
import android.media.*;
import android.os.*;
import android.provider.*;
import android.widget.*;
import android.telephony.*;

public class MainService extends Service
{
	public Integer count;
	public Integer waitFor;
	public AlarmManager am;
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
		boolean passed = true;
		PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
		Vibrator vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);
		AudioManager aud = (AudioManager)getSystemService(AUDIO_SERVICE);

		ContentResolver cr = getContentResolver();
		Boolean isPolicy = (Integer.parseInt(Settings.Global.getString(cr, Settings.Global.WIFI_SLEEP_POLICY)) < 2);
		Boolean isWifiOff = (Integer.parseInt(Settings.Global.getString(cr, Settings.Global.WIFI_ON)) == 0);

		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = registerReceiver(null, ifilter);
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		Boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL);

		TelephonyManager tl = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		Boolean inCall = ((Integer)tl.getCallState() > 0);

		passed &= !aud.isMusicActive();
		passed &= !inCall;
		passed &= !isCharging;
		passed &= (isPolicy || isWifiOff);

		if (passed)
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
					aInt = PendingIntent.getService(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
					am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 600000, aInt);

					vib.vibrate(200);
				}

				else
				{
					waitFor = 0;
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
				String result = count.toString();
				Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
				Toast.makeText(this, alarm.toString(), Toast.LENGTH_SHORT).show();
				alarm = false;
			}
		}
		return START_STICKY;
	}
}
