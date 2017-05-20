package com.centauria.slp.type;

public class SongInJson {
	public String difficulty_text;
	public String stage_level_text;
	public String max_combo_text;
	public String notes_setting_asset;
	public String name;
	public String live_icon_asset;
	public String sound_asset;
	public int member_category;
	public SongInJson() {
		// TODO Auto-generated constructor stub
	}
	public SongInJson(SongInJson old){
		this.difficulty_text=old.difficulty_text;
		this.live_icon_asset=old.live_icon_asset;
		this.max_combo_text=old.max_combo_text;
		this.member_category=old.member_category;
		this.name=old.name;
		this.notes_setting_asset=old.notes_setting_asset;
		this.sound_asset=old.sound_asset;
		this.stage_level_text=old.stage_level_text;
	}
}
