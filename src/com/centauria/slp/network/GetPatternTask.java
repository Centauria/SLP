package com.centauria.slp.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xutils.x;
import com.centauria.slp.data.Constant;
import com.centauria.slp.data.Setting;
import com.centauria.slp.type.NoteInJson;
import com.centauria.slp.util.DataProcess;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import android.support.annotation.Nullable;
import android.widget.Toast;

public class GetPatternTask {
	public volatile boolean done=false;
	public ArrayList<Map<String,Object>> list;
	
	private String sourceUrl;
	private String resultString;
	private ObjectMapper om;
	
	public GetPatternTask(@Nullable ArrayList<Map<String,Object>> list,String src) {
		om=new ObjectMapper();
		this.sourceUrl=Constant.ADDRESS_LIST[Setting.activeLinkIndex]+src;
		if(list==null){
			this.list=new ArrayList<Map<String,Object>>();
		}
		else{
			this.list=list;
		}
		// TODO Auto-generated constructor stub
	}

	public void get(){
		x.task().run(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Document doc;
				try {
					doc = Jsoup.connect(sourceUrl).get();
			        Pattern p=Pattern.compile("notes_list: \\[.*\\]");
			        Matcher m=p.matcher(doc.html());
			        if(m.find()){
			        	resultString=m.group(0).split("notes_list: ")[1];
			        	Map<String,Object>[] res=DataProcess.json2maps(resultString);
			    		for(Map<String,Object> map:res){
			    			list.add(map);
			    		}
			    		done=true;
			        }
			        else{
			        	x.task().post(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								Toast.makeText(x.app(), "get pattern failed", Toast.LENGTH_LONG).show();
							}
			        		
			        	});
			        }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					if(e instanceof HttpStatusException){
						x.task().post(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								Toast.makeText(x.app(), "get pattern failed", Toast.LENGTH_LONG).show();
							}
			        		
			        	});
					}
					e.printStackTrace();
				}
			}
			
		});
	}
	
	public NoteInJson[] getPattern(){
		NoteInJson[] res=null;
		try {
			res=om.readValue(this.resultString, NoteInJson[].class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
}
