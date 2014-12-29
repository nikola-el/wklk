package com.exnoke.wakelock;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity
{
	public int hidden;

    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
		hidden = 0;

		setInfo(R.string.info_generic);

    }

	@Override
	public void onStart()
	{
		ImageButton almBut =(ImageButton)findViewById(R.id.alarmButton);
		ImageButton serBut = (ImageButton)findViewById(R.id.serviceButton);

		if (getAlarm())
		{
			almBut.setBackgroundResource(R.drawable.switch_on);
		}
		else
		{
			almBut.setBackgroundResource(R.drawable.switch_off);
		}

		if (getPower())
		{
			serBut.setBackgroundResource(R.drawable.switch_disabled);
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
		}
		else
		{
			setAlarm(true);
			but.setBackgroundResource(R.drawable.switch_on);
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
				this.stopService(service);
			}
			else
			{
				but.setBackgroundResource(R.drawable.switch_on);
				this.startService(service);
			}
		}
	}

	public void hidden(View view)
	{
		hidden++;
		hidden %= 4;
		if (hidden == 3)
		{
			this.startActivity(new Intent(this, AlertActivity.class));
		}
	}

	public void setInfo(int info)
	{
		TextView tv = (TextView)findViewById(R.id.bottom_text);
		tv.setText(info);
	}

	public void setAlarm(boolean alarm)
	{
		SharedPreferences.Editor sharedPref = getSharedPreferences(getString(R.string.settings), Context.MODE_PRIVATE).edit();
		sharedPref.putBoolean("alarm", alarm);
		sharedPref.commit();
	}

	public void setPower(boolean service)
	{
		SharedPreferences.Editor sharedPref = getSharedPreferences(getString(R.string.settings), Context.MODE_PRIVATE).edit();
		sharedPref.putBoolean("power", service);
		sharedPref.commit();
	}

	public boolean getAlarm()
	{
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.settings), Context.MODE_PRIVATE);
		return sharedPref.getBoolean("alarm", true);
	}

	public boolean getPower()
	{
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.settings), Context.MODE_PRIVATE);
		return sharedPref.getBoolean("power", false);
	}

}
