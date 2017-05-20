package com.centauria.slp.util;

import java.util.LinkedList;

import com.centauria.slp.data.Constant;
import com.centauria.slp.type.NoteInJson;

public class PatternMaker {
	public static final int TYPE_S=1;
	public static final int TYPE_D=2;
	public static final int TYPE_SD=3;
	public static final int TYPE_F=4;
	public static final int TYPE_Z=5;
	
	public static double single_time_limit=0.2;
	
	public static NoteInJson[] makePattern(int type,double bpm,int bars,double startTime_s){
		NoteInJson[] res=null;
		LinkedList<NoteInJson> list;
		NoteInJson temp;
		switch(type){
		case TYPE_S:
			double sixteenth_time=15/bpm;
			list=new LinkedList<NoteInJson>();
			for(int i=0;i<16*bars;i++){
				temp=new NoteInJson(startTime_s+i*sixteenth_time,1,1,NoteInJson.NOTE_NORMAL,2,randPos());
				list.add(temp);
			}
			res=list.toArray(new NoteInJson[0]);
			randomize_new(res);
			break;
		case TYPE_D:
			double eighth_time=30/bpm;
			list=new LinkedList<NoteInJson>();
			for(int i=0;i<16*bars;i++){
				int[] db=PatternMaker.randDouble();
				temp=new NoteInJson(startTime_s+i*eighth_time,1,1,NoteInJson.NOTE_NORMAL,2,db[0]);
				list.add(temp);
				temp=new NoteInJson(startTime_s+i*eighth_time,1,1,NoteInJson.NOTE_NORMAL,2,db[1]);
				list.add(temp);
			}
			res=list.toArray(new NoteInJson[0]);
			break;
		case TYPE_SD:
			double eight_time=30/bpm;
			list=new LinkedList<NoteInJson>();
			for(int i=0;i<16*bars;i++){
				if(i%2==0){
					int[] db=PatternMaker.randDouble();
					temp=new NoteInJson(startTime_s+i*eight_time,1,1,NoteInJson.NOTE_NORMAL,2,db[0]);
					list.add(temp);
					temp=new NoteInJson(startTime_s+i*eight_time,1,1,NoteInJson.NOTE_NORMAL,2,db[1]);
					list.add(temp);
				}
				else{
					temp=new NoteInJson(startTime_s+i*eight_time,1,1,NoteInJson.NOTE_NORMAL,2,randPos());
					list.add(temp);
				}
			}
			res=list.toArray(new NoteInJson[0]);
			break;
		case TYPE_F:
			
			break;
		case TYPE_Z:
			
			break;
		}
		return res;
	}
	public static void rdmz(NoteInJson note){
		note.position=randPos();
	}
	public static void randomize_new(NoteInJson[] input){
		boolean id[]=PatternMaker.getDoubled(input);
		for(int i=1;i<input.length;i++){
			double delta_t=input[i].timing_sec-input[i-1].timing_sec;
			if(delta_t==0){
				int[] db=PatternMaker.randDouble();
				input[i-1].position=db[0];
				input[i].position=db[1];
			}
			else if(delta_t>0&&delta_t<PatternMaker.single_time_limit){
				if(id[i-1]){
					input[i].position=PatternMaker.randPos();
				}
				else{
					input[i].position=PatternMaker.randPosDiffSide(input[i-1].position);
				}
			}
			else if(delta_t>=PatternMaker.single_time_limit){
				input[i].position=PatternMaker.randPos();
			}
		}
	}
	public static void mirror_1_9(NoteInJson[] input){
		for(NoteInJson n:input){
			n.position=10-n.position;
		}
	}
	public static void mirror_1_4(NoteInJson[] input){
		for(NoteInJson n:input){
			if(n.position<5){
				n.position=5-n.position;
			}
			else if(n.position>5){
				n.position=15-n.position;
			}
		}
	}
	public static void rotate(NoteInJson[] input,int step){
		for(NoteInJson n:input){
			n.position+=step;
			while(n.position>9){
				n.position-=9;
			}
			while(n.position<1){
				n.position+=9;
			}
		}
	}
	public static void changeSpeed(NoteInJson[] input,double ratio){
		for(NoteInJson note:input){
			note.timing_sec/=ratio;
			if(note.effect==3){
				note.effect_value/=ratio;
			}
		}
	}
	public static double difficulty(NoteInJson[] input){
		
		return 0;
	}
	public static int r_j(int in){
		return 9-in;
	}
	public static int randPos(){
		return 9-Randsel.r.nextInt(9);
	}
	public static int randPosSameSide(int pos){
		if(pos<5&&pos>0){
			return Randsel.r.nextInt(4)+1;
		}
		else if(pos>5&&pos<10){
			return 9-Randsel.r.nextInt(4);
		}
		else if(pos==5){
			return 9-Randsel.r.nextInt(9);
		}
		else{
			return Constant.Message.General.UNDEFINED;
		}
	}
	public static int randPosDiffSide(int pos){
		if(pos<5&&pos>0){
			return 9-Randsel.r.nextInt(4);
		}
		else if(pos>5&&pos<10){
			return Randsel.r.nextInt(4)+1;
		}
		else if(pos==5){
			return 9-Randsel.r.nextInt(9);
		}
		else{
			return Constant.Message.General.UNDEFINED;
		}
	}
	public static int[] randSingle(){
		int l=randPos();
		int[] res={l};
		return res;
	}
	public static int[] randDouble(){
		int l=PatternMaker.randPos();
		int r;
		do{
			r=PatternMaker.randPosDiffSide(l);
		}while(r==l);
		int[] res={l,r};
		return res;
	}
	public static boolean[] getDoubled(NoteInJson[] input){
		int len=input.length;
		double[] times=new double[len];
		boolean[] isDouble=new boolean[len];
		for(int i=0;i<len;i++){
			times[i]=input[i].timing_sec;
		}
		for(int i=0;i<len-1;i++){
			if(times[i]==times[i+1]){
				isDouble[i]=true;
				isDouble[i+1]=true;
			}
			else{
				isDouble[i+1]=false;
			}
		}
		return isDouble;
	}
	public static double getMaxTime(NoteInJson[] input){
		double res=0;
		for(NoteInJson n:input){
			if(n.effect==3){
				if(n.timing_sec+n.effect_value>res){
					res=n.timing_sec+n.effect_value;
				}
			}
			else{
				if(n.timing_sec>res){
					res=n.timing_sec;
				}
			}
		}
		return res;
	}
	public static final int FROM_ASSET=1;
	public static final int FROM_CACHE=2;
}
