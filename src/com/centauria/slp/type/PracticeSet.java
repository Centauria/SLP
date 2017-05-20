package com.centauria.slp.type;

public class PracticeSet {
	public class SingleFingerEvent{
		public int line;
		public int cut_len;
		public SingleFingerEvent(int line,int len){
			this.line=line;
			cut_len=len;
		}
		public SingleFingerEvent(int[] line_and_len){
			line=line_and_len[0];
			if(line_and_len.length>1){
				cut_len=line_and_len[1];
			}
		}
	}
	private SingleFingerEvent[][] set;
	/*
	 * Structure of the set:
	 * set[total_cut_sum][total_finger_num];
	 * Structure of the data[][][]:
	 * data[total_cut_sum][2][2];
	 * data[cut][0][0]:first finger's line
	 * data[cut][0][1]:first finger's cut_len
	 * data[cut][1][0]:second finger's line
	 * data[cut][1][1]:second finger's cut_len
	 * the [cut] is in respond of the time that the finger should press down
	 */
	public PracticeSet(){
		set=new SingleFingerEvent[8][];
	}
	public PracticeSet(int length){
		set=new SingleFingerEvent[length][];
	}
	public PracticeSet(int[][][] data){
		set=new SingleFingerEvent[data.length][];
		for(int i=0;i<set.length;i++){
			if(data[i][0][0]!=-1){
				if(data[i][1][0]!=-1){
					set[i]=new SingleFingerEvent[2];
					set[i][0]=new SingleFingerEvent(data[i][0]);
					set[i][1]=new SingleFingerEvent(data[i][1]);
				}
				else{
					set[i]=new SingleFingerEvent[1];
					set[i][0]=new SingleFingerEvent(data[i][0]);
				}
			}
		}
	}
}
