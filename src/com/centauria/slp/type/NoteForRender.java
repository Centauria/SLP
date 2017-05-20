package com.centauria.slp.type;

import com.centauria.slp.data.Constant;

public class NoteForRender extends NoteInJson {
	public static final int UNUSED=0;
	public static final int USING=1;
	public static final int USED=2;
	
	public boolean doubled;
	public int state;
	public int assessment;
	
	public NoteForRender(){
		
	}
	
	public NoteForRender(NoteInJson note,boolean doubled){
		this.doubled=doubled;
		this.state=UNUSED;
		this.assessment=Constant.Assessment.ASSESSMENT_UNDEFINED;
		this.timing_sec=note.timing_sec;
		this.notes_attribute=note.notes_attribute;
		this.notes_level=note.notes_level;
		this.effect=note.effect;
		this.effect_value=note.effect_value;
		this.position=9-note.position;      //CAUTION: position translation!
	}
}
