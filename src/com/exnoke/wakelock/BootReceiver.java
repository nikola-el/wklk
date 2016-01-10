package com.exnoke.wakelock;

import android.content.*;

public class BootReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context p1, Intent p2)
	{
		V.clearValues(p1);
		V.checkKillList(p1);
		p1.startService(new Intent(p1, MainService.class));
	}
}
