package com.machao.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.machao.camera.utils.ExtraConstans;

/**
 * ============================================================
 *
 * 版 权 ：光量子教育科技  版权所有 (c) 2015 - 2016
 *
 * 作 者 : 马超
 *
 * 创建日期 ： 05/12/2016   10:02
 *
 * 描 述 ：
 *
 * 修订历史 ：
 *
 * ============================================================
 **/
public class ShowActivity extends Activity {

    @Bind(R.id.iv_target)
    ImageView mTarget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        ButterKnife.bind(this);

        byte[] bytes = getIntent().getByteArrayExtra(ExtraConstans.IMAGE);
        if (bytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            mTarget.setImageBitmap(bitmap);
        }
    }
}
