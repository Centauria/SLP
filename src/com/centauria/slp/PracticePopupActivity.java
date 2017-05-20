package com.centauria.slp;

import com.centauria.slp.data.Constant;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.view.View;
import android.view.View.OnClickListener;

public class PracticePopupActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_practice_popup);
		((Button) this.findViewById(R.id.activity_practice_popup_bt_close)).setOnClickListener(this);
		((Button) this.findViewById(R.id.window_popup_bt_homeButton)).setOnClickListener(this);
		NumberPicker np1=(NumberPicker) this.findViewById(R.id.live_practice_configuration_numberPicker1);
		NumberPicker np2=(NumberPicker) this.findViewById(R.id.live_practice_configuration_numberPicker2);
		np1.setMinValue(4);
		np1.setMaxValue(100);
		np1.setValue(16);
		np2.setMinValue(60);
		np2.setMaxValue(250);
		np2.setValue(120);
	}
	

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.activity_practice_popup_bt_close:
			this.setResult(Constant.Message.ActivityRequest.CANCEL);
			this.finish();
			break;
		case R.id.window_popup_bt_homeButton:
			Spinner sp1=(Spinner) this.findViewById(R.id.live_practice_configuration_spinner1);
			Spinner sp2=(Spinner) this.findViewById(R.id.live_practice_configuration_spinner2);
//			SeekBar sb=(SeekBar) this.findViewById(R.id.live_practice_configuration_seekBar1);
			NumberPicker np1=(NumberPicker) this.findViewById(R.id.live_practice_configuration_numberPicker1);
			NumberPicker np2=(NumberPicker) this.findViewById(R.id.live_practice_configuration_numberPicker2);
			Intent intent=this.getIntent();
			intent.putExtra("live_type", "practice");
			intent.putExtra("haku", sp1.getSelectedItemPosition());
			intent.putExtra("bars", np1.getValue());
			intent.putExtra("type", sp2.getSelectedItemPosition());
			intent.putExtra("BPM", (double)np2.getValue());
			this.setResult(Constant.Message.ActivityRequest.OK, intent);
			this.finish();
			break;
		}
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		this.setResult(Constant.Message.ActivityRequest.NEUTRAL);
		this.finish();
	}
}
