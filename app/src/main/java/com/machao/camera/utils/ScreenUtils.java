package com.machao.camera.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * ============================================================
 *
 * 版 权 ：光量子教育科技  版权所有 (c) 2015
 *
 * 作 者 : 马超
 *
 * 创建日期 ： 15/9/16   下午4:59
 *
 * 描 述 ：获取屏幕相关信息的一些工具类
 *
 * 修订历史 ：
 *
 * ============================================================
 **/

public class ScreenUtils {

    /**
     * 获取屏幕的宽度
     *
     * @param context Context
     * @return 屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getWidth();
    }

    /**
     * 获取屏幕的高度
     *
     * @param context Context
     * @return 屏幕高度
     */
    public static int getScreenHeight(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getHeight();
    }

    /**
     * 获取屏幕的密度比
     *
     * @param context Context
     * @return 屏幕的密度比
     */
    public static float getScreenDensity(Context context) {
        try {
            DisplayMetrics dm = new DisplayMetrics();
            WindowManager manager = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            manager.getDefaultDisplay().getMetrics(dm);
            return dm.density;
        } catch (Exception e) {
            Log.e("getScreenDensity", "getSystemService(Context.WINDOW_SERVICE) has error");
        }
        return 1.0f;
    }

    /**
     * 获取屏幕状态栏的高度
     */

    public static int getStatusBarHeight(Activity activity) {
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        return frame.top;
    }
}