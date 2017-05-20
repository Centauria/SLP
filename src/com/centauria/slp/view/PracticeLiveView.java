package com.centauria.slp.view;

import org.xutils.x;

import com.centauria.slp.data.Constant;
import com.centauria.slp.util.PatternMaker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.SparseIntArray;

public class PracticeLiveView extends LiveView {
	private boolean rhythmPoolPrepared=false;
	private double endTime_s;
	
	private SoundPool rhythmPool;
	private RhythmThread rhythmThread;
	private SparseIntArray rhythmMap;
	
	private int RHYTHM_ID_KICK=Constant.Rhythm.RHYTHM_KICK;
	private int RHYTHM_ID_HAT_CLOSED=Constant.Rhythm.RHYTHM_HAT_CLOSED;
	private int RHYTHM_ID_SNARE=Constant.Rhythm.RHYTHM_SNARE;
	
	public PracticeLiveView(Context context) {
		super(context);
		init(context);
	}

	public PracticeLiveView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PracticeLiveView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void init(Context context){
		if(!isInEditMode()){
			if(Build.VERSION.SDK_INT>=21){
				AudioAttributes attr = new AudioAttributes.Builder()
			            .setUsage(AudioAttributes.USAGE_GAME)
			            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
			            .build();
				rhythmPool = new SoundPool.Builder()
			            .setAudioAttributes(attr)
			            .setMaxStreams(20)
			            .build();
			}
			else{
				rhythmPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
			}
			rhythmPool.setOnLoadCompleteListener(this);
			rhythmMap = new SparseIntArray(10);
//			rhythmMap.put(RHYTHM_ID_KICK, rhythmPool.load(context, R.raw.practice_fx_kick, 1));
//			rhythmMap.put(RHYTHM_ID_HAT_CLOSED, rhythmPool.load(context, R.raw.practice_fx_hat_close, 1));
//			rhythmMap.put(RHYTHM_ID_SNARE, rhythmPool.load(this.getContext(), R.raw.practice_fx_snare, 1));
//			rhythmMap.put(RHYTHM_ID_HAT_CLOSED, rhythmPool.load(context, R.raw.practice_fx_hat_close, 1));
			rhythmThread=new RhythmThread(2060,198);
		}
	}
	@Override
	public void start(){
		super.start();
		endTime_s=PatternMaker.getMaxTime(noteState.getNotes())+3;
		x.task().run(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(!(updatorPrepared&&soundPoolPrepared&&rhythmPoolPrepared));
				/*
				 * to make sure the soundPool is playing all the time,
				 * avoid the "first time delay":
				 */
				soundPool.play(soundMap.get(EFFECT_ID_PERFECT), 0f, 0f, 1, -1, 1);
				if(time_running_ms==0){
					fingerEventHandler.start();
					showAssessmentThread.start();
					updator.start();
//					rhythmThread.start();
				}
				double endTime_ms=endTime_s*1000;
//				while(time_running_ms<=endTime_ms);
				/*
				 * Don't use while loop!!!
				 * the loop will never end when the time_running_ms is not volatile!
				 */
				x.task().postDelayed(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						mainActivity.handler.sendEmptyMessage(Constant.Message.GotoView.VIEW_ASSESSMENT);
					}
					
				},(long) endTime_ms);
			}
			
		});
	}
	@Override
	public void quit(){
		boolean retry=true;
		rhythmThread.isRunning=false;
		rhythmPool.release();
		while(retry){
			try {
				rhythmThread.join();
				retry=false;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		super.quit();
	}
	@Override
	public void onLoadComplete(SoundPool arg0, int sampleId, int status) {
		// TODO Auto-generated method stub
		super.onLoadComplete(arg0, sampleId, status);
		rhythmPoolPrepared=true;
	}
	private class RhythmThread extends Thread{
		public boolean isRunning=true;
		public double start_ms;
		public double bpm;
		private double inv_bpm;
		private Handler rhythm_handler;
		private int[] rhythmArray={
				RHYTHM_ID_KICK,
				RHYTHM_ID_HAT_CLOSED,
				RHYTHM_ID_SNARE,
				RHYTHM_ID_HAT_CLOSED
		};
		private float[] rhythmVolumeArray={
				0.8f,
				0.4f,
				0.8f,
				0.4f
		};
		private int currentRhythmIndex=0;
		
		public RhythmThread(long start_ms,double bpm){
			rhythm_handler=new Handler();
			this.start_ms=start_ms;
			this.bpm=bpm;
			inv_bpm=60000/bpm;
//			rhythm_handler.postDelayed(this, start_ms-(long)time_running_ms);
		}
		@Override
		public void run(){
			while(isRunning){
				if(time_running_ms>=start_ms){
					rhythmPool.play(rhythmMap.get(rhythmArray[currentRhythmIndex]),
							rhythmVolumeArray[currentRhythmIndex],
							rhythmVolumeArray[currentRhythmIndex], 1, 0, 1);
					currentRhythmIndex++;
					if(currentRhythmIndex==4){
						currentRhythmIndex=0;
					}
					start_ms+=inv_bpm;
				}
			}
//			rhythm_handler.postDelayed(this, inv_bpm);
		}
	}
}
