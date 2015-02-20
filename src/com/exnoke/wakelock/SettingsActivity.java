package com.exnoke.wakelock;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.text.*;

public class SettingsActivity extends Activity
{
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.theme_bar, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle presses on the action bar items
		switch (item.getItemId())
		{
			case android.R.id.home:
				finish();
				return true;
			case R.id.theme_switch:
				putTheme();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		V.setTheme(this);
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
		setListenerCheck();

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

	public void checkList(View v)
	{
		setListenerCheck();
		Intent reqIntent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
		startActivityForResult(reqIntent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == 0)
		{
			setListenerCheck();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void setListenerCheck()
	{
		if (V.ListenerNeeded())
		{
			CheckBox list_check = (CheckBox)findViewById(R.id.list_check);
			list_check.setChecked(V.get(this, "listener", false));
		}
		else
		{
			LinearLayout layout = (LinearLayout)findViewById(R.id.listenerLayout);
			layout.setVisibility(View.GONE);
		}
	}

	private void putTheme()
	{
		V.set(this, "theme", !V.get(this, "theme", false));
		startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
	}
}
