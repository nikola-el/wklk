package com.exnoke.wakelock;

import android.app.*;
import android.os.*;

public class AlertActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		NotificationManager note =(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		note.cancel("alert", 1);
		finish();
	}
}
