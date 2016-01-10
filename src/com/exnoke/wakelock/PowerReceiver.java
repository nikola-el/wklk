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

		Intent intent = new Intent(p1.getString(R.string.filter));
		intent.setPackage(p1.getPackageName());
		p1.sendBroadcast(intent);
	}
}
