package com.centauria.slp.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;

import org.xutils.common.util.LogUtil;

import com.centauria.slp.MainActivity;
import com.centauria.slp.R;
import com.centauria.slp.data.Constant;
import com.centauria.slp.data.Setting;
import com.centauria.slp.data.Status;
import com.centauria.slp.type.FingerEvent;
import com.centauria.slp.util.FileProcess;
import com.centauria.slp.util.PatternMaker;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.centauria.slp.type.NoteType;
import com.centauria.slp.type.NoteForRender;
import com.centauria.slp.type.NoteInJson;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import junit.framework.Assert;

public class LiveView extends SurfaceView
implements SurfaceHolder.Callback,
SoundPool.OnLoadCompleteListener{
	private int width,height;
	private LinkedList<FingerEvent> fingerEvents;
	
	public float velocity=Setting.velocity;
	private boolean autoBeat=Setting.autoPlay;
	protected boolean running=true;
	protected double time_running_ms=0;
	protected boolean updatorPrepared=false;
	protected boolean soundPoolPrepared=false;
	private int noteColor=Setting.note_color;
	private int doubleNoteSignColor=Setting.double_note_sign_color;
	private int striptailColor=Setting.striptail_color;
	private int stripColor=Setting.strip_color;
	private int staticCircleColor=Setting.static_circle_color;

	private final float CENTER_X=0.5f;			//divided by screenX
	private final float CENTER_Y=0.25f;			//divided by screenY
	private final float CENTER_R=0.625f;		//divided by screenY
	private final float ICON_R=0.1f;			//divided by screenY
	private final float NOTE_THICKNESS=0.8f;	//divided by noteR
	private final float STICK_THICKNESS=0.1f;	//divided by noteR
	private final float STICK_LENGTH=0.95f;		//divided by noteR
	private final float STRIP_ANGLE=(float) Math.asin(ICON_R/CENTER_R);
	private final int ASSESSMENT_PERFECT=Constant.Assessment.ASSESSMENT_PERFECT;
	private final int ASSESSMENT_GREAT=Constant.Assessment.ASSESSMENT_GREAT;
	private final int ASSESSMENT_GOOD=Constant.Assessment.ASSESSMENT_GOOD;
	private final int ASSESSMENT_BAD=Constant.Assessment.ASSESSMENT_BAD;
	private final int ASSESSMENT_MISS=Constant.Assessment.ASSESSMENT_MISS;
	private final int ASSESSMENT_UNDEFINED=Constant.Assessment.ASSESSMENT_UNDEFINED;
	protected final int EFFECT_ID_PERFECT=ASSESSMENT_PERFECT;
	protected final int EFFECT_ID_GREAT=ASSESSMENT_GREAT;
	protected final int EFFECT_ID_GOOD=ASSESSMENT_GOOD;
	protected final int EFFECT_ID_BAD=ASSESSMENT_BAD;
	protected final int EFFECT_ID_MISS=ASSESSMENT_MISS;
	private final int TYPE_SINGLE=1;
	private final int TYPE_DOUBLE=2;
	private final int TYPE_STRIPTAIL=3;
	private final String[] ASSESSMENTS={"PERFECT","GREAT","GOOD","BAD","MISS"};
	
	
	/*
	 * The meaning of velocity:
	 * velocity=1/(the elapsed time from a note appears to it arrives the perfect);
	 */
	protected MainActivity mainActivity;
	protected UpdateThread updator=null;
	protected FingerEventHandlerThread fingerEventHandler;
	protected ShowAssessmentThread showAssessmentThread;
	protected NoteInJson[] pattern;
	protected NoteState noteState;
	private ComboCounter comboCounter;
	private Paint paint;
	private Path path;
//	private Rect scrRect;
	protected SoundPool soundPool;
	protected SparseIntArray soundMap;
	/*
	 * Usage of LinkedList:
	 * LinkedList<Type> s;
	 * s.add(e);  //s=(s,e)
	 * s.push(e); //s=(e,s)
	 * e=s.pop(); //e=s[0],s=s-s[0]
	 * e=s.removeLast(); //e=s[-1],s=s-s[-1]
	 */
	/*
	 * Instructions on the line and position of notes:
	 * The line is an integer in 0~8;
	 * The position of a note is marked in a float number ranges usually in 0f~1.25f,
	 * which 0f means the note is in the middle of the screen and its radius is zero too,
	 * 1.0f means the note is just on the target area when you can touch it and get a "perfect",
	 * 1.25f means you have completely missed the note and it shouldn't be rendered on the screen anymore.
	 */
	public LiveView(Context context) {
		super(context);
		init(context);
	}
	public LiveView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	public LiveView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void init(Context context){
		width=-1;
		height=-1;
		getHolder().addCallback(this);
		paint=new Paint(Paint.ANTI_ALIAS_FLAG);
		fingerEvents=new LinkedList<FingerEvent>();
		fingerEventHandler=new FingerEventHandlerThread();
		showAssessmentThread=new ShowAssessmentThread();
		path=new Path();
//		scrRect=new Rect(0,0,Status.Screen.width,Status.Screen.height);
		if (!isInEditMode()) {
			mainActivity=(MainActivity)context;
			
			setBackgroundResource(R.drawable.live_bg);
			setZOrderOnTop(true);
			getHolder().setFormat(PixelFormat.TRANSLUCENT);
			if(Build.VERSION.SDK_INT>=21){
				AudioAttributes attr = new AudioAttributes.Builder()
			            .setUsage(AudioAttributes.USAGE_GAME)
			            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
			            .build();
				soundPool = new SoundPool.Builder()
			            .setAudioAttributes(attr)
			            .setMaxStreams(20)
			            .build();
			}
			else{
				soundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
			}
			soundPool.setOnLoadCompleteListener(this);
			soundMap = new SparseIntArray(10);
			soundMap.put(EFFECT_ID_PERFECT, soundPool.load(context, R.raw.se_306, 1));
			soundMap.put(EFFECT_ID_GREAT, soundPool.load(context, R.raw.se_307, 1));
			soundMap.put(EFFECT_ID_GOOD, soundPool.load(this.getContext(), R.raw.se_308, 1));
			soundMap.put(EFFECT_ID_BAD, soundPool.load(context, R.raw.se_309, 1));
			soundMap.put(EFFECT_ID_MISS, soundPool.load(context, R.raw.se_310, 1));
		}
		this.setKeepScreenOn(true);
		comboCounter=new ComboCounter();
	}
	public void setMap(NoteInJson[] notes){
		pattern=notes;
//		this.notes=this.renderNotes(pattern);
		this.noteState=new NoteState(this.renderNotes(pattern));
	}
	public void setMap(int source,String fileName){
		try {
//			File jsondir=mainActivity.getDir("maps", Context.MODE_PRIVATE);
////			jsondir=new File(mainActivity.getFilesDir().getAbsolutePath()+File.separator+"maps");
//			InputStream input=new FileInputStream(new File(jsondir,jsonFileName+".json"));
			InputStream input=null;
			if(source==PatternMaker.FROM_ASSET){
				input=mainActivity.getAssets().open("maps/latest/"+fileName+".json", Context.MODE_PRIVATE);
			}
			else if(source==PatternMaker.FROM_CACHE){
				File inputFile=new File(FileProcess.getDiskCacheDir(mainActivity)+File.separator+"temp_pattern",fileName+".json");
				input=new FileInputStream(inputFile);
			}
			ObjectMapper om=new ObjectMapper();
			JsonFactory jf=new JsonFactory();
			JsonParser jp=jf.createParser(input);
			pattern=om.readValue(jp,NoteInJson[].class);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		notes=this.renderNotes(pattern);
		this.noteState=new NoteState(this.renderNotes(pattern));
	}
	public void start(){
		
	}
	public void pause(){
		running=false;
	}
	public void resume(){
		running=true;
	}
	public void quit(){
		boolean retry=true;
		updator.isRunning=false;
		running=false;
		fingerEventHandler.isRunning=false;
		showAssessmentThread.isRunning=false;
		soundPool.release();
		while(retry){
			try{
				updator.join();
				fingerEventHandler.join();
				showAssessmentThread.join();
				retry=false;
			}
			catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}
	public int[] getResult(){
		return noteState.getAssessments();
	}
	public int getMaxCombo(){
		return comboCounter.getMax_combo_num();
	}
	@Override
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);
//		if(width==-1||height==-1){
//			width=getWidth();
//			height=getHeight();
//		}
//		drawAll(canvas);
		
	}
	@Override
	protected void onSizeChanged(int w,int h,int oldw,int oldh){
		super.onSizeChanged(w, h, oldw, oldh);
		width=w;
		height=h;
		Status.Screen.width=w;
		Status.Screen.height=h;
	}
	@Override
	public boolean onTouchEvent(MotionEvent event){
		super.onTouchEvent(event);
		if(!autoBeat){
			int actionIndex=event.getActionIndex();
			float x=event.getX(actionIndex);
			float y=event.getY(actionIndex);
			int l=getLine(x,y);
			switch(event.getAction()&MotionEvent.ACTION_MASK){
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
//				Log.i("finger down", l+"("+event.getX()+","+event.getY()+")");
				LogUtil.i("finger down: "+l+"("+event.getX()+","+event.getY()+")");
				if(l==-1){
					
				}
				else{
					synchronized(fingerEvents){
						fingerEvents.add(new FingerEvent(l,NoteType.press,time_running_ms/1000));
					}
					
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
//				Log.i("finger up", l+"("+event.getX()+","+event.getY()+")");
				LogUtil.i("finger up: "+l+"("+event.getX()+","+event.getY()+")");
				if(l==-1){
					
				}
				else{
					synchronized(fingerEvents){
						fingerEvents.add(new FingerEvent(l,NoteType.up,time_running_ms/1000));
					}
				}
				break;
			}
		}
		performClick();
		return true;
	}
	@Override
	public boolean performClick(){
		super.performClick();
		return true;
	}
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		updator=new UpdateThread(arg0);
		updatorPrepared=true;
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onLoadComplete(SoundPool arg0, int sampleId, int status) {
		// TODO Auto-generated method stub
		soundPoolPrepared=true;
//		arg0.play(sampleId, 0f, 0f, 1, -1, 1);
//		Log.i("LoadComplete", sampleId+"");
	}
	private NoteForRender[] renderNotes(NoteInJson[] pattern){
		int len=pattern.length;
		double offset_s=Setting.offset_ms/1000;
		NoteForRender[] res=new NoteForRender[len];
		boolean[] isDouble=PatternMaker.getDoubled(pattern);
		for(int i=0;i<len;i++){
			res[i]=new NoteForRender(pattern[i],isDouble[i]);
			res[i].timing_sec-=offset_s;
		}
		return res;
	}
	private synchronized void drawAll(Canvas canvas){
		if(canvas!=null){
			drawBackground(canvas);
			drawCombo(canvas,comboCounter.getCombo_num());
			for(int i=0;i<5;i++){
				drawAssessment(canvas,i);
			}
			double current_pos;
			
			for(int line=0;line<9;line++){
				for(int nn=0;nn<noteState.activeNotesIndex[line].length;nn++){
					int ind=noteState.getNthActiveNoteIndex(line,nn);
					if(ind!=-1){
						NoteForRender note=noteState.getNoteByIndex(ind);
						if(note.state==NoteForRender.UNUSED){
							current_pos=1-(note.timing_sec-this.time_running_ms/1000)*velocity;
							switch(note.effect){
							case NoteInJson.NOTE_EVENT:
							case NoteInJson.NOTE_STAR:
							case NoteInJson.NOTE_NORMAL:
								if(note.doubled){
									drawNote(canvas,note.position,current_pos,TYPE_DOUBLE);
								}
								else{
									drawNote(canvas,note.position,current_pos,TYPE_SINGLE);
								}
								break;
							case NoteInJson.NOTE_LONG:
								double current_strip_end_pos=1-(note.timing_sec+note.effect_value-this.time_running_ms/1000)*velocity;
								drawNote(canvas,note.position,current_strip_end_pos,TYPE_STRIPTAIL);
								drawStrip(canvas,note.position,current_pos,current_strip_end_pos);
								if(note.doubled){
									drawNote(canvas,note.position,current_pos,TYPE_DOUBLE);
								}
								else{
									drawNote(canvas,note.position,current_pos,TYPE_SINGLE);
								}
								break;
							default:
								
								break;
							}
						}
						else if(note.state==NoteForRender.USING){
							double current_strip_end_pos=1-(note.timing_sec+note.effect_value-this.time_running_ms/1000)*velocity;
							drawNote(canvas,note.position,current_strip_end_pos,TYPE_STRIPTAIL);
							drawStrip(canvas,note.position,1,current_strip_end_pos);
							if(note.doubled){
								drawNote(canvas,note.position,1,TYPE_DOUBLE);
							}
							else{
								drawNote(canvas,note.position,1,TYPE_SINGLE);
							}
						}
					}
				}
			}
		}
	}
	private void drawAssessment(Canvas canvas,int assessment){
		paint.setColor(Color.YELLOW);
		paint.setTextSize(width/20);
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setTypeface(Setting.font);
		if(showAssessmentThread.assessmentStatus[assessment]>0){
			paint.setAlpha((int) showAssessmentThread.assessmentStatus[assessment]);
			canvas.drawText(ASSESSMENTS[assessment], width/2, 9*height/20, paint);
		}
	}
	private void drawBackground(Canvas canvas){
//		bg.draw(canvas);
		
//		paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
//		canvas.drawPaint(paint);
//		paint.setXfermode(new PorterDuffXfermode(Mode.SRC));
		canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		canvas.drawARGB(170, 0, 0, 0);
		paint.setColor(staticCircleColor);
		paint.setStyle(Style.FILL);
		for(int line=0;line<9;line++){
			double theta=line*Math.PI/8;
			float noteX=(float) (width*CENTER_X-height*CENTER_R*Math.cos(theta));
			float noteY=(float) (height*(CENTER_Y+CENTER_R*Math.sin(theta)));
			float noteR=(float) (height*ICON_R);
			canvas.drawCircle(noteX, noteY, noteR, paint);
		}
	}
	private void drawCombo(Canvas canvas,int combo){
		paint.setColor(Color.WHITE);
		paint.setTextSize(width/30);
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setTypeface(Setting.font);
		if(combo!=0){
			canvas.drawText(combo+" combo", width/2, height/3, paint);
		}
	}
	private void drawNote(Canvas canvas,int line,double currentPosition,int type){
		if(currentPosition<=0){
			return;
		}
		double theta=line*Math.PI/8;
		float noteX=(float) (width*CENTER_X-height*currentPosition*CENTER_R*Math.cos(theta));
		float noteY=(float) (height*(CENTER_Y+currentPosition*CENTER_R*Math.sin(theta)));
		float noteR=(float) (height*ICON_R*currentPosition);
		paint.setStyle(Style.FILL);
		switch(type){
		case TYPE_DOUBLE:
			paint.setColor(doubleNoteSignColor);
			path.reset();
			float rectLeft=noteX-STICK_LENGTH*noteR;
			float rectRight=noteX+STICK_LENGTH*noteR;
			float rectUp=noteY-STICK_THICKNESS*noteR;
			float rectDown=noteY+STICK_THICKNESS*noteR;
			path.addRect(rectLeft, rectUp, rectRight, rectDown, Direction.CCW);
			canvas.drawPath(path, paint);
		case TYPE_SINGLE:
			paint.setColor(noteColor);
			path.reset();
			path.addCircle(noteX, noteY, NOTE_THICKNESS*noteR, Direction.CCW);
			path.addCircle(noteX, noteY, noteR, Direction.CW);
			canvas.drawPath(path, paint);
			break;
		case TYPE_STRIPTAIL:
			paint.setColor(striptailColor);
			canvas.drawCircle(noteX, noteY, noteR, paint);
			break;
		}
	}
	private void drawStrip(Canvas canvas,int line,double currentPosition,double currentPosition2){
		if(currentPosition>currentPosition2){
			double temp=currentPosition;
			currentPosition=currentPosition2;
			currentPosition2=temp;
		}
		if(currentPosition2<0){
			return;
		}
		if(currentPosition<0){
			currentPosition=0;
		}
		double theta=line*Math.PI/8;
		float noteX1=(float) (width*CENTER_X-height*currentPosition*CENTER_R*Math.cos(theta));
		float noteY1=(float) (height*(CENTER_Y+currentPosition*CENTER_R*Math.sin(theta)));
		float noteR1=(float) (height*ICON_R*currentPosition);
		float noteX2=(float) (width*CENTER_X-height*currentPosition2*CENTER_R*Math.cos(theta));
		float noteY2=(float) (height*(CENTER_Y+currentPosition2*CENTER_R*Math.sin(theta)));
		float noteR2=(float) (height*ICON_R*currentPosition2);
		float stripX1=(float) (width*CENTER_X-height*currentPosition*CENTER_R*Math.cos(theta-STRIP_ANGLE));
		float stripY1=(float) (height*(CENTER_Y+currentPosition*CENTER_R*Math.sin(theta-STRIP_ANGLE)));
		float stripX2=(float) (width*CENTER_X-height*currentPosition*CENTER_R*Math.cos(theta+STRIP_ANGLE));
		float stripY2=(float) (height*(CENTER_Y+currentPosition*CENTER_R*Math.sin(theta+STRIP_ANGLE)));
		float stripX3=(float) (width*CENTER_X-height*currentPosition2*CENTER_R*Math.cos(theta+STRIP_ANGLE));
		float stripY3=(float) (height*(CENTER_Y+currentPosition2*CENTER_R*Math.sin(theta+STRIP_ANGLE)));
		float stripX4=(float) (width*CENTER_X-height*currentPosition2*CENTER_R*Math.cos(theta-STRIP_ANGLE));
		float stripY4=(float) (height*(CENTER_Y+currentPosition2*CENTER_R*Math.sin(theta-STRIP_ANGLE)));
		paint.setStyle(Style.FILL);
		paint.setColor(stripColor);
		path.reset();
		path.moveTo(stripX1, stripY1);
		path.lineTo(stripX2, stripY2);
		path.lineTo(stripX3, stripY3);
		path.lineTo(stripX4, stripY4);
		path.close();
		path.addCircle(noteX1, noteY1, noteR1, Direction.CW);
		path.addCircle(noteX2, noteY2, noteR2, Direction.CW);
		canvas.drawPath(path, paint);
	}
	private void update(double time_ms){
		double time_s=time_ms/1000;
		double pos;
		//I should put update position sentence here?
		for(int line=0;line<9;line++){
			for(int n=0;n<noteState.activeNotesIndex[line].length;n++){
				int ind=noteState.getNthActiveNoteIndex(line,n);
				if(ind!=-1){
					NoteForRender note=noteState.getNoteByIndex(ind);
					switch(note.state){
					case NoteForRender.UNUSED:
						pos=1-(note.timing_sec-time_s)*velocity;
						if(autoBeat){
							if(pos>=1){
								switch(note.effect){
								case NoteInJson.NOTE_EVENT:
								case NoteInJson.NOTE_STAR:
								case NoteInJson.NOTE_NORMAL:
									note.state=NoteForRender.USED;
									note.assessment=ASSESSMENT_PERFECT;
									soundPool.play(soundMap.get(EFFECT_ID_PERFECT), 1, 1, 1, 0, 1f);
									comboCounter.incrCombo_num();
									comboCounter.incrMax_combo_num();
									synchronized (showAssessmentThread.assessmentStatus) {
										showAssessmentThread.assessmentStatus[ASSESSMENT_PERFECT] = 255f;
									}
									break;
								case NoteInJson.NOTE_LONG:
									note.state=NoteForRender.USING;
									soundPool.play(soundMap.get(EFFECT_ID_PERFECT), 1, 1, 1, 0, 1f);
									noteState.pressNoteIndex[note.position]=ind;
									synchronized (showAssessmentThread.assessmentStatus) {
										showAssessmentThread.assessmentStatus[ASSESSMENT_PERFECT] = 255f;
									}
									break;
								}
							}
						}
						else{
							if(pos>1.25&&!isIn(noteState.pressNoteIndex, ind)){
								soundPool.play(soundMap.get(EFFECT_ID_MISS), 1, 1, 1, 0, 1f);
								updateAssessment(note.position,ASSESSMENT_MISS);
								continue;
							}
						}
						break;
					case NoteForRender.USING:
						pos=1-(note.timing_sec+note.effect_value-time_s)*velocity;//now "pos" is the position of the strip tail.
						if(autoBeat){
							if(pos>=1){
								switch(note.effect){
								case NoteInJson.NOTE_EVENT:
								case NoteInJson.NOTE_STAR:
								case NoteInJson.NOTE_NORMAL:
									
									break;
								case NoteInJson.NOTE_LONG:
									note.state=NoteForRender.USED;
									note.assessment=ASSESSMENT_PERFECT;
									soundPool.play(soundMap.get(EFFECT_ID_PERFECT), 1, 1, 1, 0, 1f);
									noteState.pressNoteIndex[note.position]=-1;
									comboCounter.incrCombo_num();
									comboCounter.incrMax_combo_num();
									synchronized (showAssessmentThread.assessmentStatus) {
										showAssessmentThread.assessmentStatus[ASSESSMENT_PERFECT] = 255f;
									}
									break;
								}
							}
						}
						else{
							if(pos>1.25&&isIn(noteState.pressNoteIndex, ind)){
								soundPool.play(soundMap.get(EFFECT_ID_MISS), 1, 1, 1, 0, 1f);
								updateAssessment(note.position,ASSESSMENT_MISS);
								continue;
							}
						}
						break;
					}
				}
			}
		}
//		for(Note note:pressNotes){
//			if(note!=null){
//				note.currentPosition=1f;
//			}
//		}
	}
	class UpdateThread extends Thread{
		public boolean isRunning=true;
		private SurfaceHolder holder;
		private long startTimeMillis;
		public UpdateThread(SurfaceHolder h){
			holder=h;
			setName("UpdateThread");
		}
		@Override
		public void run(){
			Canvas canvas=null;
			startTimeMillis=System.currentTimeMillis();
			while(isRunning){
				if(running){
					time_running_ms=System.currentTimeMillis()-startTimeMillis;
					try{
						canvas=holder.lockCanvas();
						synchronized(holder){
							update(time_running_ms);
							drawAll(canvas);
						}
					}
					finally{
						if(canvas!=null){
							holder.unlockCanvasAndPost(canvas);
						}
					}
				}
			}
		}
	}
	private boolean isIn(int[] s,int index){
		for(int i:s){
			if(index==i){
				return true;
			}
		}
		return false;
	}
//	private int getFirstActiveNoteIndex(NoteForRender[] nfr,int line){
//		for(int i=0;i<nfr.length;i++){
//			if(nfr[i].state!=NoteForRender.USED){
//				if(nfr[i].position==line){
//					return i;
//				}
//			}
//		}
//		return -1;
//	}
	private int getLine(float x,float y){
		for(int line=0;line<9;line++){
			double theta=line*Math.PI/8;
			double dX=x-width*CENTER_X;
			double dY=y-height*CENTER_Y;
			double angle=Math.atan2(dX, dY)+Math.PI/2;
//			if(Math.abs(angle-theta)<STRIP_ANGLE){    //This will be stricter
			if(Math.abs(angle-theta)<Math.PI/16){
				double r2=dX*dX+dY*dY;
				double R2=height*CENTER_R;
				R2=R2*R2;
				if(r2<1.5625*R2&&r2>0.5625*R2){
					return line;
				}
			}
		}
		return -1;
	}
	private int getAssessment(int noteType,double curpos){
		double error=Math.abs(curpos);
		int res=ASSESSMENT_UNDEFINED;
		if(error>Setting.determination_good_bad&&error<=Setting.determination_bad_miss){//bad
			res=ASSESSMENT_BAD;
		}
		else if(error>Setting.determination_great_good&&error<=Setting.determination_good_bad){//good
			res=ASSESSMENT_GOOD;
		}
		else if(error>Setting.determination_perfect_great&&error<=Setting.determination_great_good){//great
			res=ASSESSMENT_GREAT;
		}
		else if(error<Setting.determination_perfect_great){//perfect
			res=ASSESSMENT_PERFECT;
		}
		else if(error>Setting.determination_bad_miss){
			switch(noteType){
			case NoteInJson.NOTE_LONG:
				res=ASSESSMENT_MISS;
				break;
			default:
				break;
			}
		}
		return res;
	}
	private void updateAssessment(int line,int assessment){
		int finalAssessment=ASSESSMENT_UNDEFINED;
		boolean time_for_updating_combo=true;
		int ind=noteState.getFirstActiveNoteIndex(line);
		if(ind==-1){
			return;
		}
		NoteForRender note=noteState.getNoteByIndex(ind);
		if(noteState.pressNoteIndex[line]!=-1){
			Assert.assertTrue(noteState.pressNoteIndex[line]==ind);
			finalAssessment=getLowerAssessment(assessment,noteState.pressAssessment[line]);
			if(finalAssessment==ASSESSMENT_UNDEFINED){
				LogUtil.e("ASSESSMENT UNDEFINED");
				LogUtil.e(line+" "+note.assessment+" "+note.state+" "+ind);
				return;
			}
			note.assessment=finalAssessment;
//			noteState.getNoteByIndex(pressNoteIndex[line]).state=NoteForRender.USED;
			noteState.goForward(line);
			noteState.pressNoteIndex[line]=-1;
			noteState.pressAssessment[line]=ASSESSMENT_UNDEFINED;
		}
		else{
			finalAssessment=assessment;
			if(finalAssessment==ASSESSMENT_UNDEFINED){
				return;
			}
			switch (note.effect) {
			case NoteInJson.NOTE_NORMAL:
			case NoteInJson.NOTE_EVENT:
			case NoteInJson.NOTE_STAR:
//				note.state=NoteForRender.USED;
				note.assessment=finalAssessment;
				noteState.goForward(line);
				break;
			case NoteInJson.NOTE_LONG:
				if (finalAssessment != ASSESSMENT_MISS) {
					noteState.pressNoteIndex[line] = ind;
					noteState.pressAssessment[line] = assessment;
					note.state=NoteForRender.USING;
				} else {
//					note.state=NoteForRender.USED;
					note.assessment=finalAssessment;
					noteState.goForward(line);
				}
				if (finalAssessment < ASSESSMENT_GOOD) {
					time_for_updating_combo = false;
				}
				break;
			default:
				break;
			}
		}
		for(int i=ASSESSMENT_PERFECT;i<=ASSESSMENT_MISS;i++){
			if(i==finalAssessment){
				synchronized(showAssessmentThread.assessmentStatus){
					showAssessmentThread.assessmentStatus[i]=255f;
				}
			}
			else{
				synchronized(showAssessmentThread.assessmentStatus){
					showAssessmentThread.assessmentStatus[i]=0f;
				}
			}
		}
		if(time_for_updating_combo){
			switch(finalAssessment){
			case ASSESSMENT_PERFECT:
			case ASSESSMENT_GREAT:
				comboCounter.incrCombo_num();
				break;
			case ASSESSMENT_GOOD:
			case ASSESSMENT_BAD:
			case ASSESSMENT_MISS:
				comboCounter.clrCombo_num();
				break;
			default:
				break;
			}
			if(comboCounter.getCombo_num()>comboCounter.getMax_combo_num()){
//				max_combo_num=combo_num;
				comboCounter.incrMax_combo_num();
			}
		}
	}
	private int getLowerAssessment(int assessment,int another_assessment){
		if(assessment<another_assessment){
			return another_assessment;
		}
		else{
			return assessment;
		}
	}
	class FingerEventHandlerThread extends Thread{
		public boolean isRunning=true;
		private FingerEvent fEvent;
		@Override
		public void run(){
			while(isRunning){
				fEvent=null;
				if(fingerEvents.size()>0){
//					Log.i("fingerevents length", fingerEvents.size()+"");
					synchronized(fingerEvents){
						fEvent=fingerEvents.pop();
					}
					int l=fEvent.line;
					int ind=noteState.getFirstActiveNoteIndex(l);
					int assessment=ASSESSMENT_UNDEFINED;
					switch(fEvent.type){
					case press:
						if(ind!=-1){
							NoteForRender note=noteState.getNoteByIndex(ind);
							double tap_pos_error=velocity*(note.timing_sec-fEvent.time);
							switch(note.effect){
							case NoteInJson.NOTE_NORMAL:
								assessment=getAssessment(NoteInJson.NOTE_NORMAL,tap_pos_error);
								break;
							case NoteInJson.NOTE_EVENT:
								assessment=getAssessment(NoteInJson.NOTE_EVENT,tap_pos_error);
								break;
							case NoteInJson.NOTE_STAR:
								assessment=getAssessment(NoteInJson.NOTE_STAR,tap_pos_error);
								break;
							case NoteInJson.NOTE_LONG:
								if(noteState.pressNoteIndex[l]==-1){
									assessment=getAssessment(NoteInJson.NOTE_NORMAL,tap_pos_error);//should use "normal" here
								}
								break;
							default:
								break;
							}
//							Log.e("down assessment", l+" "+assessment);
							LogUtil.e("down assessment: "+l+" "+assessment);
						}
						break;
					case up:
						if(ind!=-1){
							NoteForRender note=noteState.getNoteByIndex(ind);
							double tap_pos_error=velocity*(note.timing_sec+note.effect_value-fEvent.time);
							if(noteState.pressNoteIndex[l]==ind){
								switch(note.effect){
								case NoteInJson.NOTE_LONG:
									assessment=getAssessment(NoteInJson.NOTE_LONG,tap_pos_error);
									break;
								default:
									break;
								}
							}
//							Log.e("up assessment", l+" "+assessment);
							LogUtil.e("up assessment: "+l+" "+assessment);
						}
						break;
					default:
						break;
					}
					if(assessment!=ASSESSMENT_UNDEFINED){
						soundPool.play(soundMap.get(assessment), 1f, 1f, 1, 0, 1f);
						updateAssessment(l,assessment);
					}
					else{
						LogUtil.e("ASSESSMENT UNDEFINED");
					}
				}
			}
		}
	}
	class ShowAssessmentThread extends Thread{
		public boolean isRunning=true;
		public float assessmentStatus[];
		public float fadeSpeed=Setting.assessmentFadingSpeed;
		public ShowAssessmentThread(){
			assessmentStatus=new float[5];
			for(int i=0;i<5;i++){
				assessmentStatus[i]=0f;
			}
			this.setPriority(MIN_PRIORITY);
		}
		@Override
		public void run(){
			while(isRunning){
				for(int i=0;i<5;i++){
					if(assessmentStatus[i]>0f){
						synchronized(assessmentStatus){
							assessmentStatus[i]-=fadeSpeed;
						}
					}
				}
			}
		}
	}
	class NoteState{
		private NoteForRender[] notes;
		private Integer[][] activeNotesIndex;
		private int[] activeIndex={0,0,0,0,0,0,0,0,0};
		private int[] pressNoteIndex={-1,-1,-1,-1,-1,-1,-1,-1,-1};
		private int pressAssessment[]={ASSESSMENT_UNDEFINED,
				ASSESSMENT_UNDEFINED,
				ASSESSMENT_UNDEFINED,
				ASSESSMENT_UNDEFINED,
				ASSESSMENT_UNDEFINED,
				ASSESSMENT_UNDEFINED,
				ASSESSMENT_UNDEFINED,
				ASSESSMENT_UNDEFINED,
				ASSESSMENT_UNDEFINED,
		};
		
		public NoteState(NoteForRender[] nfr){
			this.notes=nfr;
			this.activeNotesIndex=new Integer[9][];
			for(int line=0;line<9;line++){
				ArrayList<Integer> notes_in_one_line=new ArrayList<Integer>();
				for(int i=0;i<nfr.length;i++){
					if(nfr[i].position==line){
						notes_in_one_line.add(i);
					}
				}
				this.activeNotesIndex[line]=notes_in_one_line.toArray(new Integer[notes_in_one_line.size()]);
			}
		}
		public NoteForRender[] getNotes() {
			return notes;
		}
		public void setNotes(NoteForRender[] notes) {
			this.notes = notes;
		}

		/*
		 * Caution: unchecked index!
		 */
		public NoteForRender getNoteByIndex(int index){
			return this.notes[index];
		}
		
		public int getFirstActiveNoteIndex(int line){
			return this.getNthActiveNoteIndex(line, 0);
		}
		
		public NoteForRender getFirstActiveNote(int line){
			if(this.activeIndex[line]<this.activeNotesIndex[line].length){
				return this.notes[this.activeNotesIndex[line][this.activeIndex[line]]];
			}
			else{
				return null;
			}
		}
		
		public int getNthActiveNoteIndex(int line,int n){
			if(this.activeIndex[line]+n<this.activeNotesIndex[line].length){
				return this.activeNotesIndex[line][this.activeIndex[line]+n];
			}
			else{
				return -1;
			}
		}
		
		public void goForward(int line){
			this.notes[this.activeNotesIndex[line][this.activeIndex[line]]].state=NoteForRender.USED;
			this.activeIndex[line]++;
		}
		
		public int[] getAssessments(){
			int[] noteAssessments={0,0,0,0,0};
			for(int ind=0;ind<notes.length;ind++){
//				LogUtil.i(ind+" "+notes[ind].assessment);
				noteAssessments[notes[ind].assessment]++;
			}
			return noteAssessments;
		}
	}
	class ComboCounter{
		private volatile int combo_num=0;
		private volatile int max_combo_num=0;
		public int getCombo_num() {
			return combo_num;
		}
		public int getMax_combo_num() {
			return max_combo_num;
		}
		public synchronized void setCombo_num(int combo_num) {
			this.combo_num = combo_num;
		}
		public synchronized void setMax_combo_num(int max_combo_num) {
			this.max_combo_num = max_combo_num;
		}
		public synchronized void incrCombo_num(){
			combo_num++;
		}
		public synchronized void incrMax_combo_num(){
			max_combo_num++;
		}
		public synchronized void clrCombo_num(){
			combo_num=0;
		}
		public synchronized void clrMax_combo_num(){
			max_combo_num=0;
		}
	}
}