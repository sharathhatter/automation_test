package com.bigbasket.mobileapp.view.uiv3;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.bigbasket.mobileapp.R;

public class AnimatedLinearLayout extends LinearLayout {

    public AnimatedLinearLayout(Context context) {
        super(context);
    }

    public AnimatedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setVisibility(final int visibility) {
        if (getVisibility() != visibility) {
            if (visibility == VISIBLE) {
                Animation inAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.sub_nav_zoom_in);
                inAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        AnimatedLinearLayout.this.clearAnimation();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                startAnimation(inAnimation);
            } else {
                Animation outAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.sub_nav_zoom_out);
                outAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        AnimatedLinearLayout.this.clearAnimation();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                startAnimation(outAnimation);
            }
        }
        super.setVisibility(visibility);
    }
}
