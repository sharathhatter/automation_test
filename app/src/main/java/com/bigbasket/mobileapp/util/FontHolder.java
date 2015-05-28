package com.bigbasket.mobileapp.util;

import android.content.Context;
import android.graphics.Typeface;

public class FontHolder {
    private static FontHolder fontHolder;
    private Typeface faceRupee;
    private Typeface faceRobotoRegular;
    private Typeface faceRobotoLight;
    private Typeface faceRobotoItalic;
    private Typeface faceRobotoMedium;
    private Typeface faceRobotoBold;

    private FontHolder(Context context) {
        faceRupee = Typeface.createFromAsset(context.getAssets(), "Rupee.ttf");
        faceRobotoRegular = Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf");
        faceRobotoLight = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
        faceRobotoItalic = Typeface.createFromAsset(context.getAssets(), "Roboto-Italic.ttf");
        faceRobotoMedium = Typeface.createFromAsset(context.getAssets(), "Roboto-Medium.ttf");
        faceRobotoBold = Typeface.createFromAsset(context.getAssets(), "Roboto-Bold.ttf");
    }

    public static FontHolder getInstance(Context context) {
        if (fontHolder == null) {
            fontHolder = new FontHolder(context);
        }
        return fontHolder;
    }

    public Typeface getFaceRupee() {
        return faceRupee;
    }

    public Typeface getFaceRobotoRegular() {
        return faceRobotoRegular;
    }

    public Typeface getFaceRobotoLight() {
        return faceRobotoLight;
    }

    public Typeface getFaceRobotoItalic() {
        return faceRobotoItalic;
    }

    public Typeface getFaceRobotoMedium() {
        return faceRobotoMedium;
    }

    public Typeface getFaceRobotoBold() {
        return faceRobotoBold;
    }
}
