package com.centauria.slp.type;

public class NoteInJson {
	public static final int NOTE_NORMAL=1;
	public static final int NOTE_EVENT=2;
	public static final int NOTE_LONG=3;
	public static final int NOTE_STAR=4;
	public static final int NOTE_SWING_NORMAL=11;
	public static final int NOTE_SWING_LONG=13;
	
	public double timing_sec;
	public int notes_attribute;
	public int notes_level;
	public int effect;
	public double effect_value;
	public int position;
	
	public NoteInJson(){
		
	}
	
	public NoteInJson(double timing_sec, int notes_attribute, int notes_level, int effect, double effect_value,
			int position) {
		this.timing_sec = timing_sec;
		this.notes_attribute = notes_attribute;
		this.notes_level = notes_level;
		this.effect = effect;
		this.effect_value = effect_value;
		this.position = position;
	}
}
