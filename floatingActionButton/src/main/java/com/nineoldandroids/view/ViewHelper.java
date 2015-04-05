package com.nineoldandroids.view;

import android.view.View;

import static com.nineoldandroids.view.animation.AnimatorProxy.NEEDS_PROXY;
import static com.nineoldandroids.view.animation.AnimatorProxy.wrap;

public final class ViewHelper {
    private ViewHelper() {}

    public static void setTranslationY(View view, float translationY) {
        if (NEEDS_PROXY) {
            wrap(view).setTranslationY(translationY);
        } else {
            Honeycomb.setTranslationY(view, translationY);
        }
    }

    private static final class Honeycomb {

        static void setTranslationY(View view, float translationY) {
            view.setTranslationY(translationY);
        }
    }
}
