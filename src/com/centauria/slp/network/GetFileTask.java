package com.centauria.slp.network;

import java.io.File;

import org.xutils.x;
import org.xutils.common.Callback.ProgressCallback;
import org.xutils.common.util.LogUtil;
import org.xutils.http.RequestParams;

import android.os.AsyncTask;

public class GetFileTask extends AsyncTask<Integer, Integer, String> implements ProgressCallback<File>{
	public volatile boolean done=false;
	private String sourceUrl;
	private String saveFilePath;
	public File resultFile;
	
	public GetFileTask(String src,String path){
		super();
		this.sourceUrl=src;
		this.saveFilePath=path;
	}
	
	@Override
	protected String doInBackground(Integer... arg0) {
		// TODO Auto-generated method stub
		RequestParams req=new RequestParams(this.sourceUrl);
		req.setSaveFilePath(this.saveFilePath);
		x.http().get(req, this);
		return null;
	}

	@Override
	public void onSuccess(File result) {
		// TODO Auto-generated method stub
		resultFile=result;
		done=true;
	}

	@Override
	public void onError(Throwable ex, boolean isOnCallback) {
		// TODO Auto-generated method stub
		LogUtil.e(ex.getMessage());
//		Toast.makeText(x.app(), ex.getMessage(), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onCancelled(CancelledException cex) {
		// TODO Auto-generated method stub
		LogUtil.e("cancelled");
//		Toast.makeText(x.app(), "cancelled", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onFinished() {
		// TODO Auto-generated method stub
		LogUtil.i("finished");
//		Toast.makeText(x.app(), "finished", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onWaiting() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStarted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoading(long total, long current, boolean isDownloading) {
		// TODO Auto-generated method stub
		
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public String getSaveFilePath() {
		return saveFilePath;
	}

	public void setSaveFilePath(String saveFilePath) {
		this.saveFilePath = saveFilePath;
	}

}
