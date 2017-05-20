package com.centauria.slp.data;

public class Constant{
	public static class Message{
		public static class General{
			public static final int UNDEFINED=-1;
		}
		public static class GotoView{
			public static final int VIEW_HOME=10;
			public static final int VIEW_LIVE_PRAC=11;
			public static final int VIEW_LIVE_SONG=12;
			public static final int VIEW_ASSESSMENT=13;
		}
		public static class Mask{
			public static final int OFF=20;
			public static final int ON=21;
		}
		public static class ActivityRequest{
			public static final int OK=30;
			public static final int CANCEL=31;
			public static final int NEUTRAL=32;
		}
	}
	public static class View{
		public static final int VIEW_HOME=0;
		public static final int VIEW_LIVE_PRAC=1;
		public static final int VIEW_LIVE_SONG=2;
		public static final int VIEW_ASSESSMENT=3;
		public static final int VIEW_SETTING=4;
		public static final int VIEW_CHOOSESONG=5;
	}
	public static class Assessment{
		public static final int ASSESSMENT_PERFECT=0;
		public static final int ASSESSMENT_GREAT=1;
		public static final int ASSESSMENT_GOOD=2;
		public static final int ASSESSMENT_BAD=3;
		public static final int ASSESSMENT_MISS=4;
		public static final int ASSESSMENT_UNDEFINED=-1;
	}
	public static class Rhythm{
		public static final int RHYTHM_KICK=0;
		public static final int RHYTHM_HAT_CLOSED=1;
		public static final int RHYTHM_SNARE=2;
	}
	public static class SongID{
		public static final int SOLDIER_GAME=0;
		public static final int NO_BRAND_GIRLS=1;
	}
	public static final String[] DIFFICULTY_TEXT_LIST={
			"Easy",//"EASY"
			"Normal",//"NORMAL"
			"Hard",//"HARD"
			"Expert",//"EXPERT"
			"Challenge",
			"Master"//"MASTER"
	};
	public static final String[] ADDRESS_ASSET_LIST={
			"https://card.lovelivesupport.com/asset",
			"https://c.dash.moe/asset",
			"https://c.1994.io/asset",
			"http://card.llsif.moe/asset",
			"https://card.llsupport.cn/asset"
	};
	public static final String[] ADDRESS_LIST={
			"https://card.lovelivesupport.com",
			"https://c.dash.moe",
			"https://c.1994.io",
			"http://card.llsif.moe",
			"https://card.llsupport.cn"
	};
}