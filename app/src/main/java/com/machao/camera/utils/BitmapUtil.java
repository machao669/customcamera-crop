package com.machao.camera.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;

/**
 * ============================================================
 *
 * 版 权 ：光量子教育科技  版权所有 (c) 2015
 *
 * 作 者 : 马超
 *
 * 创建日期 ： 5/18/16   11:02
 *
 * 描 述 ：
 *
 * 修订历史 ：
 *
 * ============================================================
 **/
public class BitmapUtil {

    /**
     * @param bitmap 目标源
     * @return byte[]
     */
    public static byte[] compress(Bitmap bitmap, int reqWidth, int quality) {

        int width = bitmap.getWidth();
        Bitmap result;
        if (width > reqWidth) {
            float ratio = reqWidth * 1.0f / width;
            Matrix matrix = new Matrix();
            matrix.postScale(ratio, ratio);
            result = Bitmap.createBitmap(bitmap, 0, 0, width, bitmap.getHeight(), matrix, true);
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        } else {
            result = bitmap;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        result.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        if (!result.isRecycled()) {
            result.recycle();
        }
        return baos.toByteArray();
    }

    /**
     * 压缩图片大小
     *
     * @param bitmap   bitmap
     * @param reqWidth witdth
     * @return bitmap
     */
    public static Bitmap compressSize(Bitmap bitmap, int reqWidth) {
        int width = bitmap.getWidth();
        Bitmap result;
        if (width > reqWidth) {
            float ratio = reqWidth * 1.0f / width;
            Matrix matrix = new Matrix();
            matrix.postScale(ratio, ratio);
            result = Bitmap.createBitmap(bitmap, 0, 0, width, bitmap.getHeight(), matrix, true);
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        } else {
            result = bitmap;
        }
        return result;
    }

    /**
     * @param angle  旋转角度
     * @param bitmap bitmap
     * @return bitmap
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
            bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
        return resizedBitmap;
    }

    /**
     * @param bm bitmap
     * @return byte[]
     */
    public static byte[] BitmapToBytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
