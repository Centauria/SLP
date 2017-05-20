package com.centauria.slp.util;

import java.util.ArrayList;
import java.util.Random;

public class Randsel {
	public static Random r=new Random();
	public static Object randsel(Object a,Object... args){
		if(args.length==0) return a;
		else{
			int n=args.length+1;
			int s=r.nextInt(n);
			if(s==n-1) return a;
			else return args[s];
		}
	}
	public static Object randsel(Object[] args){
		return args[r.nextInt(args.length)];
	}
	public static Object randsel(ArrayList<Object> args){
		return args.get(r.nextInt(args.size()));
	}
	public static Object[] randsel(Object[] args,int num){
		if(num>args.length){
			num=args.length;
		}
		ArrayList<Object> ori=new ArrayList<Object>();
		ArrayList<Object> res;
		for(Object o:args){
			ori.add(o);
		}
		res=randsel(ori,num);
		return res.toArray();
	}
	/*
	 * WARNING: this method can change the source!
	 */
	public static ArrayList<Object> randsel(ArrayList<Object> source,int num){
		Object temp;
		ArrayList<Object> res=new ArrayList<Object>();
		for(int i=0;i<num;i++){
			temp=randsel(source);
			source.remove(temp);
			res.add(temp);
		}
		return res;
	}
}
