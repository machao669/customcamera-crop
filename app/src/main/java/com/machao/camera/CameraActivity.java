package com.machao.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.machao.camera.customerview.CameraPreview;
import com.machao.camera.customerview.FocusView;
import com.machao.camera.utils.DirectionConstans;
import com.machao.camera.utils.ExtraConstans;
import com.machao.camera.utils.ToastUtil;
import com.nineoldandroids.animation.ObjectAnimator;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * ============================================================
 *
 * 版 权 ：光量子教育科技  版权所有 (c) 2015
 *
 * 作 者 : 马超
 *
 * 创建日期 ： 5/17/16   14:34
 *
 * 描 述 ：
 *
 * 修订历史 ：
 *
 * ============================================================
 **/
@RuntimePermissions
public class CameraActivity extends Activity
    implements CameraPreview.CameraListener, SensorEventListener {

    @Bind(R.id.surface_camera)
    protected CameraPreview mCameraView;

    @Bind(R.id.tv_takepoton_text_example_vertical)
    protected TextView mTextExampleVertical;

    @Bind(R.id.tv_takepoton_text_example_horizontal)
    protected TextView mTextExampleHorizontal;

    @Bind(R.id.tv_takepoton_text_example_horizontal_right)
    protected TextView mTextExampleHorizontalRight;

    @Bind(R.id.btn_take_pic)
    protected ImageView mTakePic;

    @Bind(R.id.tv_take_album)
    protected TextView mTVAlbum;

    @Bind(R.id.fv_camera_focus)
    protected FocusView mFocusView;

    public static final int URI_CAMERA = 1;
    public static final int URI_ALBUM = 2;

    private boolean mClickIng = false;

    private SensorManager mSensorManager;
    private Sensor mAccel;
    private MyOrientationListener mMyOrientationListener;
    private boolean mIsOpenOrientationListener;
    private int mOrientationState;
    private int mDirection = DirectionConstans.PORTRAIT_NORMAL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉状态栏

        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        initByOrigin();

        mCameraView.setOnCameraListener(this);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMyOrientationListener = new MyOrientationListener(this);

    }

    private void initByOrigin() {
        mTextExampleHorizontal.setText(R.string.camera_hint_horizontal);
        mTextExampleHorizontalRight.setText(R.string.camera_hint_horizontal);
        mTextExampleVertical.setText(getString(R.string.camera_hint_horizontal));

        mIsOpenOrientationListener = getIntent().getBooleanExtra(ExtraConstans.ISOPENORIENTATIONCHANGE, false);
        mOrientationState = getIntent().getIntExtra(ExtraConstans.ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if (mOrientationState == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mTextExampleVertical.setVisibility(View.INVISIBLE);
            mTextExampleHorizontal.setVisibility(View.VISIBLE);
            mTakePic.setRotation(90);
            mTVAlbum.setRotation(90);
            mDirection = DirectionConstans.LANDSCAPE_LEFT;
        }
    }

    @OnClick( {R.id.btn_take_exit, R.id.btn_take_pic})
    protected void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_take_pic:
                if (mClickIng) {
                    return;
                }
                mClickIng = true;
                mCameraView.capture();
                break;
            case R.id.btn_take_exit:
                finish();
                break;
            default:
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        mCameraView.setState(false);
        if (mIsOpenOrientationListener) {
            mMyOrientationListener.disable();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.setState(true);
        mCameraView.setFocusView(mFocusView);
        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_UI);
        if (mIsOpenOrientationListener) {
            mMyOrientationListener.enable();
        }
    }

    @Override
    public void cameraNotFound() {
        ToastUtil.showToast(R.string.camera_not_found);
    }

    @Override
    public void cameraForbid() {
        ToastUtil.showToast(R.string.camera_forbidden);
    }

    @Override
    public void onCapture(byte[] data) {
        CameraActivityPermissionsDispatcher.saveImageWithCheck(this, data);
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void saveImage(byte[] data) {

        mSensorManager.unregisterListener(this);
        try {
            saveToCache(data);
        } catch (NullPointerException e) {
            saveToAlbum(data);
        } catch (IOException e) {
            saveToAlbum(data);
        }
        mClickIng = false;
    }

    private void saveToCache(byte[] data) throws NullPointerException, IOException {
        File dir = getExternalCacheDir();
        if (dir == null || !dir.exists()) {
            dir = getCacheDir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(dir, fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data);
        fos.close();
        Uri uri = Uri.fromFile(file);
        toCrop(uri, URI_CAMERA);
    }


    private void saveToAlbum(byte[] data) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        try {
            Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null, null));
            toCrop(uri, URI_CAMERA);
        } catch (NullPointerException e) {
            ToastUtil.showToast("储存卡不存在或未对应用开放储存相关权限~");
            finish();
        }
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void denied() {
        ToastUtil.showToast("您已拒绝授权,无法进入裁剪图片");
        mClickIng = false;
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void notAsk() {
        ToastUtil.showToast("您已设置不再询问,请到设置中赋予应用'存储'权限,再进行下一步操作");
        mClickIng = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CameraActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void focusFaild() {
        mClickIng = false;
    }

    @OnClick(R.id.tv_take_album)
    protected void openAlbum(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            final Uri uri = data.getData();
            mTakePic.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toCrop(uri, URI_ALBUM);
                }
            }, 50);
        }
    }

    private void toCrop(Uri uri, int uriOrigin) {
        Intent intent = new Intent(this, CropActivity.class);
        intent.putExtra(ExtraConstans.IMAGE_URI, uri);
        intent.putExtra(ExtraConstans.ORIENTATION, mOrientationState);
        intent.putExtra(ExtraConstans.DIRECTION, mDirection);
        intent.putExtra(ExtraConstans.IMAGE_ORIGIN, uriOrigin);
        intent.putExtra(ExtraConstans.ISOPENORIENTATIONCHANGE, mIsOpenOrientationListener);
        startActivity(intent);
    }


    private float mLastX = 0;
    private float mLastY = 0;
    private float mLastZ = 0;
    private boolean mInitialized = false;
    private boolean mShouldFocus = false;

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        if (!mInitialized) {
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            mInitialized = true;
        }
        float deltaX = Math.abs(mLastX - x);
        float deltaY = Math.abs(mLastY - y);
        float deltaZ = Math.abs(mLastZ - z);

        double speed = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        if (speed > 0.8) {
            mShouldFocus = true;
        } else if (speed < 0.2 && mShouldFocus) {
            mShouldFocus = false;
            mCameraView.setFocus();
        }

        mLastX = x;
        mLastY = y;
        mLastZ = z;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void orientationChanged(int direction) {
        if (direction == DirectionConstans.PORTRAIT_NORMAL || direction == DirectionConstans.PORTRAIT_INVERTED) {
            int rotate = mDirection == DirectionConstans.LANDSCAPE_RIGHT ? -90 : 90;
            ObjectAnimator.ofFloat(mTakePic, "rotation", rotate, 0).setDuration(500).start();
            ObjectAnimator.ofFloat(mTVAlbum, "rotation", rotate, 0).setDuration(500).start();
            mTextExampleHorizontal.setVisibility(View.INVISIBLE);
            mTextExampleHorizontalRight.setVisibility(View.INVISIBLE);
            mTextExampleVertical.setVisibility(View.VISIBLE);
        } else if (direction == DirectionConstans.LANDSCAPE_LEFT) {
            ObjectAnimator.ofFloat(mTakePic, "rotation", 0, 90).setDuration(500).start();
            ObjectAnimator.ofFloat(mTVAlbum, "rotation", 0, 90).setDuration(500).start();
            mTextExampleHorizontal.setVisibility(View.VISIBLE);
            mTextExampleHorizontalRight.setVisibility(View.INVISIBLE);
            mTextExampleVertical.setVisibility(View.INVISIBLE);
        } else if (direction == DirectionConstans.LANDSCAPE_RIGHT) {
            ObjectAnimator.ofFloat(mTakePic, "rotation", 0, -90).setDuration(500).start();
            ObjectAnimator.ofFloat(mTVAlbum, "rotation", 0, -90).setDuration(500).start();
            mTextExampleHorizontal.setVisibility(View.INVISIBLE);
            mTextExampleHorizontalRight.setVisibility(View.VISIBLE);
            mTextExampleVertical.setVisibility(View.INVISIBLE);
        }
        mDirection = direction;
    }

    /**
     * 屏幕切换监听
     */
    class MyOrientationListener extends OrientationEventListener {

        public MyOrientationListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation == -1) {
                return;
            }
            if ((orientation > 350 || orientation < 10)
                && mOrientationState != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                mOrientationState = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                orientationChanged(DirectionConstans.PORTRAIT_NORMAL);
            } else if (orientation > 80 && orientation < 100
                && mOrientationState != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                mOrientationState = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                orientationChanged(DirectionConstans.LANDSCAPE_RIGHT);
            } else if (orientation > 170 && orientation < 190
                && mOrientationState != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                mOrientationState = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                orientationChanged(DirectionConstans.PORTRAIT_INVERTED);
            } else if (orientation > 260 && orientation < 280
                && mOrientationState != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                mOrientationState = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                orientationChanged(DirectionConstans.LANDSCAPE_LEFT);
            }

        }
    }

}