package com.bigbasket.mobileapp.view;

import android.content.Context;
import android.widget.ListView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;


public class ProductListView extends ListView {

    public ProductListView(Context context) {
        super(context);
        ListView listViewProducts = new ListView(context);
        PauseOnScrollListener pauseOnScrollListener = new PauseOnScrollListener(ImageLoader.getInstance(), true, true);
        listViewProducts.setOnScrollListener(pauseOnScrollListener);
    }
}
