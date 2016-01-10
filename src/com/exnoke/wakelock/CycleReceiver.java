package com.exnoke.wakelock;

import android.content.*;

public class CycleReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context p1, Intent p2)
	{
		if (p2.getAction().equals("com.exnoke.battery.cycle.BACKUP_STATS"))
		{
			Float cycle = p2.getFloatExtra("cycle", 0f);
			Float min = p2. getFloatExtra("min", 0f);
			Float max = p2. getFloatExtra("max", 0f);
			Float average = p2. getFloatExtra("average", 0f);
			Float week = p2. getFloatExtra("week", 0f);
			Float diff = p2. getFloatExtra("diff", 0f);
			String history = p2. getStringExtra("history");
			Long start = p2.getLongExtra("start", 0l);
			Float initial = p2.getFloatExtra("initial", 0l);
			Float my = p2.getFloatExtra("my", 0f);

			V.setBackup(p1, cycle, min, max, average, week, diff, history, start, initial, my);
		}

		else if (p2.getAction().equals("com.exnoke.battery.cycle.RESTORE_STATS"))
		{
			Intent restore = new Intent(p1.getPackageName() + ".RESTORE_STATS");
			restore.setPackage("com.exnoke.battery.cycle");
			restore.putExtra("cycle", V.getFloat(p1, "cycle"));
			restore.putExtra("min", V.getFloat(p1, "min"));
			restore.putExtra("max", V.getFloat(p1, "max"));
			restore.putExtra("average", V.getFloat(p1, "average"));
			restore.putExtra("week", V.getFloat(p1, "week"));
			restore.putExtra("diff", V.getFloat(p1, "diff"));
			restore.putExtra("history", V.getString(p1, "history"));
			restore.putExtra("start", V.getLong(p1, "start"));
			restore.putExtra("initial", V.getFloat(p1, "initial"));
			p1.sendBroadcast(restore);
		}
	}
}
