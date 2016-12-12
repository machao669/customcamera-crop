package com.machao.camera.customerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

import com.machao.camera.camerahelp.CameraManager;
import com.machao.camera.utils.CameraUtils;
import com.machao.camera.utils.ToastUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 通过surface的preview实现相机拍照功能
 */
public class CameraPreview extends SurfaceView
    implements SurfaceHolder.Callback, Camera.PreviewCallback, Camera.AutoFocusCallback {

    /**
     * 相机事件回调
     */
    public interface CameraListener {
        void cameraNotFound();

        void cameraForbid();

        void onCapture(byte[] data);

        void focusFaild();
    }

    private int mDegrees = 90;  //镜头旋转90°
    private CameraListener mCameraListener;
    private static final String TAG = CameraPreview.class.getName();
    private SurfaceHolder mHolder;
    private CameraManager mCameraManager;
    private boolean shouldCapture = false;
    private FocusView mFocusView;

    public CameraPreview(Context context) {
        super(context);
        init();
    }

    /**
     * 初始化相机
     *
     * @param context 上下文
     * @param attrs   包含相机角度
     */
    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setOnCameraListener(CameraListener listener) {
        mCameraListener = listener;
    }

    public CameraManager getCameraManager() {
        return mCameraManager;
    }

    @SuppressWarnings("deprecation")
    private void init() {
        if (CameraUtils.checkCameraHardware(getContext())) {
            mCameraManager = new CameraManager(getContext());

            mHolder = this.getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);  // Need to set this flag despite it's deprecated
        } else {
            Log.e(TAG, "Error: Camera not found");
            if (mCameraListener != null) {
                mCameraListener.cameraNotFound();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        try {
            // Indicate camera, our View dimensions
            mCameraManager.openDriver(holder, this.getWidth(), this.getHeight());
        } catch (Exception e) {
            Log.w(TAG, "Can not openDriver: " + e.getMessage());
            mCameraManager.closeDriver();
            if (mCameraListener != null) {
                mCameraListener.cameraForbid();
                return;
            }
        }

        try {
            mCameraManager.startPreview();
            mCameraManager.getCamera().setPreviewCallback(this);
            mCameraManager.getCamera().setDisplayOrientation(mDegrees);
            setFocus();
            setOnTouchListener(mOnTouchListener);
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            mCameraManager.closeDriver();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        Camera camera = mCameraManager.getCamera();
        if (camera != null) {
            mCameraManager.getCamera().setPreviewCallback(null);
            mCameraManager.getCamera().stopPreview();
            mCameraManager.getCamera().release();
            mCameraManager.closeDriver();
        }
    }

    // Called when camera take a frame
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (!shouldCapture || mCameraListener == null) {
            return;
        }

        shouldCapture = false;
        Camera.Parameters parameters = camera.getParameters();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;

        YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, width, height), 100, out);
        byte[] bytes = out.toByteArray();

        try {
            out.close();
        } catch (IOException e) {
            Log.w(TAG, "ByteArrayOutputStream close exception");
        }
        mCameraListener.onCapture(bytes);

    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");
        setFocus();
    }

    /**
     * 点击显示焦点区域
     */
    OnTouchListener mOnTouchListener = new OnTouchListener() {
        @SuppressWarnings("deprecation")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mCameraManager == null || mCameraManager.getCamera() == null || mFocusView == null) {
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int width = mFocusView.getWidth();
                int height = mFocusView.getHeight();
                mFocusView.setX(event.getX() - (width / 2));
                mFocusView.setY(event.getY() - (height / 2));
                mFocusView.beginFocus();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                focusOnTouch(event);
            }
            return true;
        }
    };

    /**
     * 设置焦点和测光区域
     *
     * @param event MotionEvent
     */
    public void focusOnTouch(MotionEvent event) {

        Camera camera = mCameraManager.getCamera();
        int[] location = new int[2];
        RelativeLayout relativeLayout = (RelativeLayout) getParent();
        relativeLayout.getLocationOnScreen(location);

        Rect focusRect = CameraUtils.calculateTapArea(mFocusView.getWidth(),
            mFocusView.getHeight(), 1f, event.getRawX(), event.getRawY(),
            location[0], location[0] + relativeLayout.getWidth(), location[1],
            location[1] + relativeLayout.getHeight());
        Rect meteringRect = CameraUtils.calculateTapArea(mFocusView.getWidth(),
            mFocusView.getHeight(), 1.5f, event.getRawX(), event.getRawY(),
            location[0], location[0] + relativeLayout.getWidth(), location[1],
            location[1] + relativeLayout.getHeight());

        Camera.Parameters parameters = camera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

        if (parameters.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
            focusAreas.add(new Camera.Area(focusRect, 1000));

            parameters.setFocusAreas(focusAreas);
        }

        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
            meteringAreas.add(new Camera.Area(meteringRect, 1000));

            parameters.setMeteringAreas(meteringAreas);
        }

        try {
            camera.setParameters(parameters);
            camera.autoFocus(this);
        } catch (Exception e) {
            ToastUtil.showToast("对焦失败");
        }
    }

    /**
     * 设置相机的状态
     *
     * @param isPreview 是否处于预览模式
     */
    public synchronized void setState(boolean isPreview) {
        if (mCameraManager.getCamera() == null) {
            return;
        }
        if (isPreview) {
            if (mHolder.getSurface() == null) {
                Log.e(TAG, "Error: preview surface does not exist");
                return;
            }
            mCameraManager.startPreview();
        } else {
            if (mHolder.getSurface() == null) {
                Log.e(TAG, "Error: preview surface does not exist");
                return;
            }
            mCameraManager.stopPreview();
        }
    }

    /**
     * 拍照
     */
    public void capture() {
        shouldCapture = true;
        if (mCameraManager != null && mCameraManager.getCamera() != null) {
            mCameraManager.getCamera().setPreviewCallback(this);
        }
    }

    /**
     * 对焦
     */
    public void setFocus() {
        if (mCameraManager != null && mCameraManager.getCamera() != null) {
            Camera camera = mCameraManager.getCamera();
            if (mFocusView != null && !mFocusView.isFocusing()) {
                try {
                    camera.autoFocus(this);
                    mFocusView.setX((CameraUtils.getWidthInPx(getContext()) - mFocusView.getWidth()) / 2);
                    mFocusView.setY((CameraUtils.getHeightInPx(getContext()) - mFocusView.getHeight()) / 2);
                    mFocusView.beginFocus();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 设置聚焦的图片
     *
     * @param focusView FocusView
     */
    public void setFocusView(FocusView focusView) {
        this.mFocusView = focusView;
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {

    }
}
