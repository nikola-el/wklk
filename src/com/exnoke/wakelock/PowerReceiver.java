package com.exnoke.wakelock;

import android.app.*;
import android.content.*;
import android.os.*;
import java.util.*;

public class PowerReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context p1, Intent p2)
	{
		Intent intent = new Intent(p1, MainService.class);
		p2 = p1.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		Integer status = p2.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

		SharedPreferences.Editor sharedPref = p1.getSharedPreferences(p1.getString(R.string.settings), Context.MODE_PRIVATE).edit();
		if (status > 0)
		{
			p1.stopService(intent);
			sharedPref.putBoolean("power", true);
		}
		else
		{
			p1.startService(intent);
			sharedPref.putBoolean("power", false);
		}
		sharedPref.commit();

		ActivityManager acm = (ActivityManager)p1.getSystemService(p1.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> RunningTask = acm.getRunningTasks(1);
		ActivityManager.RunningTaskInfo ar = RunningTask.get(0);
		String ra = ar.topActivity.getClassName().toString();
	    if (ra.equals("com.exnoke.wakelock.MainActivity"))
		{
			Intent act = new Intent(p1, MainActivity.class);
			act.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			p1.startActivity(act);
		}
	}

}
