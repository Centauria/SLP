package com.centauria.slp.view;

import java.io.File;

import com.centauria.slp.data.Constant;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;

public class SongLiveView extends LiveView implements
MediaPlayer.OnSeekCompleteListener,
MediaPlayer.OnCompletionListener,
MediaPlayer.OnPreparedListener{
	public int songID;
	private boolean play_after_seek=false;
	private boolean musicPlayerPrepared=true;
	private MediaPlayer musicPlayer=null;
	public SongLiveView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public SongLiveView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public SongLiveView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public void setSong(File songFile){
		musicPlayer=MediaPlayer.create(mainActivity, Uri.fromFile(songFile));
		musicPlayer.setLooping(false);
		musicPlayer.setOnPreparedListener(this);
		musicPlayer.setOnCompletionListener(this);
		musicPlayer.setOnSeekCompleteListener(this);
	}
	@Override
	public void start(){
		super.start();
		new Thread(){
			@Override
			public void run(){
				while(!(musicPlayerPrepared&&updatorPrepared&&soundPoolPrepared));
				/*
				 * to make sure the soundPool is playing all the time,
				 * avoid the "first time delay":
				 */
				soundPool.play(soundMap.get(EFFECT_ID_PERFECT), 0f, 0f, 1, -1, 1);
				if(time_running_ms==0){
					fingerEventHandler.start();
					showAssessmentThread.start();
					musicPlayer.start();
					updator.start();
				}
				else{
					play_after_seek=true;
					musicPlayer.seekTo((int)(time_running_ms));
				}
			}
		}.start();
	}
	@Override
	public void pause(){
		super.pause();
		musicPlayer.pause();
	}
	@Override
	public void resume(){
		super.resume();
		musicPlayer.start();
	}
	@Override
	public void quit(){
		musicPlayer.stop();
		musicPlayer.release();
		super.quit();
	}
	@Override
	public void onPrepared(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		musicPlayerPrepared=true;
	}
	@Override
	public void onSeekComplete(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		if(play_after_seek){
			fingerEventHandler.start();
			showAssessmentThread.start();
			musicPlayer.start();
			updator.start();
		}
	}
	@Override
	public void onCompletion(MediaPlayer arg0) {
		// TODO Auto-generated method stub
//		quit();
		mainActivity.handler.sendEmptyMessage(Constant.Message.GotoView.VIEW_ASSESSMENT);
	}
}