package com.centauria.slp.data;

import android.graphics.Color;
import android.graphics.Typeface;

public class Setting {
	/*
	 * The falling speed of notes.
	 */
	public static float velocity=1.6f;
	/*
	 * The pattern time subtracts the music time.
	 */
	public static float offset_ms=0;
	public static float offset_rough=0;
	public static float offset_slim=0;
	/*
	 * The fading speed of assessments.
	 */
	public static float assessmentFadingSpeed=0.0005f;
	/*
	 * All colors that can be set.
	 */
	public static int note_color=Color.argb(255, 88, 255, 88);
	public static int double_note_sign_color=Color.argb(255, 255, 255, 255);
	public static int striptail_color=Color.argb(230, 248, 244, 193);
	public static int strip_color=Color.argb(200, 255, 255, 255);
	public static int static_circle_color=Color.argb(255, 180, 180, 100);
	/*
	 * All note determination ranges can be set.
	 */
	public static double determination_bad_miss=0.25;			//0.25
	public static double determination_good_bad=0.19;			//0.15
	public static double determination_great_good=0.13;			//0.1
	public static double determination_perfect_great=0.08;		//0.05
	
	public static Typeface font;
	
	public static boolean autoPlay=false;
	//	public static int currentSongID=-1;

	public static int activeLinkIndex=0;
}
