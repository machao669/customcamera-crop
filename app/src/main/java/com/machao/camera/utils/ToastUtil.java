package com.machao.camera.utils;

import android.os.Handler;
import android.widget.Toast;

import com.machao.camera.DemoApplication;

/**
 * ============================================================
 *
 * 版 权 ：光量子教育科技  版权所有 (c) 2015
 *
 * 作 者 : 马超
 *
 * 创建日期 ： 15/10/22   下午4:14
 *
 * 描 述 ：封装toast的工具,不用等上一个toast结束才会弹下一个
 *
 * 修订历史 ：
 *
 * ============================================================
 **/
public class ToastUtil {

    private static Toast toast;

    /**
     * 能够连续弹吐司，不用等上个消失
     *
     * @param msg 弹出的消息
     */
    public static void showToast(final String msg) {

        DemoApplication.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (toast == null) {
                    toast = Toast.makeText(DemoApplication.getContext(), msg, Toast.LENGTH_SHORT);
                }
                toast.setText(msg);
                toast.show();
            }
        });

    }

    /**
     * 根据id显示
     * @param id 资源id
     */
    public static void showToast(int id) {
        String toast = DemoApplication.getContext().getResources().getString(id);
        showToast(toast);
    }
}
