package com.exnoke.wakelock;

import android.app.*;
import android.content.*;
import android.os.*;
import java.util.*;

public final class V
{
	protected static final boolean KitKat()
	{
		return Build.VERSION.SDK_INT == 19;
	}

	protected static final boolean Lollipop()
	{
		return Build.VERSION.SDK_INT == 21;
	}
	
	protected static final boolean ListenerNeeded()
	{
		return Build.VERSION.SDK_INT >= 18 & Build.VERSION.SDK_INT <= 20;
	}

	protected static final String getTaskInfo(Context p1)
	{
		ActivityManager acm = (ActivityManager)p1.getSystemService(p1.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> RunningTask = acm.getRunningTasks(1);
		ActivityManager.RunningTaskInfo ar = RunningTask.get(0);
		return ar.topActivity.getClassName().toString();
	}

	protected static final boolean get(Context p1 , String p2, int res)
	{
		SharedPreferences sharedPref = p1.getSharedPreferences(p1.getString(R.string.settings), Context.MODE_PRIVATE);
		return sharedPref.getBoolean(p2, p1.getResources().getBoolean(res));
	}

	protected static final boolean get(Context p1, String p2, boolean res)
	{
		SharedPreferences sharedPref = p1.getSharedPreferences(p1.getString(R.string.settings), Context.MODE_PRIVATE);
		return sharedPref.getBoolean(p2, res);
	}

	protected static final long getOff(Context p1)
	{
		SharedPreferences sharedPref = p1.getSharedPreferences(p1.getString(R.string.settings), Context.MODE_PRIVATE);
		return sharedPref.getLong("offTime", -1);
	}

	protected static final void setOff(Context p1, long p2)
	{
		SharedPreferences.Editor sharedPref = p1.getSharedPreferences(p1.getString(R.string.settings), Context.MODE_PRIVATE).edit();
		sharedPref.putLong("offTime", p2);
		sharedPref.commit();
	}

	protected static final int getWait(Context p1)
	{
		SharedPreferences sharedPref = p1.getSharedPreferences(p1.getString(R.string.settings), Context.MODE_PRIVATE);
		return sharedPref.getInt("waitFor", 0);
	}

	protected static final void setWait(Context p1, int p2)
	{
		SharedPreferences.Editor sharedPref = p1.getSharedPreferences(p1.getString(R.string.settings), Context.MODE_PRIVATE).edit();
		sharedPref.putInt("waitFor", p2);
		sharedPref.commit();
	}

	protected static final void set(Context p1, String p2, boolean res)
	{
		SharedPreferences.Editor sharedPref = p1.getSharedPreferences(p1.getString(R.string.settings), Context.MODE_PRIVATE).edit();
		sharedPref.putBoolean(p2, res);
		sharedPref.commit();
	}

	protected static final void setBackup(Context p1, String p2)
	{
		SharedPreferences.Editor sharedPref = p1.getSharedPreferences(p1.getString(R.string.settings), Context.MODE_PRIVATE).edit();
		sharedPref.putString("cycle", p2);
		sharedPref.commit();
	}

	protected static final String getBackup(Context p1)
	{
		SharedPreferences sharedPref = p1.getSharedPreferences(p1.getString(R.string.settings), Context.MODE_PRIVATE);
		return sharedPref.getString("cycle", "");
	}

	protected static final boolean getPower(Context p1)
	{
		Intent p2 = p1.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		Integer status = p2.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		return status > 0;
	}

	protected static int clearValues(Context p1)
	{
		set(p1, "update", false);
		setWait(p1, 0);
		setOff(p1, -1);

		return Service.START_STICKY;
	}
	
	protected static final void setTheme(Context p1)
	{
		p1.setTheme(get(p1, "theme", false)?R.style.DarkTheme:R.style.LightTheme);
	}
}
