package com.exnoke.wakelock;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity
{
	public BroadcastReceiver receiver;
	private boolean waitforkill;

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_bar, menu);
		if (batteryStats())
		{
			menu.add(0, 100, 0, "Cycle Info");
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle presses on the action bar items
		switch (item.getItemId())
		{
			case 100:
				Intent settings = new Intent().setComponent(new ComponentName("com.exnoke.battery.cycle", "com.exnoke.battery.cycle.StatsActivity"));
				settings.putExtra("parent", true);
				settings.putExtra("theme", V.get(this, "theme", false));
				startActivity(settings);
				return true;
			case R.id.action_settings:
				openSettings();
				return true;
			case R.id.action_email:
				emailDev();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

    @Override
    public void onCreate(Bundle savedInstanceState)
	{
		V.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		if (newVersion())
		{
			startService(new Intent(this, MainService.class));
		}

		V.checkKillList(this);
		//V.setBackup(this, 200.0f, 0.26f, 1.61f, 0.86f, 200.0f, 1.61f, "31.03.2015_200.00_1.61", System.currentTimeMillis(), 200.0f, 1.0f);
    }

	@Override
	public void onResume()
	{
		super.onResume();

		waitforkill = false;
		receiver = new LocalReceiver();
		IntentFilter InF = new IntentFilter(getString(R.string.filter));
		InF.addAction(getString(R.string.detect));
		registerReceiver(receiver, InF);

		updateUI();
	}

	@Override
	protected void onPause()
	{
		unregisterReceiver(receiver);
		super.onPause();
	}

	private void updateUI()
	{
		Switch almBut =(Switch)findViewById(R.id.alarmButton);
		Switch serBut = (Switch)findViewById(R.id.serviceButton);

		if (V.Lollipop() && !getListener())
		{
			setAlarm(false);
		}

		if (getAlarm())
		{
			almBut.setChecked(true);
			setInfo(R.string.info_generic_alarm_on, R.string.sugg_alarm_off);
		}
		else
		{
			almBut.setChecked(false);
			setInfo(R.string.info_generic_alarm_off, (!V.Lollipop() || getListener()) ?R.string.sugg_alarm_on: R.string.sugg_alarm_listener);
		}

		if (V.getPower(this))
		{
			serBut.setChecked(false);
			serBut.setEnabled(false);
			setInfo(R.string.info_generic_service_disabled);
		}
		else
		{
			serBut.setEnabled(true);
			if (isServiceRunning())
			{
				serBut.setChecked(true);
			}
			else
			{
				serBut.setChecked(false);
				setInfo(R.string.info_generic_service_off, R.string.sugg_service_disabled);
			}
		}
	}

	private boolean isServiceRunning()
	{
		return serviceCheck(MainService.class);
	}

	private boolean isKillInProgress()
	{
		return serviceCheck(KillService.class);
	}

	private boolean serviceCheck(Class<?> serviceClass)
	{
		ActivityManager acm =(ActivityManager)getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> rs = acm.getRunningServices(Integer.MAX_VALUE);

		for (int i=0; i < rs.size(); i++)
		{
			if (serviceClass.getName().equals((rs.get(i).service.getClassName())))
			{return true;} 
		}

		return false;
	}

	public void alarmClick(View view)
	{
		Switch but = (Switch)view;

		if (getAlarm())
		{
			setAlarm(false);
			but.setChecked(false);
			if (isServiceRunning())
			{
				setInfo(R.string.info_generic_alarm_off, R.string.sugg_alarm_on);
			}
		}
		else
		{
			if (!V.Lollipop() || getListener())
			{
				setAlarm(true);
				but.setChecked(true);
				if (isServiceRunning())
				{
					setInfo(R.string.info_generic_alarm_on, R.string.sugg_alarm_off);
				}
			}
			else
			{
				Toast.makeText(this, getString(R.string.toast_set_listener), Toast.LENGTH_LONG).show();
				Intent reqIntent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
				startActivityForResult(reqIntent, 0);
			}
		}
	}

	public void serviceClick(View view)
	{
		Intent service = new Intent(this, MainService.class);
		Switch but = (Switch)view;

		if (!V.getPower(this))
		{
			but.setEnabled(true);
			if (isServiceRunning())
			{
				but.setChecked(false);
				setInfo(R.string.info_generic_service_off, R.string.sugg_service_disabled);
				this.stopService(service);
			}
			else
			{
				but.setChecked(true);
				this.startService(service);
				if (!getAlarm())
				{
					setInfo(R.string.info_generic_alarm_off, (!V.Lollipop() || getListener()) ?R.string.sugg_alarm_on: R.string.sugg_alarm_listener);
				}
				else
				{
					setInfo(R.string.info_generic_alarm_on, R.string.sugg_alarm_off);
				}
			}
		}
	}

	public void releaseAudioMix(View view)
	{
		V.putAudioMix(this, "");
		if (V.getAudioMix(this))
		{
			TextView tv2 = (TextView)view;
			tv2.setTextColor(-16777216);
			tv2.setText(R.string.kill_in_progress);

			Intent ks = new Intent(this, KillService.class);
			this.startService(ks);
		}
	}

	private void setInfo(int info)
	{
		setInfo(info, R.string.empty);
	}

	private void setInfo(int info, int italics)
	{
		TextView tv = (TextView)findViewById(R.id.bottom_text);
		tv.setText(info);
		TextView tv2 = (TextView)findViewById(R.id.italics_text);

		if (waitforkill)
		{
			if (!V.getAudioMix(this))
			{
				tv2.setTextColor(-16776961);
				tv2.setText(getString(R.string.audiomix_success) + " " + V.getString(this, "audiomix") + ".");
			}
			else
			{
				tv2.setTextColor(-65536);
				tv2.setText(R.string.audiomix_failed);
			}
		}

		else if (V.getAudioMix(this))
		{
			tv2.setTextColor(-65536);
			tv2.setText(R.string.audiomix_info);
		}

		else
		{
			tv2.setTextColor(-16777216);
			tv2.setText(isKillInProgress() ?R.string.kill_in_progress: italics);
		}
	}

	private void setAlarm(boolean alarm)
	{
		V.set(this, "alarm", alarm);
	}

	private boolean getAlarm()
	{
		return V.get(this, "alarm", R.bool.alarm);
	}

	private boolean getListener()
	{
		return V.get(this, "listener", false);
	}

	private boolean newVersion()
	{
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.settings), Context.MODE_PRIVATE);
		try
		{
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			boolean result = pInfo.versionCode > sharedPref.getInt("version", -1);
			if (result)
			{
				SharedPreferences.Editor edit = sharedPref.edit();
				edit.putInt("version", pInfo.versionCode);
				edit.commit();
			}
			return result;
		}
		catch (PackageManager.NameNotFoundException e)
		{return false;}
	}

	private boolean batteryStats()
	{
		try
		{
			getPackageManager().getPackageInfo("com.exnoke.battery.cycle", 0);
		} 
		catch (PackageManager.NameNotFoundException e)
		{
			return false;
		}  
		return true;
	}

	private void openSettings()
	{
		Intent settings = new Intent(this, SettingsActivity.class);
		settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(settings);
	}

	private void emailDev()
	{
		try
		{
			Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "exnoke@gmail.com", null));
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Wakelock: ");
			startActivityForResult(emailIntent, 1);
		}
		catch (Exception e)
		{
			Toast.makeText(this, getString(R.string.no_email), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == 0 && getListener())
		{
			alarmClick(findViewById(R.id.alarmButton));
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private class LocalReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context p1, Intent p2)
		{
			if (p2.getAction().equals(getString(R.string.detect)))
			{
				waitforkill = true;
			}
			updateUI();
		}
	}
}
