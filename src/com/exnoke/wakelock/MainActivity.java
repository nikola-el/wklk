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

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_bar, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle presses on the action bar items
		switch (item.getItemId())
		{
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
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

		if (newVersion())
		{
			startService(new Intent(this, MainService.class));
		}

    }

	@Override
	public void onStart()
	{
		ImageButton almBut =(ImageButton)findViewById(R.id.alarmButton);
		ImageButton serBut = (ImageButton)findViewById(R.id.serviceButton);

		if (getAlarm())
		{
			almBut.setBackgroundResource(R.drawable.switch_on);
			setInfo(R.string.info_generic_alarm_on, R.string.sugg_alarm_off);
		}
		else
		{
			almBut.setBackgroundResource(R.drawable.switch_off);
			setInfo(R.string.info_generic_alarm_off, R.string.sugg_alarm_on);
		}

		if (getPower())
		{
			serBut.setBackgroundResource(R.drawable.switch_disabled);
			setInfo(R.string.info_generic_service_disabled);
		}
		else
		{
			if (isServiceRunning(MainService.class))
			{
				serBut.setBackgroundResource(R.drawable.switch_on);
			}
			else
			{
				serBut.setBackgroundResource(R.drawable.switch_off);
				setInfo(R.string.info_generic_service_off, R.string.sugg_service_disabled);
			}
		}

		super.onStart();
	}

	public boolean isServiceRunning(Class<?> serviceClass)
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
		ImageButton but = (ImageButton)view;

	    if (getAlarm())
		{
			setAlarm(false);
			but.setBackgroundResource(R.drawable.switch_off);
			if (isServiceRunning(MainService.class))
			{
				setInfo(R.string.info_generic_alarm_off, R.string.sugg_alarm_on);
			}
		}
		else
		{
			setAlarm(true);
			but.setBackgroundResource(R.drawable.switch_on);
			if (isServiceRunning(MainService.class))
			{
				setInfo(R.string.info_generic_alarm_on, R.string.sugg_alarm_off);
			}
		}
	}

	public void serviceClick(View view)
	{
		Intent service = new Intent(this, MainService.class);
		ImageButton but = (ImageButton)view;

		if (!getPower())
		{
			if (isServiceRunning(MainService.class))
			{
				but.setBackgroundResource(R.drawable.switch_off);
				setInfo(R.string.info_generic_service_off, R.string.sugg_service_disabled);
				this.stopService(service);
			}
			else
			{
				but.setBackgroundResource(R.drawable.switch_on);
				this.startService(service);
				if (!getAlarm())
				{
					setInfo(R.string.info_generic_alarm_off, R.string.sugg_alarm_on);
				}
				else
				{
					setInfo(R.string.info_generic_alarm_on, R.string.sugg_alarm_off);
				}
			}
		}
	}

	public void setInfo(int info)
	{
		TextView tv = (TextView)findViewById(R.id.bottom_text);
		tv.setText(info);
		TextView tv2 = (TextView)findViewById(R.id.italics_text);
		tv2.setText(R.string.empty);
	}

	public void setInfo(int info, int italics)
	{
		TextView tv = (TextView)findViewById(R.id.bottom_text);
		tv.setText(info);
		TextView tv2 = (TextView)findViewById(R.id.italics_text);
		tv2.setText(italics);
	}

	public void setAlarm(boolean alarm)
	{
		SharedPreferences.Editor sharedPref = getSharedPreferences(getString(R.string.settings), Context.MODE_PRIVATE).edit();
		sharedPref.putBoolean("alarm", alarm);
		sharedPref.commit();
	}

	public void setPower(boolean power)
	{
		SharedPreferences.Editor sharedPref = getSharedPreferences(getString(R.string.settings), Context.MODE_PRIVATE).edit();
		sharedPref.putBoolean("power", power);
		sharedPref.commit();
	}

	public boolean getAlarm()
	{
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.settings), Context.MODE_PRIVATE);
		return sharedPref.getBoolean("alarm", getResources().getBoolean(R.bool.alarm));
	}

	public boolean getPower()
	{
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.settings), Context.MODE_PRIVATE);
		return sharedPref.getBoolean("power", false);
	}

	public boolean newVersion()
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

	public void openSettings()
	{
		Intent settings = new Intent(this, SettingsActivity.class);
		settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(settings);
	}

	public void emailDev()
	{
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "exnoke@gmail.com", null));
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Wakelock: ");
		startActivityForResult(emailIntent, 0);
	}
}
