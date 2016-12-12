### 自定义相机 + 裁剪的 demo

#### 用到的三方库：
    zxing的部分代码，主要是相机的设置和扫描时获取帧数据
    裁剪库是在[cropper](https://github.com/edmodo/cropper)修改而来，主要是修改了样式，添加了旋转功能，及修复部分bug
    其他的库可以在build.gradle中查看

#### 配置使用：
    拍照页面默认是横屏，不开启手机方向感应的

    如果需要，可以在CameraActivity中修改默认值，也可以在开启CameraActivity的Activity中配置相关参数：
        intent.putExtra(ExtraConstans.ISOPENORIENTATIONCHANGE, mIsOpenOrientationListener);
        mIsOpenOrientationListener取值：
            true: 开启手机方向监听（根据你手持手机的状态，自动变化为横屏或竖屏，不需要设置ExtraConstans.ORIENTATION的值）
            false：关闭手机方向监听，默认值

        intent.putExtra(ExtraConstans.ORIENTATION, mOrientationState);
        mOrientationState取值：
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE：横屏
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT：竖屏

本DEMO主要用于学习交流，有问题请发邮件至[(492231487@qq.com)](mailto:492231487@qq.com)
