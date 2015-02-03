package com.exnoke.wakelock;

import android.content.*;

public class PowerReceiver extends BroadcastReceiver
{
	
	@Override
	public void onReceive(Context p1, Intent p2)
	{
		if (!V.getPower(p1))
		{
			V.clearValues(p1);
			p1.startService(new Intent(p1, MainService.class));
		}

		Intent intent = new Intent(MainService.FILTER);
		intent.setPackage("com.exnoke.wakelock");
		p1.sendBroadcast(intent);
	}
}
