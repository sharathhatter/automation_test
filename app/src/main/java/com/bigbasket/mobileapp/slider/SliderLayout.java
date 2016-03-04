package com.bigbasket.mobileapp.slider;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.slider.Indicators.PagerIndicator;
import com.bigbasket.mobileapp.slider.SliderTypes.BaseSliderView;
import com.bigbasket.mobileapp.slider.Transformers.BaseTransformer;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

/**
 * SliderLayout is compound layout. This is combined with {@link PagerIndicator}
 * <p/>
 * There is some properties you can set in XML:
 * <p/>
 * indicator_visibility
 * visible
 * invisible
 * <p/>
 * indicator_shape
 * oval
 * rect
 * <p/>
 * indicator_selected_color
 * <p/>
 * indicator_unselected_color
 * <p/>
 * indicator_selected_drawable
 * <p/>
 * indicator_unselected_drawable
 * <p/>
 * pager_animation
 * Default
 * Accordion
 * Background2Foreground
 * CubeIn
 * DepthPage
 * Fade
 * FlipHorizontal
 * FlipPage
 * Foreground2Background
 * RotateDown
 * RotateUp
 * Stack
 * Tablet
 * ZoomIn
 * ZoomOutSlide
 * ZoomOut
 * <p/>
 * pager_animation_span
 */
public class SliderLayout extends RelativeLayout {

    /**
     * InfiniteViewPager is extended from ViewPagerEx. As the name says, it can scroll without bounder.
     */
    private InfiniteViewPager mViewPager;

    /**
     * InfiniteViewPager adapter.
     */
    private SliderAdapter mSliderAdapter;

    private PagerIndicator mIndicator;


    private Timer mCycleTimer;
    private TimerTask mCycleTask;

    private Timer mResumingTimer;
    private TimerTask mResumingTask;

    private boolean mCycling;

    private boolean mAutoRecover = true;
    private boolean mAutoCycle;
    private AutoCycleHandler mh;
    /**
     * the duration between animation.
     */
    private long mSliderDuration = 4000;

    private boolean mIsInfiniteScroll = true;

    /**
     * Visibility of {@link PagerIndicator}
     */
    private PagerIndicator.IndicatorVisibility mIndicatorVisibility = PagerIndicator.IndicatorVisibility.Visible;


    public SliderLayout(Context context) {
        this(context, null);
        this.mh = new AutoCycleHandler(this);
    }

    public SliderLayout(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.SliderStyle);
        this.mh = new AutoCycleHandler(this);
    }

    public SliderLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mh = new AutoCycleHandler(this);
        LayoutInflater.from(context).inflate(R.layout.slider_layout, this, true);

        final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SliderLayout,
                defStyle, 0);

        int transformerSpan = attributes.getInteger(R.styleable.SliderLayout_pager_animation_span, 1100);
        mAutoCycle = attributes.getBoolean(R.styleable.SliderLayout_auto_cycle, true);
        int visibility = attributes.getInt(R.styleable.SliderLayout_indicator_visibility, 0);
        for (PagerIndicator.IndicatorVisibility v : PagerIndicator.IndicatorVisibility.values()) {
            if (v.ordinal() == visibility) {
                mIndicatorVisibility = v;
                break;
            }
        }
        mSliderAdapter = new SliderAdapter();
        PagerAdapter wrappedAdapter = new InfinitePagerAdapter(mSliderAdapter);

        mViewPager = (InfiniteViewPager) findViewById(R.id.daimajia_slider_viewpager);
        if (mIsInfiniteScroll) {
            mViewPager.setAdapter(wrappedAdapter);
        } else {
            mViewPager.setAdapter(mSliderAdapter);
            mSliderAdapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    if (mSliderAdapter.getCount() <= 10) {
                        mViewPager.setOffscreenPageLimit(mSliderAdapter.getCount());
                    } else {
                        mViewPager.setOffscreenPageLimit(10);
                    }
                }
            });
        }
        mViewPager.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_UP:
                        recoverCycle();
                        break;
                }
                return false;
            }
        });

        attributes.recycle();
        setPresetIndicator(PresetIndicators.Center_Bottom);
        setSliderTransformDuration(transformerSpan, null);
        setIndicatorVisibility(mIndicatorVisibility);

    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (mAutoCycle) {
            if (visibility == View.VISIBLE) {
                startAutoCycle();
            } else {
                stopAutoCycle();
            }
        }
    }

    public void setAutoCycle(boolean autoCycle) {
        this.mAutoCycle = autoCycle;
        if (isShown()) {
            if (autoCycle) {
                startAutoCycle();
            } else {
                stopAutoCycle();
            }
        }
    }

    public void setCustomIndicator(PagerIndicator indicator) {
        if (mIndicator != null) {
            mIndicator.destroySelf();
        }
        mIndicator = indicator;
        mIndicator.setIndicatorVisibility(mIndicatorVisibility);
        mIndicator.setViewPager(mViewPager);
        mIndicator.redraw();
    }

    public <T extends BaseSliderView> void addSlider(T imageContent) {
        mSliderAdapter.addSlider(imageContent);
    }

    public <T extends BaseSliderView> void addSliders(Collection<? extends T> imageContent) {
        mSliderAdapter.addSlider(imageContent);
    }

    public <T extends BaseSliderView> void changeSliders(Collection<? extends T> imageContent) {
        mSliderAdapter.changeSliders(imageContent);
    }

    private static class AutoCycleHandler extends Handler {
        private WeakReference<SliderLayout> sliderLayoutRef;

        public AutoCycleHandler(SliderLayout sliderLayout) {
            this.sliderLayoutRef = new WeakReference<>(sliderLayout);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (sliderLayoutRef != null && sliderLayoutRef.get() != null) {
                sliderLayoutRef.get().moveNextPosition(true);
            }
        }
    }

    private static class AutoCycleTimerTask extends TimerTask {
        private AutoCycleHandler autoCycleHandler;

        public AutoCycleTimerTask(AutoCycleHandler autoCycleHandler) {
            this.autoCycleHandler = autoCycleHandler;
        }

        @Override
        public void run() {
            autoCycleHandler.sendEmptyMessage(0);
        }
    }

    public void startAutoCycle() {
        startAutoCycle(mSliderDuration, mSliderDuration, mAutoRecover);
    }

    /**
     * start auto cycle.
     *
     * @param delay       delay time
     * @param duration    animation duration time.
     * @param autoRecover if recover after user touches the slider.
     */
    private void startAutoCycle(long delay, long duration, boolean autoRecover) {
        if (mCycleTimer != null) mCycleTimer.cancel();
        if (mCycleTask != null) mCycleTask.cancel();
        if (mResumingTask != null) mResumingTask.cancel();
        if (mResumingTimer != null) mResumingTimer.cancel();
        mSliderDuration = duration;
        mCycleTimer = new Timer();
        mAutoRecover = autoRecover;
        mCycleTask = new AutoCycleTimerTask(mh);
        mCycleTimer.schedule(mCycleTask, delay, mSliderDuration);
        mCycling = true;
    }

    /**
     * pause auto cycle.
     */
    private void pauseAutoCycle() {
        if (mCycling) {
            mCycleTimer.cancel();
            mCycleTask.cancel();
            mCycling = false;
        } else {
            if (mResumingTimer != null && mResumingTask != null) {
                recoverCycle();
            }
        }
    }

    public void setPagerTransformer(boolean reverseDrawingOrder, BaseTransformer transformer) {
        mViewPager.setPageTransformer(reverseDrawingOrder, transformer);
    }

    /**
     * stop the auto circle
     */
    private void stopAutoCycle() {

        if (mCycleTask != null) {
            mCycleTask.cancel();
        }
        if (mCycleTimer != null) {
            mCycleTimer.cancel();
        }
        if (mResumingTimer != null) {
            mResumingTimer.cancel();
        }
        if (mResumingTask != null) {
            mResumingTask.cancel();
        }
        mCycling = false;
    }

    /**
     * when paused cycle, this method can weak it up.
     */
    private void recoverCycle() {
        if (!mAutoRecover || !mAutoCycle) {
            return;
        }

        if (!mCycling) {
            if (mResumingTask != null && mResumingTimer != null) {
                mResumingTimer.cancel();
                mResumingTask.cancel();
            }
            mResumingTimer = new Timer();
            mResumingTask = new TimerTask() {
                @Override
                public void run() {
                    startAutoCycle();
                }
            };
            mResumingTimer.schedule(mResumingTask, 6000);
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                pauseAutoCycle();
                break;
        }
        return false;
    }

    /**
     * set the duration between two slider changes.
     *
     * @param period
     * @param interpolator
     */
    public void setSliderTransformDuration(int period, Interpolator interpolator) {
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(mViewPager.getContext(), interpolator, period);
            mScroller.set(mViewPager, scroller);
        } catch (Exception e) {

        }
    }

    /**
     * Set the visibility of the indicators.
     *
     * @param visibility
     */
    public void setIndicatorVisibility(PagerIndicator.IndicatorVisibility visibility) {
        if (mIndicator == null) {
            return;
        }

        mIndicator.setIndicatorVisibility(visibility);
    }

    public enum PresetIndicators {
        Center_Bottom("Center_Bottom", R.id.default_center_bottom_indicator),
        Right_Bottom("Right_Bottom", R.id.default_bottom_right_indicator),
        Left_Bottom("Left_Bottom", R.id.default_bottom_left_indicator),
        Center_Top("Center_Top", R.id.default_center_top_indicator),
        Right_Top("Right_Top", R.id.default_center_top_right_indicator),
        Left_Top("Left_Top", R.id.default_center_top_left_indicator);

        private final String name;
        private final int id;

        PresetIndicators(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public String toString() {
            return name;
        }

        public int getResourceId() {
            return id;
        }
    }

    public void setPresetIndicator(PresetIndicators presetIndicator) {
        PagerIndicator pagerIndicator = (PagerIndicator) findViewById(presetIndicator.getResourceId());
        setCustomIndicator(pagerIndicator);
    }

    private SliderAdapter getRealAdapter() {
        PagerAdapter adapter = mViewPager.getAdapter();
        if (adapter instanceof InfinitePagerAdapter) {
            return ((InfinitePagerAdapter) adapter).getRealAdapter();
        }
        if (adapter instanceof SliderAdapter) {
            return (SliderAdapter) adapter;
        }
        return null;
    }

    /**
     * move to next slide.
     */
    public void moveNextPosition(boolean smooth) {
        SliderAdapter realAdapter = getRealAdapter();
        if (realAdapter == null)
            throw new IllegalStateException("You did not set a slider adapter");

        int nextItem = mViewPager.getCurrentItem() + 1;
        if (!mIsInfiniteScroll) {
            int realCount = realAdapter.getCount();
            if (nextItem >= realCount) {
                nextItem = 0;
                smooth = false;
            }
        }

        mViewPager.setCurrentItem(nextItem, smooth);
    }
}
