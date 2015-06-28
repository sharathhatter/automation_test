package com.bigbasket.mobileapp.view.uiv3;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.util.FontHolder;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class FloatingBadgeCountView extends FrameLayout {

    private ImageView img;
    private TextView txt;
    private static final int TRANSLATE_DURATION_MILLIS = 200;

    public FloatingBadgeCountView(Context context) {
        super(context);
    }

    public FloatingBadgeCountView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.floating_badge_count, this);
        init(context, attrs);
        this.setOnTouchListener(new OnSwipeTouchListener(context) {
            @Override
            public void onSwipeLeft() {
                int translationX = -(getScreenWidth() - getMeasuredWidth() - getMarginRight() * 2);
                ViewPropertyAnimator.animate(FloatingBadgeCountView.this).setInterpolator(mInterpolator)
                        .setDuration(TRANSLATE_DURATION_MILLIS)
                        .translationX(translationX);
            }

            @Override
            public void onSwipeRight() {
                int translationX = 0;
                ViewPropertyAnimator.animate(FloatingBadgeCountView.this).setInterpolator(mInterpolator)
                        .setDuration(TRANSLATE_DURATION_MILLIS)
                        .translationX(translationX);
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        img = (ImageView) findViewById(R.id.img);
        txt = (TextView) findViewById(R.id.txt);
        if (!isInEditMode()) {
            txt.setTypeface(FontHolder.getInstance(getContext()).getFaceRobotoMedium());
        }
    }

    public void setImg(@DrawableRes int drawableRes) {
        this.img.setImageResource(drawableRes);
    }

    public void setText(String text) {
        if (TextUtils.isEmpty(text) || text.equals("0")) {
            this.txt.setVisibility(View.GONE);
        } else {
            this.txt.setText(text);
            this.txt.setVisibility(View.VISIBLE);
        }
    }

    private int getScreenWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    private int getMarginRight() {
        int marginRight = 0;
        final ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginRight = ((MarginLayoutParams) layoutParams).rightMargin;
        }
        return marginRight;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_NORMAL, TYPE_MINI})
    public @interface TYPE {
    }

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_MINI = 1;

    private boolean mVisible;

    private int mColorNormal;
    private int mColorPressed;
    private int mColorRipple;
    private int mColorDisabled;
    private boolean mShadow;
    private int mType;

    private int mShadowSize;

    private boolean mMarginsSet;

    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = getDimension(
                mType == TYPE_NORMAL ? com.melnykov.fab.R.dimen.fab_size_normal : com.melnykov.fab.R.dimen.fab_size_mini);
        if (mShadow && !hasLollipopApi()) {
            size += mShadowSize * 2;
            setMarginsWithoutShadow();
        }
        setMeasuredDimension(size, size);
    }

    private void init(Context context, AttributeSet attributeSet) {
        mVisible = true;
        mColorNormal = getColor(com.melnykov.fab.R.color.material_blue_500);
        mColorPressed = darkenColor(mColorNormal);
        mColorRipple = lightenColor(mColorNormal);
        mColorDisabled = getColor(android.R.color.darker_gray);
        mType = TYPE_NORMAL;
        mShadow = true;
        mShadowSize = getDimension(com.melnykov.fab.R.dimen.fab_shadow_size);
        if (attributeSet != null) {
            initAttributes(context, attributeSet);
        }
        updateBackground();
    }

    private void initAttributes(Context context, AttributeSet attributeSet) {
        TypedArray attr = getTypedArray(context, attributeSet, com.melnykov.fab.R.styleable.FloatingActionButton);
        if (attr != null) {
            try {
                mColorNormal = attr.getColor(com.melnykov.fab.R.styleable.FloatingActionButton_fab_colorNormal,
                        getColor(com.melnykov.fab.R.color.material_blue_500));
                mColorPressed = attr.getColor(com.melnykov.fab.R.styleable.FloatingActionButton_fab_colorPressed,
                        darkenColor(mColorNormal));
                mColorRipple = attr.getColor(com.melnykov.fab.R.styleable.FloatingActionButton_fab_colorRipple,
                        lightenColor(mColorNormal));
                mColorDisabled = attr.getColor(com.melnykov.fab.R.styleable.FloatingActionButton_fab_colorDisabled,
                        mColorDisabled);
                mShadow = attr.getBoolean(com.melnykov.fab.R.styleable.FloatingActionButton_fab_shadow, true);
                mType = attr.getInt(com.melnykov.fab.R.styleable.FloatingActionButton_fab_type, TYPE_NORMAL);
            } finally {
                attr.recycle();
            }
        }
    }

    private void updateBackground() {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed}, createDrawable(mColorPressed));
        drawable.addState(new int[]{-android.R.attr.state_enabled}, createDrawable(mColorDisabled));
        drawable.addState(new int[]{}, createDrawable(mColorNormal));
        setBackgroundCompat(drawable);
    }

    private Drawable createDrawable(int color) {
        OvalShape ovalShape = new OvalShape();
        ShapeDrawable shapeDrawable = new ShapeDrawable(ovalShape);
        shapeDrawable.getPaint().setColor(color);

        if (mShadow && !hasLollipopApi()) {
            Drawable shadowDrawable = ContextCompat.getDrawable(getContext(),
                    mType == TYPE_NORMAL ? com.melnykov.fab.R.drawable.fab_shadow
                            : com.melnykov.fab.R.drawable.fab_shadow_mini);
            LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{shadowDrawable, shapeDrawable});
            layerDrawable.setLayerInset(1, mShadowSize, mShadowSize, mShadowSize, mShadowSize);
            return layerDrawable;
        } else {
            return shapeDrawable;
        }
    }

    private TypedArray getTypedArray(Context context, AttributeSet attributeSet, int[] attr) {
        return context.obtainStyledAttributes(attributeSet, attr, 0, 0);
    }

    private int getColor(@ColorRes int id) {
        return getResources().getColor(id);
    }

    private int getDimension(@DimenRes int id) {
        return getResources().getDimensionPixelSize(id);
    }

    private void setMarginsWithoutShadow() {
        if (!mMarginsSet) {
            if (getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
                int leftMargin = layoutParams.leftMargin - mShadowSize;
                int topMargin = layoutParams.topMargin - mShadowSize;
                int rightMargin = layoutParams.rightMargin - mShadowSize;
                int bottomMargin = layoutParams.bottomMargin - mShadowSize;
                layoutParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);

                requestLayout();
                mMarginsSet = true;
            }
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void setBackgroundCompat(Drawable drawable) {
        if (hasLollipopApi()) {
            float elevation;
            if (mShadow) {
                elevation = getElevation() > 0.0f ? getElevation()
                        : getDimension(com.melnykov.fab.R.dimen.fab_elevation_lollipop);
            } else {
                elevation = 0.0f;
            }
            setElevation(elevation);
            RippleDrawable rippleDrawable = new RippleDrawable(new ColorStateList(new int[][]{{}},
                    new int[]{mColorRipple}), drawable, null);
            setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    int size = getDimension(mType == TYPE_NORMAL ? com.melnykov.fab.R.dimen.fab_size_normal
                            : com.melnykov.fab.R.dimen.fab_size_mini);
                    outline.setOval(0, 0, size, size);
                }
            });
            setClipToOutline(true);
            setBackground(rippleDrawable);
        } else if (hasJellyBeanApi()) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }

    private int getMarginBottom() {
        int marginBottom = 0;
        final ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
        }
        return marginBottom;
    }

    public void setType(@TYPE int type) {
        if (type != mType) {
            mType = type;
            updateBackground();
        }
    }

    @TYPE
    public int getType() {
        return mType;
    }

    public boolean isVisible() {
        return mVisible;
    }

    public void show() {
        show(true);
    }

    public void hide() {
        hide(true);
    }

    public void show(boolean animate) {
        toggle(true, animate, false);
    }

    public void hide(boolean animate) {
        toggle(false, animate, false);
    }

    private void toggle(final boolean visible, final boolean animate, boolean force) {
        if (mVisible != visible || force) {
            mVisible = visible;
            int height = getHeight();
            if (height == 0 && !force) {
                ViewTreeObserver vto = getViewTreeObserver();
                if (vto.isAlive()) {
                    vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            ViewTreeObserver currentVto = getViewTreeObserver();
                            if (currentVto.isAlive()) {
                                currentVto.removeOnPreDrawListener(this);
                            }
                            toggle(visible, animate, true);
                            return true;
                        }
                    });
                    return;
                }
            }
            int translationY = visible ? 0 : height + getMarginBottom();
            if (animate) {
                ViewPropertyAnimator.animate(this).setInterpolator(mInterpolator)
                        .setDuration(TRANSLATE_DURATION_MILLIS)
                        .translationY(translationY);
            } else {
                ViewHelper.setTranslationY(this, translationY);
            }

            // On pre-Honeycomb a translated view is still clickable, so we need to disable clicks manually
            if (!hasHoneycombApi()) {
                setClickable(visible);
            }
        }
    }

    private boolean hasLollipopApi() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    private boolean hasJellyBeanApi() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    private boolean hasHoneycombApi() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    private static int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.9f;
        return Color.HSVToColor(hsv);
    }

    private static int lightenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 1.1f;
        return Color.HSVToColor(hsv);
    }
}
