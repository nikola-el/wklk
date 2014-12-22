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
    }
}
