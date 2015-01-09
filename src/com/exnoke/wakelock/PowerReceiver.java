package com.exnoke.wakelock;

import android.content.*;

public class PowerReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context p1, Intent p2)
	{
		Intent intent = new Intent(p1, MainService.class);

		if (!V.getPower(p1))
		{
			p1.startService(intent);
		}

		String ra = V.getTaskInfo(p1);
	    if (ra.equals("com.exnoke.wakelock.MainActivity"))
		{
			Intent act = new Intent(p1, MainActivity.class);
			act.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			p1.startActivity(act);
		}
	}

}
