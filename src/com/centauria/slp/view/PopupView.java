package com.centauria.slp.view;

import com.centauria.slp.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class PopupView extends View {
//	private MainActivity mainActivity;
	private Bitmap bg_p;
	private NinePatch bg;
	private RectF popupRect;
	private float testMarginLeft=0;
	public PopupView(Context context) {
		super(context);
		init(context,null,0);
	}

	public PopupView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context,attrs,0);
	}

	public PopupView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context,attrs,defStyleAttr);
	}

	private void init(Context context,AttributeSet attrs, int defStyle){
//		if(!isInEditMode()){
//			mainActivity=(MainActivity)context;
//		}
		bg_p = BitmapFactory.decodeResource(getResources(), R.drawable.popup_frame);
		bg = new NinePatch(bg_p, bg_p.getNinePatchChunk(), null);
		popupRect=new RectF();
        
		final TypedArray tArray = getContext().obtainStyledAttributes(attrs, R.styleable.PopupView, defStyle, 0);
		testMarginLeft=tArray.getDimension(R.styleable.PopupView_testMarginLeft, testMarginLeft);
		tArray.recycle();
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		drawPopup(canvas,0f,1f,0f,1f);
	}
	
	private void drawPopup(Canvas canvas,float left,float right,float top,float bottom){
		left*=this.getWidth();
		right*=this.getWidth();
		top*=this.getHeight();
		bottom*=this.getHeight();
		popupRect.set(left, top, right, bottom);
		bg.draw(canvas, popupRect);
	}
}
