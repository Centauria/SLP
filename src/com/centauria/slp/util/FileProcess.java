package com.centauria.slp.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.xutils.common.util.LogUtil;

import android.content.Context;
import android.os.Environment;

public class FileProcess {
	public static void copyData(InputStream input,OutputStream out) throws IOException{
		byte[] buf = new byte[1024];
	    int len;
	    while ((len = input.read(buf)) > 0) {
	    	out.write(buf, 0, len);
	    }
	}
	public static String getDiskCacheDir(Context context){
		String cachePath=null;
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				||!Environment.isExternalStorageRemovable()){
			cachePath=context.getExternalCacheDir().getPath();
		}
		else{
			cachePath=context.getCacheDir().getPath();
		}
		LogUtil.i("cachedir= "+cachePath);
		return cachePath;
	}
	@Deprecated
	public static String getDiskCacheDirOld(Context context){
		String cachePath=null;
		cachePath=context.getCacheDir().getPath();
		LogUtil.i("cachedir= "+cachePath);
		return cachePath;
	}
	public static void getAllDirs(String path){
		File file=new File(path);
		String[] filestrs=file.list();
		for(String s:filestrs){
			String filestr=path+File.separator+s;
			File file_s=new File(filestr);
			if(file_s.isDirectory()){
				getAllDirs(filestr);
			}
			else{
				LogUtil.i(filestr);
			}
		}
	}
}
