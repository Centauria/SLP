package com.centauria.slp.type;

import com.centauria.slp.type.NoteType;

public class FingerEvent{
	public int line;
	public NoteType type;
	public double time;
	public FingerEvent(int line,NoteType type,double time){
		this.line=line;
		this.type=type;
		this.time=time;
	}
}