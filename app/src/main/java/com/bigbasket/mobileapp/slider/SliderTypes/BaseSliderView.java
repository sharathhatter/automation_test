package com.bigbasket.mobileapp.slider.SliderTypes;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;

import com.bigbasket.mobileapp.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * When you want to make your own slider view, you must extends from this class.
 * BaseSliderView provides some useful methods.
 * if you want to show progressbar, you just need to set a progressbar id as @+id/loading_bar.
 */
public abstract class BaseSliderView {

    protected Context mContext;

    private Bundle mBundle;

    /**
     * Error place holder image.
     */
    private int mErrorPlaceHolderRes;

    /**
     * Empty imageView placeholder.
     */
    private int mEmptyPlaceHolderRes;

    private String mUrl;
    private File mFile;
    private int mRes;

    protected OnSliderClickListener mOnSliderClickListener;

    private boolean mErrorDisappear;

    private ImageLoadListener mLoadListener;

    private String mDescription;
    private int mImgWidth;
    private int mImgHeight;
    private SparseArray<Object> mKeyedTags;

    /**
     * Scale type of the image.
     */
    private ScaleType mScaleType = ScaleType.Fit;

    public enum ScaleType {
        CenterCrop, CenterInside, Fit, FitCenterCrop
    }

    protected BaseSliderView(Context context) {
        mContext = context;
        this.mBundle = new Bundle();
    }

    public void setTag(int key, Object tag) {
        // If the package id is 0x00 or 0x01, it's either an undefined package
        // or a framework id
        if ((key >>> 24) < 2) {
            throw new IllegalArgumentException("The key must be an application-specific "
                    + "resource id.");
        }
        if (mKeyedTags == null) {
            mKeyedTags = new SparseArray<>(2);
        }
        mKeyedTags.put(key, tag);
    }

    public Object getTag(int key) {
        if (mKeyedTags != null) {
            return mKeyedTags.get(key);
        }
        return null;
    }


    /**
     * the placeholder image when loading image from url or file.
     *
     * @param resId Image resource id
     * @return
     */
    public BaseSliderView empty(int resId) {
        mEmptyPlaceHolderRes = resId;
        return this;
    }

    /**
     * determine whether remove the image which failed to download or load from file
     *
     * @param disappear
     * @return
     */
    public BaseSliderView errorDisappear(boolean disappear) {
        mErrorDisappear = disappear;
        return this;
    }

    /**
     * if you set errorDisappear false, this will set a error placeholder image.
     *
     * @param resId image resource id
     * @return
     */
    public BaseSliderView error(int resId) {
        mErrorPlaceHolderRes = resId;
        return this;
    }

    /**
     * the description of a slider image.
     *
     * @param description
     * @return
     */
    public BaseSliderView description(String description) {
        mDescription = description;
        return this;
    }

    /**
     * set a url as a image that preparing to load
     *
     * @param url
     * @return
     */
    public BaseSliderView image(String url) {
        if (mFile != null || mRes != 0) {
            throw new IllegalStateException("Call multi image function," +
                    "you only have permission to call it once");
        }
        mUrl = url;
        return this;
    }

    /**
     * set a file as a image that will to load
     *
     * @param file
     * @return
     */
    public BaseSliderView image(File file) {
        if (mUrl != null || mRes != 0) {
            throw new IllegalStateException("Call multi image function," +
                    "you only have permission to call it once");
        }
        mFile = file;
        return this;
    }

    public BaseSliderView image(int res) {
        if (mUrl != null || mFile != null) {
            throw new IllegalStateException("Call multi image function," +
                    "you only have permission to call it once");
        }
        mRes = res;
        return this;
    }

    public BaseSliderView setWidth(int width) {
        this.mImgWidth = width;
        return this;
    }

    public BaseSliderView setHeight(int height) {
        this.mImgHeight = height;
        return this;
    }

    public String getUrl() {
        return mUrl;
    }

    public boolean isErrorDisappear() {
        return mErrorDisappear;
    }

    public int getEmpty() {
        return mEmptyPlaceHolderRes;
    }

    public int getError() {
        return mErrorPlaceHolderRes;
    }

    public String getDescription() {
        return mDescription;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * set a slider image click listener
     *
     * @param l
     * @return
     */
    public BaseSliderView setOnSliderClickListener(OnSliderClickListener l) {
        mOnSliderClickListener = l;
        return this;
    }

    /**
     * When you want to implement your own slider view, please call this method in the end in `getView()` method
     *
     * @param v               the whole view
     * @param targetImageView where to place image
     */
    protected void bindEventAndShow(final View v, ImageView targetImageView) {
        final BaseSliderView me = this;

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSliderClickListener != null) {
                    mOnSliderClickListener.onSliderClick(me);
                }
            }
        });

        if (targetImageView == null)
            return;

        if (mRes != 0) {
            targetImageView.setImageResource(mRes);
            targetImageView.setVisibility(View.VISIBLE);
            if (v.findViewById(R.id.loading_bar) != null) {
                v.findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
            }
            return;
        }

        mLoadListener.onStart(me);
        Uri uri;
        if (mUrl != null) {
            uri = Uri.parse(mUrl);
        } else if (mFile != null) {
            uri = Uri.fromFile(mFile);
        } else {
            return;
        }
        Picasso p = Picasso.with(mContext);
        RequestCreator rq = null;
        if (mUrl != null) {
            rq = p.load(mUrl);
        } else if (mFile != null) {
            rq = p.load(mFile);
        } else {
            return;
        }

        if (rq == null) {
            return;
        }

        if (getEmpty() != 0) {
            rq.placeholder(getEmpty());
        }

        if (getError() != 0) {
            rq.error(getError());
        }

        if (mImgWidth > 0 && mImgHeight > 0) {
            rq.resize(mImgWidth, mImgHeight).onlyScaleDown();
            Log.i(targetImageView.getContext().getClass().getName(), "Loading image (" + mImgWidth + "," + mImgHeight + ") = " + mUrl);
        } else {
            Log.i(targetImageView.getContext().getClass().getName(), "Loading image = " + mUrl);
            switch (mScaleType) {
                case Fit:
                    rq.fit();
                    break;
                case CenterCrop:
                    rq.fit().centerCrop();
                    break;
                case CenterInside:
                    rq.fit().centerInside();
                    break;
            }
        }
        rq.into(targetImageView, new OnImageDownloadedListener(v, this, mLoadListener));
    }

    private static class OnImageDownloadedListener implements Callback {

        private WeakReference<View> sliderViewWeakRef;
        private WeakReference<BaseSliderView> baseSliderViewWeakRef;
        private WeakReference<ImageLoadListener> imageLoadListenerWeakRef;

        public OnImageDownloadedListener(View sliderView, BaseSliderView baseSliderView,
                                         ImageLoadListener imageLoadListener) {
            sliderViewWeakRef = new WeakReference<>(sliderView);
            baseSliderViewWeakRef = new WeakReference<>(baseSliderView);
            imageLoadListenerWeakRef = new WeakReference<>(imageLoadListener);
        }

        @Override
        public void onSuccess() {
            if (sliderViewWeakRef != null && sliderViewWeakRef.get() != null) {
                View loadingBar = sliderViewWeakRef.get().findViewById(R.id.loading_bar);
                if (loadingBar != null) {
                    loadingBar.setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        public void onError() {
            if (imageLoadListenerWeakRef != null && imageLoadListenerWeakRef.get() != null
                    && baseSliderViewWeakRef != null && baseSliderViewWeakRef.get() != null) {
                imageLoadListenerWeakRef.get().onEnd(false, baseSliderViewWeakRef.get());
            }
            if (sliderViewWeakRef != null && sliderViewWeakRef.get() != null) {
                View loadingBar = sliderViewWeakRef.get().findViewById(R.id.loading_bar);
                if (loadingBar != null) {
                    loadingBar.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    public BaseSliderView setScaleType(ScaleType type) {
        mScaleType = type;
        return this;
    }

    public ScaleType getScaleType() {
        return mScaleType;
    }

    /**
     * the extended class have to implement getView(), which is called by the adapter,
     * every extended class response to render their own view.
     *
     * @return
     */
    public abstract View getView();

    /**
     * set a listener to get a message , if load error.
     *
     * @param l
     */
    public void setOnImageLoadListener(ImageLoadListener l) {
        mLoadListener = l;
    }

    public interface OnSliderClickListener {
        void onSliderClick(BaseSliderView slider);
    }

    /**
     * when you have some extra information, please put it in this bundle.
     *
     * @return
     */
    public Bundle getBundle() {
        return mBundle;
    }

    public interface ImageLoadListener {
        void onStart(BaseSliderView target);

        void onEnd(boolean result, BaseSliderView target);
    }

}
