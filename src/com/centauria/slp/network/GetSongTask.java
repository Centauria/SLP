package com.centauria.slp.network;

import com.centauria.slp.R;

import android.app.ProgressDialog;

public class GetSongTask extends GetFileTask {
	private ProgressDialog pd;
	
	public GetSongTask(String src, String path,ProgressDialog dialog) {
		super(src, path);
		// TODO Auto-generated constructor stub
		this.pd=dialog;
	}
	
	@Override
	public void onStarted(){
		super.onStarted();
		pd.setCancelable(false);
		pd.setTitle(R.string.downloading);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.show();
	}
	
	@Override
	public void onLoading(long total, long current, boolean isDownloading) {
		// TODO Auto-generated method stub
		super.onLoading(total, current, isDownloading);
		pd.setMax((int) total);
		pd.setProgress((int) current);
	}
	
	@Override
	public void onFinished(){
		pd.dismiss();
		super.onFinished();
	}
}
