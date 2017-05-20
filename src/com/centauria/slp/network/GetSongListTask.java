package com.centauria.slp.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xutils.x;

import com.centauria.slp.util.StringUtils;
import android.support.annotation.Nullable;

public class GetSongListTask {
	public volatile boolean done=false;
	public ArrayList<Map<String,Object>> list;
	public GetSongListTask(@Nullable ArrayList<Map<String,Object>> list) {
		// TODO Auto-generated constructor stub
		if(list==null){
			this.list=new ArrayList<Map<String,Object>>();
		}
		else{
			this.list=list;
		}
	}

	public void get(){
		x.task().run(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				HashMap<String,Object> smap=new HashMap<String,Object>();
				Document doc;
				try {
					/*
					 * There are only 2 sites support the download of bgm.
					 * link 2 & link 4
					 * so here just use card.llsif.moe
					 * not use user setting.
					 */
					doc = Jsoup.connect("http://card.llsif.moe/live").get();
			        Elements ele=doc.getElementsByAttributeValue("id", "main");
			        Elements songs=ele.get(0).children().get(0).children();
			        for(Element song:songs){
			        	if(song.attr("id")=="do-not-hide-me"){
			        		continue;
			        	}
			        	if(song.hasAttr("data-category")){
			        		if(song.children().size()==4){
				        		smap=new HashMap<String,Object>();
				        		smap.put("member_category",Integer.parseInt(song.attr("data-category")));
			        			smap.put("name", StringUtils.nbsp(song.child(0).text(), "---").split("---")[0]);
			        			smap.put("live_icon_asset",song.child(0).getElementsByTag("img").attr("data-src"));
			        			if(song.child(0)
			        					.getElementsByAttributeValueStarting("href", "/asset/assets/sound/music/").size()!=0){
			        				smap.put("sound_asset",song.child(0)
			        					.getElementsByAttributeValueStarting("href", "/asset/assets/sound/music/")
			        					.get(0).attr("href"));
			        			}
			        			else{
			        				smap.put("sound_asset","");
			        			}
			        			smap.put("difficulty_text",song.child(1).child(0).text());
			        			smap.put("notes_setting_asset",song.child(1).child(0).attr("href"));
			        			smap.put("stage_level_text",StringUtils.nbsp(song.child(2).text(), ""));
			        			smap.put("max_combo_text",StringUtils.nbsp(song.child(3).text(), ""));
			        		}
			        		else{
			        			smap=new HashMap<String,Object>(smap);
			        			smap.put("difficulty_text",song.child(0).child(0).text());
			        			smap.put("notes_setting_asset",song.child(0).child(0).attr("href"));
			        			smap.put("stage_level_text",StringUtils.nbsp(song.child(1).text(), ""));
			        			smap.put("max_combo_text",StringUtils.nbsp(song.child(2).text(), ""));
			        		}
		        			list.add(smap);
			        	}
			        }
					done=true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
	}
}
