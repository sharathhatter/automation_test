package com.daimajia.slider.library.Transformers;


import android.os.Build;
import android.view.View;

public class CubeOutTransformer extends BaseTransformer {

    @Override
    protected void onTransform(View view, float position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            view.setPivotX(position < 0f ? view.getWidth() : 0f);
            view.setPivotY(view.getHeight() * 0.5f);
            view.setRotationY(90f * position);
        }
    }

    @Override
    protected boolean isPagingEnabled() {
        return true;
    }
}