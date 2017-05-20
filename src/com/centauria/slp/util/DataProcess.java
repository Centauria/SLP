package com.centauria.slp.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.xutils.common.util.LogUtil;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataProcess {
	public static Map<String,Object> json2map(String jsonString){
		Map<String,Object> map=null;
		ObjectMapper om=new ObjectMapper();
		try {
			map=om.readValue(jsonString, Map.class);
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
		return map;
	}
	public static String map2json(Map<String,Object> map){
		ObjectMapper om=new ObjectMapper();
		String json="";
		try {
			json = om.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
	public static Map<String,Object>[] json2maps(String jsonString){
		Map<String,Object>[] map=null;
		ObjectMapper om=new ObjectMapper();
		try {
			map=om.readValue(jsonString, Map[].class);
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
		return map;
	}
	public static String maps2json(Map<String,Object>[] map){
		ObjectMapper om=new ObjectMapper();
		String json="";
		try {
			json = om.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
	public static ArrayList<Map<String,Object>> filterSongList(ArrayList<Map<String,Object>> list,String difficulty_text){
		ArrayList<Map<String,Object>> res=new ArrayList<Map<String,Object>>();
		for(Map<String,Object> map:list){
			if(difficulty_text.equals(map.get("difficulty_text"))){
				res.add(new HashMap<String,Object>(map));
			}
		}
		LogUtil.i("filtered: "+res.size()+" items");
		return res;
	}
}
