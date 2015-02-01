package com.exnoke.wakelock;

import android.app.*;
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
		ops_check.setChecked(V.get(this, "ops", R.bool.ops));
		ops_check.setEnabled(getResources().getBoolean(R.bool.ops_enabled));
		TextView text = (TextView)findViewById(R.id.app_ops_info);
		text.setText(ops_check.isChecked() ?R.string.ops_true: R.string.ops_false);
		CheckBox not_check = (CheckBox)findViewById(R.id.not_check);
		not_check.setChecked(V.get(this, "not", true));

		super.onStart();
	}


	public void checkOps(View v)
	{
		CheckBox ops_check = (CheckBox)findViewById(R.id.ops_check);
		TextView text = (TextView)findViewById(R.id.app_ops_info);
		if (ops_check.isChecked())
		{
			text.setText(R.string.ops_true);
		}
		else
		{
			text.setText(R.string.ops_false);
		}
		V.set(this, "ops", ops_check.isChecked());
	}

	public void checkOpsFromLabel(View v)
	{
		CheckBox ops_check = (CheckBox)findViewById(R.id.ops_check);
		ops_check.setChecked(!ops_check.isChecked());
		checkOps(v);
	}

	public void checkNot(View v)
	{
		V.set(this, "not", ((CheckBox)findViewById(R.id.not_check)).isChecked());
	}

	public void checkNotFromLabel(View v)
	{
		CheckBox not_check = (CheckBox)findViewById(R.id.not_check);
		not_check.setChecked(!not_check.isChecked());
		checkNot(v);
	}
}
