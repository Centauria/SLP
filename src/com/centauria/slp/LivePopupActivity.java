package com.centauria.slp;

import java.util.Map;

import org.xutils.x;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import com.centauria.slp.data.Constant;
import com.centauria.slp.data.Setting;
import com.centauria.slp.util.DataProcess;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class LivePopupActivity extends Activity {
	@ViewInject(R.id.layout_live_song_configuration_imageView)
	private ImageView imageView;
	@ViewInject(R.id.layout_live_song_configuration_titleTextView)
	private TextView titleTextView;
	@ViewInject(R.id.layout_live_song_configuration_difficultyTextView)
	private TextView difficultyTextView;
	@ViewInject(R.id.layout_live_song_configuration_maxComboTextView)
	private TextView maxComboTextView;
	@ViewInject(R.id.layout_live_song_configuration_checkBox1)
	private CheckBox cb_change_tempo;
	@ViewInject(R.id.layout_live_song_configuration_checkBox2)
	private CheckBox cb_play_bgm;
	@ViewInject(R.id.layout_live_song_configuration_seekBar1)
	private SeekBar sb_ratio;
	@ViewInject(R.id.layout_live_song_configuration_editText)
	private EditText edit_ratio;
	@ViewInject(R.id.layout_live_song_configuration_bt_homeButton)
	private Button btn_ok;
	@ViewInject(R.id.activity_live_popup_bt_close)
	private Button btn_close;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_live_popup);
		x.view().inject(this);
		Intent intent=this.getIntent();
		String jstr=intent.getExtras().getString("info");
		Map<String,Object> songinfo=DataProcess.json2map(jstr);
		ImageOptions options=new ImageOptions.Builder()
				.setIgnoreGif(false)
				.setImageScaleType(ScaleType.FIT_CENTER)
				.setFadeIn(true)
				.setLoadingDrawableId(R.drawable.love_live__school_idol_project_eng)
				.setFailureDrawableId(R.drawable.love_live__school_idol_project)
				.build();
		x.image().bind(imageView, Constant.ADDRESS_LIST[Setting.activeLinkIndex]+songinfo.get("live_icon_asset"),options);
//		x.image().bind(titleImageView, Constant.ADDRESS_LIST[Setting.activeLinkIndex]+songinfo.get("title_asset"),options);
		titleTextView.setText(songinfo.get("name").toString());
		difficultyTextView.setText(songinfo.get("stage_level_text").toString());
		maxComboTextView.setText(songinfo.get("max_combo_text").toString());
		sb_ratio.setEnabled(false);
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		this.setResult(Constant.Message.ActivityRequest.NEUTRAL);
		this.finish();
	}

	@Event(value=R.id.layout_live_song_configuration_bt_homeButton)
	private void okPressed(View view){
		String text=edit_ratio.getText().toString();
		if(text.length()==0){
			edit_ratio.setText(String.format("%1.2f", sb_ratio.getProgress()*0.05+0.5));
			return;
		}
		double ratio=Double.parseDouble(text);
		if(ratio>1.5){
			Toast.makeText(this, this.getString(R.string.warning_ratio_too_high), Toast.LENGTH_SHORT).show();
			edit_ratio.setText("1.5");
			ratio=1.5;
		}
		else if(ratio<0.5){
			Toast.makeText(this, this.getString(R.string.warning_ratio_too_low), Toast.LENGTH_SHORT).show();
			edit_ratio.setText("0.5");
			ratio=0.5;
		}
		sb_ratio.setProgress((int) ((ratio-0.5)*20));
		Intent intent=this.getIntent();
		intent.putExtra("live_type", "live");
		intent.putExtra("live_data_ratio", ratio);
		intent.putExtra("live_data_playbgm?", cb_play_bgm.isChecked());
		intent.putExtra("live_data_changetempo?", cb_change_tempo.isChecked());
		this.setResult(Constant.Message.ActivityRequest.OK,intent);
		this.finish();
	}
	
	@Event(value=R.id.activity_live_popup_bt_close)
	private void closePressed(View view){
		this.setResult(Constant.Message.ActivityRequest.CANCEL);
		this.finish();
	}
	
	@Event(value=R.id.layout_live_song_configuration_checkBox1,
			type=CompoundButton.OnCheckedChangeListener.class)
	private void tempo_change_cb_changed(CompoundButton buttonView, boolean isChecked){
		if(isChecked){
			cb_play_bgm.setChecked(false);
			cb_play_bgm.setEnabled(false);
			sb_ratio.setEnabled(true);
			edit_ratio.setEnabled(true);
		}
		else{
			cb_play_bgm.setEnabled(true);
			sb_ratio.setEnabled(false);
			edit_ratio.setEnabled(false);
		}
	}
	
	@Event(value=R.id.layout_live_song_configuration_checkBox2,
			type=CompoundButton.OnCheckedChangeListener.class)
	private void play_bgm_cb_changed(CompoundButton buttonView, boolean isChecked){
		if(isChecked){
			sb_ratio.setProgress(10);
		}
	}
	
	/*
	 * Here I only overwrote ONE method "onProgressChanged",
	 * the other 2 methods were not implemented,
	 * the LogUtil will send warnings when the other 2 methods were called.
	 */
	@Event(value=R.id.layout_live_song_configuration_seekBar1,
			type=OnSeekBarChangeListener.class,
			method="onProgressChanged")
	private void ratioChanged(SeekBar seekbar, int progress, boolean fromUser){
		edit_ratio.setText(String.format("%1.2f", progress*0.05+0.5));
	}
}
