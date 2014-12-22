package com.exnoke.wakelock;

import android.content.*;

public class BootReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context p1, Intent p2)
	{
		Intent intent = new Intent(p1, MainService.class);
		p1.startService(intent);
	}
}
