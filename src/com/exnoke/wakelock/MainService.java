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
	private String msg;
	private PendingIntent aInt;
	private BroadcastReceiver mReceiver;

	@Override
	public IBinder onBind(Intent p1)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		mReceiver = new MainReceiver();
		registerReceiver(mReceiver, filter);

		msg = "";
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		super.onStartCommand(intent, flags, startId);

		if (V.get(this, "update", false))
		{
			V.set(this, "update", false);

			Intent restore = new Intent("com.exnoke.wakelock.WAKE_WHEN_PAUSED");
			restore.setPackage("ru.yanus171.android.handyclockwidget.free");
			sendBroadcast(restore);

			updateOFF();
		}

		return START_STICKY;
	}

	private int updateON()
	{
		try
		{
			am.cancel(aInt);
		}
		catch (Exception e)
		{}

		//Toast.makeText(this,V.getTaskInfo(this),1).show();

		if (msg != "")
		{
			Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
			msg = "";
		}

		return START_STICKY;
	}

	private int updateOFF()
	{
		// break code due to unwanted conditions

		msg = "";

		if (V.getPower(this))
		{
			return V.clearValues(this);
		}

		AudioManager aud = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (aud.isMusicActive())
		{
			V.killDinerDash(this);
			if (aud.isWiredHeadsetOn())
			{
				waitFiveMins(0);
				return START_STICKY;
			}
		}

		TelephonyManager tl = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		Boolean inCall = ((Integer)tl.getCallState() > 0);
		if (inCall)
		{
			return V.clearValues(this);
		}

		if (getCurrents())
		{
			waitFiveMins(0);
			return START_STICKY;
		}

		int waitFor = V.getWait(this);
		long offTime = V.getOff(this);

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
				if (wakelockDetected(offTime))
				{
					if (V.get(this, "alarm", R.bool.alarm))
					{
						waitFiveMins(2);
					}
					notifyWhat();
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
		}

		else
		{
			notifyError(waitFor);
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	private void notifyWhat()
	{
		if (V.getAudioMix(this))
		{
			notifyAudioMix();
		}
		else
		{
			notifyWakelock();
		}
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

	private void notifyAudioMix()
	{
		Intent audioIntent = new Intent(this, KillService.class);
		audioIntent.putExtra("from", true);
		PendingIntent wakePIntent = PendingIntent.getService(this, 3, audioIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		CharSequence wakeInfo = getString(R.string.wake_audio);

		Notification noti = new Notification.Builder(this)
			.setContentTitle("AudioMix")
			.setContentText(wakeInfo)
			.setSmallIcon(R.drawable.ic_dialog_alert)
			.setContentIntent(wakePIntent)
			.setPriority(Notification.PRIORITY_HIGH)
			.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
			.setAutoCancel(true)
			.build();

		if (V.get(this, "not", true))
		{
			noti.sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.error);
		}
		NotificationManager note = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		note.notify("audio", 2, noti);
	}

	private void notifyWakelock()
	{
		Intent wakeIntent = new Intent(this, AlertActivity.class);
		wakeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent wakePIntent = PendingIntent.getActivity(this, 1, wakeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		CharSequence wakeInfo = V.get(this, "ops", R.bool.ops) ?getString(R.string.wake_ops): getString(R.string.wake_battery);

		Notification noti = new Notification.Builder(this)
			.setContentTitle("Wakelock")
			.setContentText(wakeInfo)
			.setSmallIcon(R.drawable.ic_dialog_alert)
			.setContentIntent(wakePIntent)
			.setPriority(Notification.PRIORITY_HIGH)
			.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
			.setAutoCancel(true)
			.build();

		if (V.get(this, "not", true))
		{
			noti.sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.error);
		}
		NotificationManager note = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		note.notify("alert", 1, noti);
	}

	private void waitFiveMins(int waitStatus)
	{
		am = (AlarmManager)getSystemService(ALARM_SERVICE);
		Intent alarmIntent = new Intent(this, MainService.class);
		V.setOff(this, SystemClock.uptimeMillis());
		V.setWait(this, waitStatus);
		V.set(this, "update", true);
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
		Long test = (SystemClock.uptimeMillis() - diff) / 1000;
		msg = test.toString();
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
			{
				return true;
			}
		}

		String ra = V.getTaskInfo(this);
		String[] tasks = getResources().getStringArray(R.array.tasks);

		for (String act:tasks)
		{
			if (ra.contains(act))
			{
				return true;
			}
		}

		return V.get(this, "listener", false) ?V.get(this, "pkg" , false): false;
	}

	private class MainReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context p1, Intent p2)
		{
			V.clearValues(p1);

			if (!V.getPower(p1))
			{
				if (p2.getAction().equals(Intent.ACTION_SCREEN_ON))
				{
					updateON();
				}
				else
				{
					updateOFF();
				}
			}
		}
	}
}
