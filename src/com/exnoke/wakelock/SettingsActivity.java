package com.exnoke.wakelock;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

public class SettingsActivity extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public void onStart()
	{
		CheckBox ops_check = (CheckBox)findViewById(R.id.ops_check);
		ops_check.setChecked(getOps());
		ops_check.setEnabled(getResources().getBoolean(R.bool.ops_enabled));
		TextView text = (TextView)findViewById(R.id.app_ops_info);
		text.setText(ops_check.isChecked() ?R.string.ops_true: R.string.ops_false);
		CheckBox not_check = (CheckBox)findViewById(R.id.not_check);
		not_check.setChecked(getNot());

		super.onStart();
	}


	public void checkOps(View v)
	{
		CheckBox ops_check = (CheckBox)v;
		TextView text = (TextView)findViewById(R.id.app_ops_info);
		if (ops_check.isChecked())
		{
			text.setText(R.string.ops_true);
		}
		else
		{
			text.setText(R.string.ops_false);
		}
		setOps(ops_check.isChecked());
	}

	public void checkNot(View v)
	{
		CheckBox not_check = (CheckBox)v;
		setNot(not_check.isChecked());
	}

	public void setOps(boolean ops)
	{
		SharedPreferences.Editor sharedPref = getSharedPreferences(getString(R.string.settings), Context.MODE_PRIVATE).edit();
		sharedPref.putBoolean("ops", ops);
		sharedPref.commit();
	}

	public void setNot(boolean not)
	{
		SharedPreferences.Editor sharedPref = getSharedPreferences(getString(R.string.settings), Context.MODE_PRIVATE).edit();
		sharedPref.putBoolean("not", not);
		sharedPref.commit();
	}

	public boolean getNot()
	{
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.settings), Context.MODE_PRIVATE);
		return sharedPref.getBoolean("not", true);
	}

	public boolean getOps()
	{
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.settings), Context.MODE_PRIVATE);
		return sharedPref.getBoolean("ops", getResources().getBoolean(R.bool.ops));
	}
}
