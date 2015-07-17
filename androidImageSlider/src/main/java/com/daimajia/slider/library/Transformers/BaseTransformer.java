package com.daimajia.slider.library.Transformers;

import android.support.v4.view.ViewPager;
import android.view.View;

import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is all transformers father.
 * <p/>
 * BaseTransformer implement {@link android.support.v4.view.ViewPager.PageTransformer}
 * which is just same as {@link android.support.v4.view.ViewPager.PageTransformer}.
 * <p/>
 * After you call setPageTransformer(), transformPage() will be called by {@link android.support.v4.view.ViewPager}
 * when your slider are animating.
 * <p/>
 * if you want to make an acceptable transformer, please do not forget to extend from this class.
 */
public abstract class BaseTransformer implements ViewPager.PageTransformer {

    /**
     * Called each {@link #transformPage(View, float)}.
     */
    protected abstract void onTransform(View view, float position);

    private HashMap<View, ArrayList<Float>> h = new HashMap<>();

    @Override
    public void transformPage(View view, float position) {
        onPreTransform(view, position);
        onTransform(view, position);
    }

    /**
     * If the position offset of a fragment is less than negative one or greater than one, returning true will set the
     * visibility of the fragment to {@link View#GONE}. Returning false will force the fragment to {@link View#VISIBLE}.
     *
     * @return
     */
    protected boolean hideOffscreenPages() {
        return true;
    }

    /**
     * Indicates if the default animations of the view pager should be used.
     *
     * @return
     */
    protected boolean isPagingEnabled() {
        return false;
    }

    /**
     * Called each {@link #transformPage(View, float)} before {{@link #onTransform(View, float)} is called.
     *
     * @param view
     * @param position
     */
    protected void onPreTransform(View view, float position) {
        final float width = view.getWidth();

        ViewHelper.setRotationX(view, 0);
        ViewHelper.setRotationY(view, 0);
        ViewHelper.setRotation(view, 0);
        ViewHelper.setScaleX(view, 1);
        ViewHelper.setScaleY(view, 1);
        ViewHelper.setPivotX(view, 0);
        ViewHelper.setPivotY(view, 0);
        ViewHelper.setTranslationY(view, 0);
        ViewHelper.setTranslationX(view, isPagingEnabled() ? 0f : -width * position);

        if (hideOffscreenPages()) {
            ViewHelper.setAlpha(view, position <= -1f || position >= 1f ? 0f : 1f);
        } else {
            ViewHelper.setAlpha(view, 1f);
        }
    }

}