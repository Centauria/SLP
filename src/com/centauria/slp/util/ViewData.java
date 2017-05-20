package com.centauria.slp.util;

import java.util.ArrayList;
import java.util.Map;

import org.xutils.x;
import org.xutils.common.util.LogUtil;

import com.centauria.slp.R;
import com.centauria.slp.data.Constant;
import com.centauria.slp.data.Setting;
import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;

public class ViewData {

	public static void refreshSongListViewFromData(Activity activity,
			ArrayList<Map<String,Object>> list,
			ListView listview,
			RadioGroup radiogroup){
		LogUtil.i(list.size()+"");
		ArrayList<Map<String,Object>> newList=DataProcess.filterSongList(list, 
				Constant.DIFFICULTY_TEXT_LIST[
				                              radiogroup.indexOfChild(
				                            		  radiogroup.findViewById(
				                            				  radiogroup.getCheckedRadioButtonId()
				                            				  )
				                            		  )
				                              ]);
		SimpleAdapter adapter=new SimpleAdapter(activity,newList,R.layout.list_item_songlist,
				new String[]{"name","difficulty_text","live_icon_asset"},
				new int[]{R.id.list_item_songlist_title,
						R.id.list_item_songlist_info,
						R.id.list_item_songlist_img});
		adapter.setViewBinder(new ViewBinder(){

			@Override
			public boolean setViewValue(View view, Object data, String textRepresentation) {
				// TODO Auto-generated method stub
				if(view instanceof ImageView && data instanceof String){
					ImageView iv=(ImageView) view;
					//使用xutils加载图片到imageView
					x.image().bind(iv, Constant.ADDRESS_LIST[Setting.activeLinkIndex]+data);
					return true;
				}
				return false;
			}
			
		});
		listview.setAdapter(adapter);
	}

}
