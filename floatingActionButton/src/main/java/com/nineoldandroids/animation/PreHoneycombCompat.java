package com.nineoldandroids.animation;

import android.view.View;
import com.nineoldandroids.util.FloatProperty;
import com.nineoldandroids.util.IntProperty;
import com.nineoldandroids.util.Property;
import com.nineoldandroids.view.animation.AnimatorProxy;

final class PreHoneycombCompat {
    static Property<View, Float> TRANSLATION_Y = new FloatProperty<View>("translationY") {
        @Override
        public void setValue(View object, float value) {
            AnimatorProxy.wrap(object).setTranslationY(value);
        }

        @Override
        public Float get(View object) {
            return AnimatorProxy.wrap(object).getTranslationY();
        }
    };
    static Property<View, Integer> SCROLL_Y = new IntProperty<View>("scrollY") {
        @Override
        public void setValue(View object, int value) {
            AnimatorProxy.wrap(object).setScrollY(value);
        }

        @Override
        public Integer get(View object) {
            return AnimatorProxy.wrap(object).getScrollY();
        }
    };
    static Property<View, Float> Y = new FloatProperty<View>("y") {
        @Override
        public void setValue(View object, float value) {
            AnimatorProxy.wrap(object).setY(value);
        }

        @Override
        public Float get(View object) {
            return AnimatorProxy.wrap(object).getY();
        }
    };


    //No instances
    private PreHoneycombCompat() {}
}
