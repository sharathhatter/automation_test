package com.bigbasket.mobileapp.application;

import android.app.Application;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AuthParameters.updateInstance(this);
        //Configure ImageLoader
        DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .showImageOnLoading(R.drawable.loading)
                .showImageForEmptyUri(R.drawable.image_404)
                .showImageOnFail(R.drawable.image_404)
                .delayBeforeLoading(100)
                .build();

        ImageLoaderConfiguration imageLoaderConfiguration = new
                ImageLoaderConfiguration.Builder(getApplicationContext())
                .memoryCacheExtraOptions(480, 800)
                .diskCacheExtraOptions(480, 800, null)  //Bitmap.CompressFormat.PNG, 75, null
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(20 * 1024 * 1024))
                .threadPriority(Thread.NORM_PRIORITY - 1)
                .threadPoolSize(3)
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .diskCacheSize(50 * 1024 * 1024)
                .defaultDisplayImageOptions(displayImageOptions)
                .build();
        ImageLoader.getInstance().init(imageLoaderConfiguration);
    }

}
