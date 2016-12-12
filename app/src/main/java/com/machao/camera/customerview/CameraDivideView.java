package com.machao.camera.customerview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * ============================================================
 *
 * 版 权 ：光量子教育科技  版权所有 (c) 2015
 *
 * 作 者 : 马超
 *
 * 创建日期 ： 6/2/16   14:21
 *
 * 描 述 ：
 *
 * 修订历史 ：
 *
 * ============================================================
 **/
public class CameraDivideView extends View {

    private Paint mPaint;

    public CameraDivideView(Context context) {
        super(context);
        init();
    }

    public CameraDivideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(0x88ffffff);
        mPaint.setStrokeWidth(1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int measuredHeight = getMeasuredHeight();
        int measuredWidth = getMeasuredWidth();
        int cellHeight = measuredHeight / 3;
        int cellWidth = measuredWidth / 3;
        canvas.drawLine(cellWidth, 0, cellWidth, measuredHeight, mPaint);
        canvas.drawLine(cellWidth * 2, 0, cellWidth * 2, measuredHeight, mPaint);
        canvas.drawLine(0, cellHeight, measuredWidth, cellHeight, mPaint);
        canvas.drawLine(0, cellHeight * 2, measuredWidth, cellHeight * 2, mPaint);
    }
}
