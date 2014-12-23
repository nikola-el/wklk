package com.exnoke.wakelock;

import android.app.*;
import android.content.*;
import android.os.*;

public class MainActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		Intent service = new Intent(this, MainService.class);
		startService(service);

		notifyWakelock();
    }

	public void notifyWakelock()
	{
		Intent wakeIntent = new Intent(this, AlertActivity.class);
		wakeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent wakePIntent = PendingIntent.getActivity(this, 1, wakeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification noti = new Notification.Builder(this)
			.setContentTitle("Wakelock")
			.setContentText("Press to open App Ops to find wakelock.")
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentIntent(wakePIntent)
			.build();

		NotificationManager note = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		note.notify("alert", 1, noti);
	}
}
