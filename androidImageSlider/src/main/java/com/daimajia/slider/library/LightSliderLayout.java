package com.daimajia.slider.library;

import android.content.Context;
import android.util.AttributeSet;

public class LightSliderLayout extends SliderLayout {
    public LightSliderLayout(Context context) {
        super(context);
    }

    public LightSliderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LightSliderLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.light_slider_layout;
    }
}
