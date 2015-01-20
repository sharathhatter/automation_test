package com.daimajia.slider.library.SliderTypes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.widget.ImageView;

import com.daimajia.slider.library.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Hashtable;

/**
 * When you want to make your own slider view, you must extends from this class.
 * BaseSliderView provides some useful methods.
 * I provide two example: {@link com.daimajia.slider.library.SliderTypes.DefaultSliderView} and
 * {@link com.daimajia.slider.library.SliderTypes.TextSliderView}
 * if you want to show progressbar, you just need to set a progressbar id as @+id/loading_bar.
 */
public abstract class BaseSliderView {

    protected Context mContext;

    private Bundle mBundle;
    private Hashtable<String, Integer> swatchMap;

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
        this.swatchMap = new Hashtable<>();
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

        mLoadListener.onStart(me);

        ImageLoader imageLoader = ImageLoader.getInstance();

//        Picasso p = Picasso.with(mContext);
//        RequestCreator rq = null;
        if (mUrl != null) {
            imageLoader.displayImage(mUrl, targetImageView, new ImageLoaderListener(v));
        }
//        else if(mFile != null){
//            imageLoader.loadImage(mFile, targetImageView, new ImageLoaderListener(v));
//        }else if(mRes != 0){
//            rq = p.load(mRes);
//        }
//        else{
//            return;
//        }
//
//        if(getEmpty() != 0){
//            rq.placeholder(getEmpty());
//        }
//
//        if(getError() != 0){
//            rq.error(getError());
//        }
//
//        switch (mScaleType){
//            case Fit:
//                rq.fit();
//                break;
//            case CenterCrop:
//                rq.fit().centerCrop();
//                break;
//            case CenterInside:
//                rq.fit().centerInside();
//                break;
//        }
//
//        rq.into(targetImageView,new Callback() {
//            @Override
//            public void onSuccess() {
//                if(v.findViewById(R.id.loading_bar) != null){
//                    v.findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
//                }
//            }
//
//            @Override
//            public void onError() {
//                if(mLoadListener != null){
//                    mLoadListener.onEnd(false,me);
//                }
//                if(v.findViewById(R.id.loading_bar) != null){
//                    v.findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
//                }
//            }
//        });
    }

    private class ImageLoaderListener extends SimpleImageLoadingListener {

        private View v;

        public ImageLoaderListener(View v) {
            this.v = v;
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (v.findViewById(R.id.loading_bar) != null) {
                v.findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
            }
//            if (view != null && view instanceof ImageView) {
//                Integer bkgColor = swatchMap.get(imageUri);
//                if (bkgColor == null) {
//                    Palette.generateAsync(loadedImage, new PaletteAsyncListener((ImageView) view, imageUri));
//                } else {
//                    setImageBackgroundSwatch((ImageView) view, bkgColor);
//                }
//            }
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            if (v.findViewById(R.id.loading_bar) != null) {
                v.findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
            }
        }
    }

    private class PaletteAsyncListener implements Palette.PaletteAsyncListener {

        private WeakReference<ImageView> imgView;
        private String imageUri;

        private PaletteAsyncListener(ImageView imgView, String imageUri) {
            this.imgView = new WeakReference<>(imgView);
            this.imageUri = imageUri;
        }

        @Override
        public void onGenerated(Palette palette) {
            if (imgView != null && imgView.get() != null) {
                Palette.Swatch swatch = palette.getLightMutedSwatch();
                int bkgColor = swatch != null ? swatch.getRgb() : Color.WHITE;
                swatchMap.put(imageUri, bkgColor);
                if (imgView.get() != null) {
                    setImageBackgroundSwatch(imgView.get(), bkgColor);
                }
            }
        }
    }

    private void setImageBackgroundSwatch(ImageView targetImageView, int bkgColor) {
        targetImageView.setBackgroundColor(bkgColor);
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
        public void onSliderClick(BaseSliderView slider);
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
        public void onStart(BaseSliderView target);

        public void onEnd(boolean result, BaseSliderView target);
    }

}
