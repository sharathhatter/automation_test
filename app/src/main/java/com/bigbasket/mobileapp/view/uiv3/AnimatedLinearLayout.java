package com.bigbasket.mobileapp.view.uiv3;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.bigbasket.mobileapp.R;

public class AnimatedLinearLayout extends LinearLayout {

    private Animation inAnimation;
    private Animation outAnimation;

    public AnimatedLinearLayout(Context context) {
        super(context);
    }

    public AnimatedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setInAnimation(Animation inAnimation) {
        this.inAnimation = inAnimation;
        this.inAnimation.setAnimationListener(new Animation.AnimationListener() {
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
    }

    public void setOutAnimation(Animation outAnimation) {
        this.outAnimation = outAnimation;
        this.outAnimation.setAnimationListener(new Animation.AnimationListener() {
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
    }

    @Override
    public void setVisibility(final int visibility) {
        if (getVisibility() != visibility) {
            if (inAnimation == null) {
                Animation slideIn = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in);
                setInAnimation(slideIn);
            }
            if (outAnimation == null) {
                Animation slideOut = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out);
                setOutAnimation(slideOut);
            }
            if (visibility == VISIBLE) {
                if (inAnimation != null) {
                    startAnimation(inAnimation);
                }
            } else {
                if (outAnimation != null) {
                    startAnimation(outAnimation);
                }
            }
        }
        super.setVisibility(visibility);
    }
}
