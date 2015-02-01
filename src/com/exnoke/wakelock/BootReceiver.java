package com.exnoke.wakelock;

import android.content.*;

public class BootReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context p1, Intent p2)
	{
		p1.startService(new Intent(p1, MainService.class));
	}
}
