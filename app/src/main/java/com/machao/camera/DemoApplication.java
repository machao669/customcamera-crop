package com.machao.camera;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

/**
 * ============================================================
 *
 * 版 权 ：光量子教育科技  版权所有 (c) 2015 - 2016
 *
 * 作 者 : 马超
 *
 * 创建日期 ： 03/12/2016   16:39
 *
 * 描 述 ：
 *
 * 修订历史 ：
 *
 * ============================================================
 **/
public class DemoApplication extends Application {

    private static DemoApplication  mContext;
    private static Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mHandler = new Handler();
    }

    public static DemoApplication getContext() {
        return mContext;
    }

    public static Handler getHandler() {
        return mHandler;
    }
}
