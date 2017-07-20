package com.userinfosetting;

import android.content.Context;
import android.widget.Toast;

/**
 * 弹吐司的工具类
 */
public class ToastUtils {

	public static void showLong(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
	public static void showShort(Context context, String msg){
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
}
