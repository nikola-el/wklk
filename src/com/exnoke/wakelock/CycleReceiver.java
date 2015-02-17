package com.exnoke.wakelock;

import android.content.*;

public class CycleReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context p1, Intent p2)
	{
		if (p2.getAction().equals("com.exnoke.battery.cycle.BACKUP_STATS"))
		{
			String cycle = "";
			try
			{
				cycle = p2.getStringExtra("cycle");
			}
			catch (Exception e)
			{}
			V.setBackup(p1, cycle);
		}

		else if (p2.getAction().equals("com.exnoke.battery.cycle.RESTORE_STATS"))
		{
			Intent restore = new Intent("com.exnoke.wakelock.RESTORE_STATS");
			restore.setPackage("com.exnoke.battery.cycle");
			restore.putExtra("cycle", V.getBackup(p1));
			p1.sendBroadcast(restore);
		}
	}
}
