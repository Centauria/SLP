package com.centauria.slp;

import org.xutils.x;

import android.app.Application;

public class MainApp extends Application {

	@Override
	public void onCreate(){
		super.onCreate();
		x.Ext.init(this);
		x.Ext.setDebug(true);
	}

}
