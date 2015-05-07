package com.bigbasket.mobileapp.interfaces;

public interface InfiniteProductListAware {

    void loadMoreProducts();

    boolean isNextPageLoading();

    void setNextPageLoading(boolean isNextPageLoading);

}
