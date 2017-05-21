package com.centauria.slp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.xutils.x;
import org.xutils.common.util.LogUtil;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import com.centauria.slp.R;
import com.centauria.slp.data.Constant;
import com.centauria.slp.data.Setting;
import com.centauria.slp.data.Status;
import com.centauria.slp.network.GetPatternTask;
import com.centauria.slp.network.GetSongListTask;
import com.centauria.slp.network.GetSongTask;
import com.centauria.slp.type.NoteInJson;
import com.centauria.slp.util.DataProcess;
import com.centauria.slp.util.FileProcess;
import com.centauria.slp.util.PatternMaker;
import com.centauria.slp.util.ViewData;
import com.centauria.slp.view.LiveView;
import com.centauria.slp.view.PracticeLiveView;
import com.centauria.slp.view.SongLiveView;
import com.fasterxml.jackson.databind.ObjectMapper;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements OnClickListener{
	private final int DIALOG_LIVE_SETUP=0;
	
	private int currentView;
	private SongLiveView songLiveView;
	private PracticeLiveView practiceLiveView;
	private RelativeLayout innerFrame;
	private FrameLayout outerFrame;
	private View popupWindow=null;
	
	@ViewInject(R.id.layout_choose_song_radioGroup1)
	RadioGroup radiogroup;
	@ViewInject(R.id.layout_choose_song_listView1)
	ListView songlistview;
	
	private GetSongListTask getSongListTask=null;
	
	@SuppressLint("HandlerLeak")
	public Handler handler=new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
			case Constant.Message.GotoView.VIEW_HOME:
				gotoHomeView();
				break;
			case Constant.Message.GotoView.VIEW_LIVE_SONG:
				
				break;
			case Constant.Message.GotoView.VIEW_ASSESSMENT:
				switch(currentView){
				case Constant.View.VIEW_LIVE_PRAC:
					gotoAssessmentView(practiceLiveView);
					break;
				case Constant.View.VIEW_LIVE_SONG:
					gotoAssessmentView(songLiveView);
				}
				break;
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		Status.Screen.density=getResources().getDisplayMetrics().density;
		Status.Screen.width=getResources().getDisplayMetrics().widthPixels;
		Status.Screen.height=getResources().getDisplayMetrics().heightPixels;
		currentView=Constant.View.VIEW_HOME;
		gotoHomeView();
		setInnerFrame((RelativeLayout) findViewById(R.id.layout_home_relative));
		setOuterFrame((FrameLayout) findViewById(R.id.layout_home_frame));
		Setting.font=Typeface.createFromAsset(this.getAssets(), "fonts/MTLmr3m.ttf");
		this.loadSettings();
	}
	@Override
	public boolean onTouchEvent(MotionEvent event){
		
		return true;
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
			
		}
	}
	@Override
	public void onBackPressed(){
		switch(currentView){
		case Constant.View.VIEW_HOME:
			super.onBackPressed();
			break;
		case Constant.View.VIEW_SETTING:
			Setting.autoPlay=((CheckBox) this.findViewById(R.id.layout_global_settings_checkBox1)).isChecked();
			Setting.velocity=Float.parseFloat(((TextView) this.findViewById(R.id.layout_global_settings_textView1)).getText().toString());
			Setting.offset_slim=Float.parseFloat(((TextView) this.findViewById(R.id.layout_global_settings_textView2)).getText().toString());
			Setting.offset_rough=Float.parseFloat(((TextView) this.findViewById(R.id.layout_global_settings_textView3)).getText().toString());
			Setting.offset_ms=Setting.offset_rough+Setting.offset_slim;
			RadioGroup radiogroup=(RadioGroup) this.findViewById(R.id.layout_global_settings_radioGroup1);
			Setting.activeLinkIndex=radiogroup.indexOfChild(radiogroup.findViewById(radiogroup.getCheckedRadioButtonId()));
		case Constant.View.VIEW_LIVE_PRAC:
		case Constant.View.VIEW_LIVE_SONG:
		case Constant.View.VIEW_ASSESSMENT:
		case Constant.View.VIEW_CHOOSESONG:
			gotoHomeView();
			break;
		}
	}
	@Override
	protected void onPause(){
//		switch(currentView){
//		case Constant.View.VIEW_LIVE:
//			if(practiceLiveView!=null){
//				practiceLiveView.pause();
//			}
//			if(songLiveView!=null){
//				songLiveView.pause();
//			}
//			break;
//		}
		super.onPause();
	}
	@Override
	protected void onResume(){
		super.onResume();
//		switch(currentView){
//		case Constant.View.VIEW_LIVE:
//			if(practiceLiveView!=null){
//				practiceLiveView.resume();
//			}
//			if(songLiveView!=null){
//				songLiveView.resume();
//			}
//			break;
//		}
	}
	@Override
	protected void onStop(){
		this.saveSettings();
		super.onStop();
	}
	private void saveSettings(){
		SharedPreferences sp=this.getSharedPreferences("setting", Activity.MODE_PRIVATE);
		SharedPreferences.Editor e=sp.edit();
		e.putBoolean("first", false);
		e.putBoolean("autoPlay", Setting.autoPlay);
		e.putFloat("velocity", Setting.velocity);
		e.putFloat("offset", Setting.offset_ms);
		e.putFloat("offset_rough", Setting.offset_rough);
		e.putFloat("offset_slim", Setting.offset_slim);
		e.putInt("note_color", Setting.note_color);
		e.putInt("double_note_sign_color", Setting.double_note_sign_color);
		e.putInt("striptail_color", Setting.striptail_color);
		e.putInt("strip_color", Setting.strip_color);
		e.putInt("static_circle_color", Setting.static_circle_color);
		e.putLong("determination_bad_miss", Double.doubleToLongBits(Setting.determination_bad_miss));
		e.putLong("determination_good_bad", Double.doubleToLongBits(Setting.determination_good_bad));
		e.putLong("determination_great_good", Double.doubleToLongBits(Setting.determination_great_good));
		e.putLong("determination_perfect_great", Double.doubleToLongBits(Setting.determination_perfect_great));
		e.putInt("current_active_link_index", Setting.activeLinkIndex);
		e.commit();
	}
	private void loadSettings(){
		SharedPreferences sp=this.getSharedPreferences("setting", Activity.MODE_PRIVATE);
		if(sp.getBoolean("first", true)){
			this.saveSettings();
		}
		Setting.autoPlay=sp.getBoolean("autoPlay", false);
		Setting.velocity=sp.getFloat("velocity", 10);
		Setting.offset_ms=sp.getFloat("offset", 0);
		Setting.offset_rough=sp.getFloat("offset_rough", 0);
		Setting.offset_slim=sp.getFloat("offset_slim", 0);
		Setting.note_color=sp.getInt("note_color", 0);
		Setting.double_note_sign_color=sp.getInt("double_note_sign_color", 0);
		Setting.striptail_color=sp.getInt("striptail_color", 0);
		Setting.strip_color=sp.getInt("strip_color", 0);
		Setting.static_circle_color=sp.getInt("static_circle_color", 0);
		Setting.determination_bad_miss=Double.longBitsToDouble(sp.getLong("determination_bad_miss", 0));
		Setting.determination_good_bad=Double.longBitsToDouble(sp.getLong("determination_good_bad", 0));
		Setting.determination_great_good=Double.longBitsToDouble(sp.getLong("determination_great_good", 0));
		Setting.determination_perfect_great=Double.longBitsToDouble(sp.getLong("determination_perfect_great", 0));
		Setting.activeLinkIndex=sp.getInt("current_active_link_index", 0);
	}
	private View getCurrentView(){
		switch(currentView){
		case Constant.View.VIEW_HOME:
			return this.getLayoutInflater().inflate(R.layout.layout_home, getInnerFrame());
//			return homeView;
		case Constant.View.VIEW_LIVE_PRAC:
			return practiceLiveView;
		case Constant.View.VIEW_LIVE_SONG:
			return songLiveView;
		}
		return null;
	}
	private void gotoLiveView(NoteInJson[] notes,File musicFile){
		songLiveView=new SongLiveView(this);
		songLiveView.setMap(notes);
		songLiveView.setSong(musicFile);
		setContentView(songLiveView);
		songLiveView.start();
		currentView=Constant.View.VIEW_LIVE_SONG;
	}
	private void gotoPractice(NoteInJson[] notes){
		practiceLiveView=new PracticeLiveView(this);
		setContentView(practiceLiveView);
		practiceLiveView.setMap(notes);
		practiceLiveView.start();
		currentView=Constant.View.VIEW_LIVE_PRAC;
	}
	private void gotoPractice(int haku, int bars, int type, double bpm){
		switch(currentView){
		case Constant.View.VIEW_HOME:
			break;
		}
		String cachePath=FileProcess.getDiskCacheDir(this);
		String pattern_cachePath=cachePath+File.separator+"temp_pattern";
		String fileName=System.currentTimeMillis()+"";
		try {
			File cacheDir=new File(pattern_cachePath);
			if(!cacheDir.exists()){
				cacheDir.mkdir();
			}
			File outFile=new File(pattern_cachePath,fileName+".json");
			ObjectMapper om=new ObjectMapper();
			if(outFile.createNewFile()){
				FileOutputStream out=new FileOutputStream(outFile);
				switch(haku){
				case Constant.Message.General.UNDEFINED:
					Log.e("error", "UNDEFINED");
					break;
				case 0:
					NoteInJson[] res=null;
					if(type==0){
						res=PatternMaker.makePattern(PatternMaker.TYPE_S, bpm, bars, 3);
					}
					else if(type==1){
						res=PatternMaker.makePattern(PatternMaker.TYPE_D, bpm, bars, 3);
					}
					else if(type==2){
						res=PatternMaker.makePattern(PatternMaker.TYPE_SD, bpm, bars, 3);
					}
					else{
						Toast.makeText(this, this.getString(R.string.next_version), Toast.LENGTH_SHORT).show();
						break;
					}
					om.writeValue(out, res);
					break;
				default:
					Toast.makeText(this, R.string.next_version, Toast.LENGTH_SHORT).show();
					break;
				}
				out.close();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		practiceLiveView=new PracticeLiveView(this);
		setContentView(practiceLiveView);
		practiceLiveView.setMap(PatternMaker.FROM_CACHE,fileName);
		practiceLiveView.start();
		currentView=Constant.View.VIEW_LIVE_PRAC;
	}
	private void gotoHomeView(){
		switch(currentView){
		case Constant.View.VIEW_LIVE_PRAC:
			if(practiceLiveView!=null){
				practiceLiveView.quit();
				practiceLiveView=null;
			}
			System.gc();
			break;
		case Constant.View.VIEW_LIVE_SONG:
			if(songLiveView!=null){
				songLiveView.quit();
				songLiveView=null;
			}
			break;
		}
		setContentView(R.layout.layout_home);

		Button home=(Button) findViewById(R.id.layout_home_homeButton);
		home.setOnClickListener(this);
		Button live=(Button) findViewById(R.id.layout_home_liveButton);
		live.setOnClickListener(this);
		Button set=(Button) findViewById(R.id.layout_home_settingsButton);
		set.setOnClickListener(this);
		
		currentView=Constant.View.VIEW_HOME;
	}
	private void gotoAssessmentView(LiveView liveView){
		liveView.quit();
		int[] result=liveView.getResult();
		int max_combo=liveView.getMaxCombo();
		setContentView(R.layout.layout_assessment);
		((TextView)findViewById(R.id.layout_assessment_perfectView)).setText(result[Constant.Assessment.ASSESSMENT_PERFECT]+"");
		((TextView)findViewById(R.id.layout_assessment_greatView)).setText(result[Constant.Assessment.ASSESSMENT_GREAT]+"");
		((TextView)findViewById(R.id.layout_assessment_goodView)).setText(result[Constant.Assessment.ASSESSMENT_GOOD]+"");
		((TextView)findViewById(R.id.layout_assessment_badView)).setText(result[Constant.Assessment.ASSESSMENT_BAD]+"");
		((TextView)findViewById(R.id.layout_assessment_missView)).setText(result[Constant.Assessment.ASSESSMENT_MISS]+"");
		((TextView)findViewById(R.id.layout_assessment_comboView)).setText(max_combo+"");
		liveView=null;
		System.gc();
		currentView=Constant.View.VIEW_ASSESSMENT;
	}
	private void gotoChooseSongView(){
		setContentView(R.layout.layout_choose_song);
		x.view().inject(this);
//		getSongListTask=new GetSongListTask(null);
//		getSongListTask.execute(1000);
		getSongListTask=new GetSongListTask(null);
		getSongListTask.get();
		x.task().run(new Runnable(){
			@Override
			public void run(){
				while(!getSongListTask.done);
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						ViewData.refreshSongListViewFromData(MainActivity.this, getSongListTask.list, songlistview, radiogroup);
					}
					
				});
			}
		});
		currentView=Constant.View.VIEW_CHOOSESONG;
	}
	private void gotoSettingView(){
		this.setContentView(R.layout.layout_global_settings);
		((CheckBox) this.findViewById(R.id.layout_global_settings_checkBox1)).setChecked(Setting.autoPlay);
		SeekBar sb=(SeekBar) this.findViewById(R.id.layout_global_settings_seekBar1);
		sb.setProgress((int) (Setting.velocity*200-20));
		final TextView tv=(TextView) this.findViewById(R.id.layout_global_settings_textView1);
		tv.setText(String.format("%1.2f", Setting.velocity));
		sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar seekbar, int progress, boolean arg2) {
				// TODO Auto-generated method stub
				tv.setText(String.format("%1.2f", ((float)progress+20)/200));
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		sb=(SeekBar) this.findViewById(R.id.layout_global_settings_seekBar2);
		sb.setProgress((int) (2.5*(Setting.offset_slim+100)));
		final TextView tv2=(TextView) this.findViewById(R.id.layout_global_settings_textView2);
		tv2.setText(String.format("%.1f", Setting.offset_slim));
		sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar seekbar, int progress, boolean arg2) {
				// TODO Auto-generated method stub
				tv2.setText(String.format("%.1f", 0.4*progress-100));
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		sb=(SeekBar) this.findViewById(R.id.layout_global_settings_seekBar3);
		
		sb.setProgress((int) ((Setting.offset_rough+1000)/200));
		final TextView tv3=(TextView) this.findViewById(R.id.layout_global_settings_textView3);
		tv3.setText(String.format("%.0f", Setting.offset_rough));
		sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar seekbar, int progress, boolean arg2) {
				// TODO Auto-generated method stub
				tv3.setText(String.format("%.0f", 200*progress-1000.));
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		RadioGroup radiogroup=(RadioGroup) this.findViewById(R.id.layout_global_settings_radioGroup1);
		((RadioButton) radiogroup.getChildAt(Setting.activeLinkIndex)).setChecked(true);
		radiogroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				// TODO Auto-generated method stub
				final int index=arg0.indexOfChild(arg0.findViewById(arg1));
				x.task().post(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						Toast.makeText(x.app(), Constant.ADDRESS_LIST[index], Toast.LENGTH_SHORT).show();
					}
					
				});
			}
			
		});
		this.currentView=Constant.View.VIEW_SETTING;
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.layout_home_homeButton:
//			gotoLiveView(Constant.SongID.NO_BRAND_GIRLS);
			Intent i=new Intent(this,PracticePopupActivity.class);
			this.startActivityForResult(i, DIALOG_LIVE_SETUP);
			break;
		case R.id.layout_home_liveButton:
			this.gotoChooseSongView();
			break;
		case R.id.layout_home_settingsButton:
			this.gotoSettingView();
			break;
		case R.id.activity_practice_popup_bt_close:
			getInnerFrame().removeView(popupWindow);
			popupWindow=null;
			break;
		}
	}
	public int dp2px(int padding_in_dp){
		return (int)(padding_in_dp*Status.Screen.density+0.5f);
	}
	/*
	@Override
	protected Dialog onCreateDialog(int id){
		switch(id){
		case DIALOG_LIVE_SETUP:
			return new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher)
					.setTitle(R.string.live_setup).setView(MainActivity.this.getLayoutInflater().inflate(R.layout.layout_live_practice_configuration, null))
					.setPositiveButton(R.string.go, 
							new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							String[] ss=MainActivity.this.fileList();
							for(String s:ss){
								Log.i("files", s);
							}
						}
					})
					.setNegativeButton(R.string.cancel, 
							new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							MainActivity.this.deleteFile("config.json");
						}
					}).create();
		}
		return null;
	}
	*/
	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data){
		switch(requestCode){
		case DIALOG_LIVE_SETUP:
			switch(resultCode){
			case Constant.Message.ActivityRequest.OK:
				if(data.getExtras().getString("live_type").equals("practice")){
					int haku=data.getIntExtra("haku", -1);
					int bars=data.getIntExtra("bars", -1);
					int type=data.getIntExtra("type", -1);
					double bpm=data.getDoubleExtra("BPM", -1);
					this.gotoPractice(haku,bars,type,bpm);
				}
				else if(data.getExtras().getString("live_type").equals("live")){
					final boolean play_bgm=data.getExtras().getBoolean("live_data_playbgm?");
					final boolean change_tempo=data.getExtras().getBoolean("live_data_changetempo?");
					final double ratio=data.getExtras().getDouble("live_data_ratio");
					Map<String,Object> map=DataProcess.json2map(data.getExtras().getString("info"));
//					final GetPatternTask gpt=new GetPatternTask(null,map.get("notes_setting_asset").toString());
//					gpt.execute(1000);
					final GetPatternTask gpt=new GetPatternTask(null,map.get("notes_setting_asset").toString());
					gpt.get();
					if(play_bgm){
						String sound_asset=map.get("sound_asset").toString();
						File sound_asset_file=new File(sound_asset);
						File sound_cacheDir=new File(FileProcess.getDiskCacheDir(this),"temp_music");
						if(!sound_cacheDir.exists()){
							sound_cacheDir.mkdir();
						}
						final File sound_cache=new File(sound_cacheDir,sound_asset_file.getName());
						if(sound_cache.exists()){
							x.task().run(new Runnable(){

								@Override
								public void run() {
									// TODO Auto-generated method stub
									
									while(!gpt.done);
									x.task().post(new Runnable(){

										@Override
										public void run() {
											// TODO Auto-generated method stub
											NoteInJson[] pattern=gpt.getPattern();
											gotoLiveView(pattern,sound_cache);
										}
										
									});
								}
								
							});
						}
						else{
							ProgressDialog pd=new ProgressDialog(this);
							final GetSongTask gst=new GetSongTask(Constant.ADDRESS_LIST[Setting.activeLinkIndex]+sound_asset,
									sound_cache.getPath(),
									pd);
							gst.execute(1000);
							x.task().run(new Runnable(){

								@Override
								public void run() {
									// TODO Auto-generated method stub
									while(!(gst.done&&gpt.done));
									x.task().post(new Runnable(){

										@Override
										public void run() {
											// TODO Auto-generated method stub
											NoteInJson[] pattern=gpt.getPattern();
											gotoLiveView(pattern,gst.resultFile);
										}
										
									});
								}
								
							});
						}
						
					}
					else{
						x.task().run(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								while(!gpt.done);
								x.task().post(new Runnable(){

									@Override
									public void run() {
										// TODO Auto-generated method stub
										NoteInJson[] pattern=gpt.getPattern();
										if(change_tempo){
											PatternMaker.changeSpeed(pattern, ratio);
										}
										gotoPractice(pattern);
									}
									
								});
							}
							
						});
					}
				}
				break;
			case Constant.Message.ActivityRequest.CANCEL:
				Log.i("OK", "CANCEL");
				break;
			case Constant.Message.ActivityRequest.NEUTRAL:
				Log.i("OK", "NEUTRAL");
				break;
			}
			break;
		}
	}
	@Event(value = R.id.layout_choose_song_radioGroup1,type=RadioGroup.OnCheckedChangeListener.class)
	private void onDifficultyRadioGroupPressed(RadioGroup group,int checkedId){
		if(this.getSongListTask!=null){
			ViewData.refreshSongListViewFromData(MainActivity.this, getSongListTask.list, songlistview, radiogroup);
		}
	}
	@Event(value=R.id.layout_choose_song_listView1,type=OnItemClickListener.class)
	private void onSongListItemClicked(AdapterView<?> parent, View view, int position, long id){
//		Intent i=new Intent(this,LivePopupActivity.class);
		Intent i=new Intent("android.intent.action.live_popup");
		Object map=parent.getItemAtPosition(position);
		if(map instanceof Map<?,?>){
			i.putExtra("info", DataProcess.map2json((Map<String, Object>) map));
		}
		LogUtil.i(map.toString());
		this.startActivityForResult(i, DIALOG_LIVE_SETUP);
	}
	/**
	 * @return the innerFrame
	 */
	public RelativeLayout getInnerFrame() {
		return innerFrame;
	}
	/**
	 * @param innerFrame the innerFrame to set
	 */
	public void setInnerFrame(RelativeLayout innerFrame) {
		this.innerFrame = innerFrame;
	}
	/**
	 * @return the outerFrame
	 */
	public FrameLayout getOuterFrame() {
		return outerFrame;
	}
	/**
	 * @param outerFrame the outerFrame to set
	 */
	public void setOuterFrame(FrameLayout outerFrame) {
		this.outerFrame = outerFrame;
	}
}