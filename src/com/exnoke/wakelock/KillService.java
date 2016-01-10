package com.exnoke.wakelock;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.widget.*;
import java.util.*;

public class KillService extends Service
{

	@Override
	public IBinder onBind(Intent p1)
	{
		// TODO: Implement this method
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		super.onStartCommand(intent, flags, startId);
		String result = getString(R.string.audiomix_failed);

		ActivityManager acm = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> RunningProcesses = acm.getRunningAppProcesses();
		for (ActivityManager.RunningAppProcessInfo info:RunningProcesses)
		{
			String process = info.processName;
			if (!process.contains("Alarm") && !process.contains("tasker") && !process.contains("wakelock") && !process.contains("backup") && !process.contains("widget"))
			{
				acm.killBackgroundProcesses(info.processName);
			}
			SystemClock.sleep(500);
			if (!V.getAudioMix(this))
			{
				PackageManager pm = getPackageManager();
				try
				{
					String name = (String)pm.getApplicationLabel(pm.getApplicationInfo(info.processName, 0));
					SharedPreferences.Editor sharedPref = getSharedPreferences(getString(R.string.settings), Context.MODE_PRIVATE).edit();
					sharedPref.putString("audiomix", name);
					sharedPref.commit();
					result = "AudioMix wakelock (" + name + getString(R.string.audiomix_success_not);
					setKillList(info.processName);
				}
				catch (PackageManager.NameNotFoundException e)
				{}

				break;
			}
		}
		SystemClock.sleep(300);

		if (intent.getBooleanExtra("from", false))
		{
			Toast.makeText(this, result, 1).show();
		}
		else
		{
			Intent intent2 = new Intent(getString(R.string.detect));
			intent.setPackage(getPackageName());
			sendBroadcast(intent2);
		}
		stopSelf();

		return START_NOT_STICKY;
	}

	private void setKillList(String newProcess)
	{
		String killList = V.getString(this, "killlist");
		boolean newKill= true;
		String newList = newProcess;
		if (!killList.equals(""))
		{
			String[] list = killList.split(",");
			for (String process:list)
			{
				if (newProcess.equals(process))
				{
					newKill = false;
					break;
				}
			}
			if (newKill)
			{
				newList = killList + "," + newProcess;
			}
		}
		if (newKill)
		{
			SharedPreferences.Editor sharedPref = getSharedPreferences(getString(R.string.settings), Context.MODE_PRIVATE).edit();
			sharedPref.putString("killlist", newList);
			sharedPref.commit();
		}
	}
}
