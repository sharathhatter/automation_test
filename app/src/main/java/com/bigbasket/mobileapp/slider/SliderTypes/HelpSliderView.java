package com.bigbasket.mobileapp.slider.SliderTypes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.bigbasket.mobileapp.R;
import com.facebook.drawee.view.SimpleDraweeView;

public class HelpSliderView extends BaseSliderView {

    public HelpSliderView(Context context) {
        super(context);
    }

    @Override
    public View getView() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.render_type_transparent, null);
        SimpleDraweeView target = (SimpleDraweeView) v.findViewById(R.id.daimajia_slider_image);
        bindEventAndShow(v, target);
        return v;
    }
}