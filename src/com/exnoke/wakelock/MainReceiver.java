package com.exnoke.wakelock;

import android.content.*;

public class MainReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		boolean screen;
		if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
		{
            screen = true;
        }
		else
		{
            screen = false;
        }
		Intent i = new Intent(context, MainService.class);
		i.putExtra("screen", screen);
		context.startService(i);
	}
}
