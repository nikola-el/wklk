package com.exnoke.wakelock;

import android.content.*;

public class MainReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Intent i = new Intent(context, MainService.class);
		context.startService(i);
	}
}
