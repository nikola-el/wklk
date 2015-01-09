package com.exnoke.wakelock;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
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

		findViewById(R.id.alarmButton).setOnLongClickListener(new OnLongClickListener()
			{
				public boolean onLongClick(View v)
				{
					if (V.KitKat())
					{
						Intent reqIntent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
						startActivityForResult(reqIntent, 2);
					}
					return true;
				}
			});

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

		if (V.Lollipop() && !getListener())
		{
			setAlarm(false);
		}

		if (getAlarm())
		{
			almBut.setBackgroundResource(R.drawable.switch_on);
			setInfo(R.string.info_generic_alarm_on, R.string.sugg_alarm_off);
		}
		else
		{
			almBut.setBackgroundResource(R.drawable.switch_off);
			setInfo(R.string.info_generic_alarm_off, (!V.Lollipop() || getListener()) ?R.string.sugg_alarm_on: R.string.sugg_alarm_listener);
		}

		if (V.getPower(this))
		{
			serBut.setBackgroundResource(R.drawable.switch_disabled);
			setInfo(R.string.info_generic_service_disabled);
		}
		else
		{
			if (V.isServiceRunning(this, MainService.class))
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

	public void alarmClick(View view)
	{
		ImageButton but = (ImageButton)view;

	    if (getAlarm())
		{
			setAlarm(false);
			but.setBackgroundResource(R.drawable.switch_off);
			if (V.isServiceRunning(this, MainService.class))
			{
				setInfo(R.string.info_generic_alarm_off, R.string.sugg_alarm_on);
			}
		}
		else
		{
			if (!V.Lollipop() || getListener())
			{
				setAlarm(true);
				but.setBackgroundResource(R.drawable.switch_on);
				if (V.isServiceRunning(this, MainService.class))
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
		ImageButton but = (ImageButton)view;

		if (!V.getPower(this))
		{
			if (V.isServiceRunning(this, MainService.class))
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
					setInfo(R.string.info_generic_alarm_off, (!V.Lollipop() || getListener()) ?R.string.sugg_alarm_on: R.string.sugg_alarm_listener);
				}
				else
				{
					setInfo(R.string.info_generic_alarm_on, R.string.sugg_alarm_off);
				}
			}
		}
	}

	private void setInfo(int info)
	{
		TextView tv = (TextView)findViewById(R.id.bottom_text);
		tv.setText(info);
		TextView tv2 = (TextView)findViewById(R.id.italics_text);
		tv2.setText(R.string.empty);
	}

	private void setInfo(int info, int italics)
	{
		TextView tv = (TextView)findViewById(R.id.bottom_text);
		tv.setText(info);
		TextView tv2 = (TextView)findViewById(R.id.italics_text);
		tv2.setText(italics);
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

	private void openSettings()
	{
		Intent settings = new Intent(this, SettingsActivity.class);
		settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(settings);
	}

	private void emailDev()
	{
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "exnoke@gmail.com", null));
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Wakelock: ");
		startActivityForResult(emailIntent, 1);
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

}
