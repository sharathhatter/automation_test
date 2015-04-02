package com.bigbasket.mobileapp.util;

import android.content.Context;
import android.graphics.Typeface;

public class FontHolder {
    private static FontHolder fontHolder;

    public static FontHolder getInstance(Context context) {
        if (fontHolder == null) {
            fontHolder = new FontHolder(context);
        }
        return fontHolder;
    }

    private FontHolder(Context context) {
        faceRupee = Typeface.createFromAsset(context.getAssets(), "Rupee.ttf");
        faceRobotoRegular = Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf");
        faceRobotoLight = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
    }

    private Typeface faceRupee;
    private Typeface faceRobotoRegular;
    private Typeface faceRobotoLight;


    public Typeface getFaceRupee() {
        return faceRupee;
    }

    public Typeface getFaceRobotoRegular() {
        return faceRobotoRegular;
    }

    public Typeface getFaceRobotoLight() {
        return faceRobotoLight;
    }
}
