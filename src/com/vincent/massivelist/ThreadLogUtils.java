package com.vincent.massivelist;

import android.util.Log;

public class ThreadLogUtils
{
	private static final String TAG = "ThreadLogUtils";
	
	public static final void logThread()
	{
		Thread t = Thread.currentThread();
		Log.d(TAG, "<" + t.getName() + ">ID: " + t.getId() + ", Priority: " + t.getPriority() + ", Group: "
                + t.getThreadGroup().getName());
	}
}
