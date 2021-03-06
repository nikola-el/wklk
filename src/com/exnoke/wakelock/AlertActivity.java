package com.exnoke.wakelock;

import android.app.*;
import android.content.*;
import android.os.*;

public class AlertActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Intent intent = new Intent();
		if (V.get(this, "ops", R.bool.ops))
		{
			if (V.KitKat())
			{
				intent.setClassName("com.android.settings", "com.android.settings.Settings");
				intent.setAction("android.intent.action.MAIN");
				intent.addCategory("android.intent.category.DEFAULT");
				intent.setFlags(0x10008000);
				intent.putExtra(":android:show_fragment", "com.android.settings.applications.AppOpsSummary");
			} 
			else
			{
				intent.setAction("android.intent.action.MAIN");
				intent.addCategory("android.intent.category.LAUNCHER");
				intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$AppOpsSummaryActivity"));
			}
		}
		else
		{
			intent.setAction(Intent.ACTION_POWER_USAGE_SUMMARY);
		}

		NotificationManager note = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		note.cancel("alert", 1);

		startActivity(intent);
		finish();
	}
}
