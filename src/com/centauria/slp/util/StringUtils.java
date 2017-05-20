package com.centauria.slp.util;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class StringUtils {
	public static String matchCharset(String content) {  
		String chs = "gb2312";  
		Pattern p = Pattern.compile("(?<=charset=)(.+)(?=\")");  
		Matcher m = p.matcher(content);  
		if (m.find())
		    return m.group();
		return chs;
	}  
	public static String getCharset(Document doc){  
		Elements eles = doc.select("meta[http-equiv=Content-Type]");  
		Iterator<Element> itor = eles.iterator();  
		while (itor.hasNext())   
		    return StringUtils.matchCharset(itor.next().toString());  
		return "utf-8";
	}  
	public static String nbsp(String s,String c){
		return s.replace(Jsoup.parse("&nbsp;").text(), c);
	}
}
