package com.machao.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.edmodo.cropper.CropImageView;
import com.machao.camera.utils.BitmapUtil;
import com.machao.camera.utils.DirectionConstans;
import com.machao.camera.utils.ExtraConstans;
import com.machao.camera.utils.FileUtils;
import com.machao.camera.utils.ScreenUtils;
import com.machao.camera.utils.ToastUtil;
import com.nineoldandroids.animation.ObjectAnimator;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

/**
 * ============================================================
 *
 * 版 权 ：光量子教育科技  版权所有 (c) 2015
 *
 * 作 者 : 马超
 *
 * 创建日期 ： 7/5/16   19:39
 *
 * 描 述 ：
 *
 * 修订历史 ：
 *
 * ============================================================
 **/
@RuntimePermissions
public class CropActivity extends Activity {

    @Bind(R.id.btn_crop_save)
    protected ImageView mSaveCrop;

    @Bind(R.id.crop_image_view)
    protected CropImageView mCropImageView;

    @Bind(R.id.tv_crop_hint_horizontal)
    protected TextView mTVCropHintHorizontal;

    @Bind(R.id.tv_crop_hint_horizontal_right)
    protected TextView mTVCropHintHorizontalRight;

    @Bind(R.id.tv_crop_hint_vertical)
    protected TextView mTVCropHintVertical;

    @Bind(R.id.iv_crop_rotate)
    protected ImageView mCropRotate;

    private int mImageOrigin;
    private boolean mClickIng = false;
    private int mOrientationState;
    private int mCompressWidth = 1080;
    public int MAX_WIDTH;
    public int MAX_HEIGHT;

    private MyOrientationListener mMyOrientationListener;
    private boolean mIsOpenOrientationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉状态栏
        setContentView(R.layout.activity_crop);
        ButterKnife.bind(this);

        //设置加载进来图片的最大宽度和高度
        MAX_WIDTH = ScreenUtils.getScreenWidth(this) / 2 + 1;
        MAX_HEIGHT = ScreenUtils.getScreenHeight(this) / 2 + 1;

        mOrientationState = getIntent().getIntExtra(ExtraConstans.ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mDirection = getIntent().getIntExtra(ExtraConstans.DIRECTION, 0);
        mImageOrigin = getIntent().getIntExtra(ExtraConstans.IMAGE_ORIGIN, -1);
        mIsOpenOrientationListener = getIntent().getBooleanExtra(ExtraConstans.ISOPENORIENTATIONCHANGE, false);

        init();
        mMyOrientationListener = new MyOrientationListener(this);
        Uri inputUri = getIntent().getParcelableExtra(ExtraConstans.IMAGE_URI);
        CropActivityPermissionsDispatcher.setImageWithCheck(this, inputUri);
    }

    @NeedsPermission( {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    void setImage(Uri inputUri) {

        String path = FileUtils.getPath(this, inputUri);
        if (path == null) {
            ToastUtil.showToast("图片路径错误");
            finish();
            return;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int originWidth = options.outWidth;
        int originHeight = options.outHeight;

        int withSample;
        int heightSample;
        if (originHeight < originWidth) {
            withSample = originWidth / MAX_HEIGHT;
            heightSample = originHeight / MAX_WIDTH;
        } else {
            withSample = originWidth / MAX_WIDTH;
            heightSample = originHeight / MAX_HEIGHT;
        }
        int inSampleSize = 1;
        int max = Math.max(withSample, heightSample);
        if (max > 1) {
            inSampleSize = max;
        }
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeFile(path, options);

        if (mImageOrigin == CameraActivity.URI_CAMERA) {
            src = BitmapUtil.rotaingImageView(90, src);
        } else if (mImageOrigin == CameraActivity.URI_ALBUM
            && mOrientationState == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            src = BitmapUtil.rotaingImageView(mDirection == DirectionConstans.LANDSCAPE_RIGHT ? -90 : 90, src);
        }

        if (mImageOrigin == CameraActivity.URI_ALBUM) {
            mCropImageView.setOrientationState(CropImageView.STATE_BOX_ALL);
        } else {
            mCropImageView.setOrientationState(mOrientationState);
        }
        mCropImageView.setImageBitmap(src);
    }

    @OnPermissionDenied( {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    void denied() {
        ToastUtil.showToast("您已拒绝授权,无法设置图片");
        finish();
    }

    @OnNeverAskAgain( {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    void notAsk() {
        ToastUtil.showToast("您已设置不再询问,请到设置中赋予应用'存储'权限,再进行下一步操作");
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CropActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private void init() {
        if (mOrientationState == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mTVCropHintVertical.setVisibility(View.INVISIBLE);
            if (mDirection == DirectionConstans.LANDSCAPE_LEFT) {
                mSaveCrop.setRotation(90);
                mCropRotate.setRotation(90);
                mTVCropHintHorizontal.setVisibility(View.VISIBLE);
            } else if (mDirection == DirectionConstans.LANDSCAPE_RIGHT) {
                mSaveCrop.setRotation(-90);
                mCropRotate.setRotation(-90);
                mTVCropHintHorizontalRight.setVisibility(View.VISIBLE);
            }
        }
    }

    @OnClick(R.id.btn_crop_save)
    protected void cropImage(View view) {
        if (mClickIng) {
            return;
        }

        // TODO: 06/12/2016 loading 开始 自己加
        mClickIng = true;

        Bitmap bitmap = mCropImageView.getCroppedImage();
        if (bitmap == null) {
            mClickIng = false;
            return;
        }
        if (mDirection == DirectionConstans.LANDSCAPE_LEFT) {
            bitmap = BitmapUtil.rotaingImageView(-90, bitmap);
        } else if (mDirection == DirectionConstans.LANDSCAPE_RIGHT) {
            bitmap = BitmapUtil.rotaingImageView(90, bitmap);
        }

        //**这步最好在子线程中操作
        byte[] bytes = BitmapUtil.compress(bitmap, 1080, 50);
        Intent intent = new Intent(this, ShowActivity.class);
        intent.putExtra(ExtraConstans.IMAGE, bytes);
        startActivity(intent);

        // TODO: 06/12/2016 loading 结束 自己加
        mClickIng = false;
    }

    @OnClick(R.id.btn_crop_exit)
    protected void exit(View view) {
        finish();
    }

    @OnClick(R.id.iv_crop_rotate)
    protected void rotate(View view) {
        mCropImageView.rotateImage(90);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsOpenOrientationListener) {
            mMyOrientationListener.enable();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsOpenOrientationListener) {
            mMyOrientationListener.disable();
        }
    }

    private int mDirection;

    private void orientationChanged(int orientation, int type) {
        mCropImageView.setOrientationState(orientation);
        if (type == DirectionConstans.PORTRAIT_NORMAL || type == DirectionConstans.PORTRAIT_INVERTED) {
            int rotate = mDirection == DirectionConstans.LANDSCAPE_RIGHT ? -90 : 90;
            ObjectAnimator.ofFloat(mSaveCrop, "rotation", rotate, 0).setDuration(500).start();
            ObjectAnimator.ofFloat(mCropRotate, "rotation", rotate, 0).setDuration(500).start();
            mTVCropHintHorizontal.setVisibility(View.INVISIBLE);
            mTVCropHintHorizontalRight.setVisibility(View.INVISIBLE);
            mTVCropHintVertical.setVisibility(View.VISIBLE);
            mCropImageView.rotateImage(mDirection == DirectionConstans.LANDSCAPE_RIGHT ? 90 : -90);
        } else if (type == DirectionConstans.LANDSCAPE_LEFT) {
            ObjectAnimator.ofFloat(mSaveCrop, "rotation", 0, 90).setDuration(500).start();
            ObjectAnimator.ofFloat(mCropRotate, "rotation", 0, 90).setDuration(500).start();
            mTVCropHintHorizontal.setVisibility(View.VISIBLE);
            mTVCropHintHorizontalRight.setVisibility(View.INVISIBLE);
            mTVCropHintVertical.setVisibility(View.INVISIBLE);
            mCropImageView.rotateImage(90);
        } else if (type == DirectionConstans.LANDSCAPE_RIGHT) {
            ObjectAnimator.ofFloat(mSaveCrop, "rotation", 0, -90).setDuration(500).start();
            ObjectAnimator.ofFloat(mCropRotate, "rotation", 0, -90).setDuration(500).start();
            mTVCropHintHorizontal.setVisibility(View.INVISIBLE);
            mTVCropHintVertical.setVisibility(View.INVISIBLE);
            mTVCropHintHorizontalRight.setVisibility(View.VISIBLE);
            mCropImageView.rotateImage(-90);
        }
        mDirection = type;
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
                orientationChanged(mOrientationState, DirectionConstans.PORTRAIT_NORMAL);
            } else if (orientation > 80 && orientation < 100
                && mOrientationState != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                mOrientationState = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                orientationChanged(mOrientationState, DirectionConstans.LANDSCAPE_RIGHT);
            } else if (orientation > 170 && orientation < 190
                && mOrientationState != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                mOrientationState = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                orientationChanged(mOrientationState, DirectionConstans.PORTRAIT_INVERTED);
            } else if (orientation > 260 && orientation < 280
                && mOrientationState != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                mOrientationState = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                orientationChanged(mOrientationState, DirectionConstans.LANDSCAPE_LEFT);
            }
        }
    }
}
