package com.bigbasket.mobileapp.fragment.base;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.product.ProductListRecyclerAdapter;
import com.bigbasket.mobileapp.interfaces.ProductListDataAware;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.interfaces.SortAware;
import com.bigbasket.mobileapp.model.product.Option;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductListData;
import com.bigbasket.mobileapp.model.product.ProductQuery;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListOption;
import com.bigbasket.mobileapp.task.uiv3.ProductListTask;
import com.bigbasket.mobileapp.task.uiv3.ShoppingListDoAddDeleteTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.FilterProductDialog;
import com.bigbasket.mobileapp.view.uiv3.SortProductDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public abstract class ProductListAwareFragment extends BaseFragment implements ProductListDataAware,
        ShoppingListNamesAware, SortAware {

    private ProductListData productListData;
    private ArrayList<ShoppingListName> shoppingListNames;
    private String selectedProductId;
    private ProductListRecyclerAdapter mProductListRecyclerAdapter;
    private View mFooterView;
    private boolean mIsNextPageLoading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        restoreProductList(savedInstanceState);
    }

    /**
     * This method is called by onActivityCreated and can is used to load products on fresh start or restore state,
     * when screen is rotated
     *
     * @param savedInstanceState Bundle containing parceled data, when the screen was rotated
     */
    public void restoreProductList(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            productListData = savedInstanceState.getParcelable(Constants.PRODUCTS);
            updateData();
        } else {
            loadProducts();
        }
    }

    public void loadProducts() {
        getProductListAsyncTask().execute();
    }

    public void loadMoreProducts() {
        if (isNextPageLoading()) return;
        int nextPage = productListData.getCurrentPage() + 1;
        if (nextPage <= productListData.getTotalPages()) {
            setNextPageLoading(true);
            if (mFooterView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                mFooterView = inflater.inflate(R.layout.uiv3_list_loading_footer, null);
            }
            //mProductRecyclerView.addFooterView(mFooterView, null, false);
            getProductListAsyncTask(nextPage).execute();
        }
    }

    public String getProductListUrl() {
        return MobileApiUrl.getBaseAPIUrl() + Constants.PRODUCT_LIST_URL + "/?";
    }

    public AsyncTask getProductListAsyncTask() {
        return new ProductListTask<>(this);
    }

    public AsyncTask getProductListAsyncTask(int page) {
        return new ProductListTask<>(page, this);
    }

    public ProductListData getProductListData() {
        return productListData;
    }

    public void setProductListData(ProductListData productListData) {
        this.productListData = productListData;
    }

    @Override
    public void updateData() {
        setProductListView();
    }

    private void setProductListView() {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;
        if (productListData == null) return;

        LayoutInflater inflater = getActivity().getLayoutInflater();

        RecyclerView productRecyclerView = UIUtil.getResponsiveRecyclerView(getActivity());

        // Set product-list header view
        View headerView = inflater.inflate(R.layout.uiv3_product_list_filter_layout, null);
        LinearLayout layoutFilterBy = (LinearLayout) headerView.findViewById(R.id.layoutFilterBy);
        RelativeLayout layoutSortBy = (RelativeLayout) headerView.findViewById(R.id.layoutSortBy);
        LinearLayout layoutFilterSort = (LinearLayout) headerView.findViewById(R.id.layoutFilterSort);
        TextView txtNumProducts = (TextView) headerView.findViewById(R.id.txtNumProducts);
        txtNumProducts.setText(getNumProductsMessage());

        contentView.addView(headerView);
        //mProductRecyclerView.addHeaderView(headerView, null, false);

        TextView txtFilterBy = (TextView) headerView.findViewById(R.id.txtFilterBy);
        TextView txtSortedOnValue = (TextView) headerView.findViewById(R.id.txtSortedOnValue);

        if (productListData.getProductCount() > 0) {
            if (productListData.getFilterOptions().size() > 0) {
                layoutFilterBy.setVisibility(View.VISIBLE);
                layoutFilterBy.setOnTouchListener(new FilterOptionsOnClickListener());
                int filterDrawableId = productListData.isFilterSelected() ? R.drawable.filter_applied : R.drawable.no_filter;
                txtFilterBy.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(filterDrawableId), null, null, null);
            } else {
                layoutFilterBy.setVisibility(View.GONE);
            }

            if (productListData.getSortOptions().size() > 0) {
                layoutSortBy.setVisibility(View.VISIBLE);
                layoutSortBy.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;
                    }
                });
                layoutSortBy.setOnTouchListener(new SortOptionsOnClickListener());
                txtSortedOnValue.setText(productListData.getSortedOnDisplay());
            } else {
                layoutSortBy.setVisibility(View.GONE);
            }
        } else {
            layoutFilterSort.setVisibility(View.GONE);
        }
        // Set product-list data
        AuthParameters authParameters = AuthParameters.getInstance(getActivity());
        ProductViewDisplayDataHolder productViewDisplayDataHolder = new ProductViewDisplayDataHolder.Builder()
                .setCommonTypeface(faceRobotoRegular)
                .setRupeeTypeface(faceRupee)
                .setHandler(handler)
                .setLoggedInMember(!authParameters.isAuthTokenEmpty())
                .setShowShoppingListBtn(true)
                .setShowBasketBtn(true)
                .setShowShopListDeleteBtn(false)
                .build();
        mProductListRecyclerAdapter = new ProductListRecyclerAdapter(productListData.getProducts(), null,
                getCurrentActivity(), productViewDisplayDataHolder, this, productListData.getProductCount());

        productRecyclerView.setAdapter(mProductListRecyclerAdapter);
        contentView.addView(productRecyclerView);
    }

    @Override
    public void updateProductList(List<Product> nextPageProducts) {
        if (productListData == null || mProductListRecyclerAdapter == null) return;
        setNextPageLoading(false);
        //mProductRecyclerView.removeFooterView(mFooterView);
        List<Product> currentProducts = productListData.getProducts();
        currentProducts.addAll(nextPageProducts);
        mProductListRecyclerAdapter.notifyDataSetChanged();
    }

    public String getNumProductsMessage() {
        if (productListData == null) return null;
        String msg = "No products found";
        if (productListData.getProductCount() > 0) {
            int firstCount = (productListData.getCurrentPage() - 1) * Constants.PAGE_SIZE + 1;
            int endCount;
            if (productListData.getCurrentPage() * Constants.PAGE_SIZE > productListData.getProductCount()) {
                endCount = productListData.getProductCount();
            } else {
                endCount = productListData.getCurrentPage() * Constants.PAGE_SIZE;
            }
            String products = "product" + (productListData.getProductCount() > 1 ? "s" : "");
            msg = "Showing " + firstCount + " - " + endCount + " of " +
                    productListData.getProductCount() + " " + products;
        }
        if (!TextUtils.isEmpty(productListData.getQuery())) {
            msg += " for \"" + productListData.getQuery() + "\"";
        }
        return msg;
    }

    public abstract String getProductListSlug();

    public abstract String getProductQueryType();

    public ProductQuery getProductQuery() {
        Map<String, Set<String>> filteredOnMap = null;
        String sortedOn = null;
        ProductListData productListData = getProductListData();
        if (productListData != null) {
            filteredOnMap = productListData.getFilteredOn();
            sortedOn = productListData.getSortedOn();
        }
        return new ProductQuery(getProductQueryType(),
                getProductListSlug(), sortedOn, filteredOnMap, 1);
    }

    @Override
    public ArrayList<ShoppingListName> getShoppingListNames() {
        return shoppingListNames;
    }

    @Override
    public void setShoppingListNames(ArrayList<ShoppingListName> shoppingListNames) {
        this.shoppingListNames = shoppingListNames;
    }

    @Override
    public String getSelectedProductId() {
        return selectedProductId;
    }

    @Override
    public void setSelectedProductId(String selectedProductId) {
        this.selectedProductId = selectedProductId;
    }

    @Override
    public void postShoppingListItemDeleteOperation() {

    }

    private ProductListAwareFragment getFragment() {
        return this;
    }

    @Nullable
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    private class FilterOptionsOnClickListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.setBackgroundColor(Color.YELLOW);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                v.setBackgroundColor(Color.TRANSPARENT);
                FilterProductDialog filterProductDialog = FilterProductDialog.newInstance(
                        productListData.getFilterOptions(), productListData.getFilteredOn());
                filterProductDialog.setTargetFragment(getFragment(), 0);
                filterProductDialog.show(getFragmentManager(), Constants.FILTER_ON);
            }
            return true;
        }
    }

    private class SortOptionsOnClickListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.setBackgroundColor(Color.YELLOW);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                v.setBackgroundColor(Color.TRANSPARENT);
                SortProductDialog sortProductDialog = SortProductDialog.newInstance(productListData.getSortedOn(),
                        productListData.getSortOptions());
                sortProductDialog.setTargetFragment(getFragment(), 0);
                sortProductDialog.show(getFragmentManager(), Constants.SORT_ON);
            }
            return true;
        }
    }

    @Override
    public List<Option> getSortOptions() {
        return productListData.getSortOptions();
    }

    @Override
    public String getSortedOn() {
        return productListData.getSortedOn();
    }

    @Override
    public void setSortedOn(String sortedOn) {
        this.productListData.setSortedOn(sortedOn);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        parcelProductList(outState);
        super.onSaveInstanceState(outState);
    }

    /**
     * This method is called, just before the activity is destroyed, to parcel ProductListData,
     * which is reused, when screen rotation takes place
     *
     * @param outState Bundle that stores the product list
     */
    public void parcelProductList(Bundle outState) {
        if (productListData != null) {
            outState.putParcelable(Constants.PRODUCTS, productListData);
        }
    }

    @Override
    public boolean isNextPageLoading() {
        return mIsNextPageLoading;
    }

    @Override
    public void setNextPageLoading(boolean isNextPageLoading) {
        this.mIsNextPageLoading = isNextPageLoading;
    }

    @Override
    public void addToShoppingList(List<ShoppingListName> selectedShoppingListNames) {
        if (getActivity() == null) return;
        if (selectedShoppingListNames == null || selectedShoppingListNames.size() == 0) {
            Toast.makeText(getActivity(), getString(R.string.chooseShopList), Toast.LENGTH_SHORT).show();
            return;
        }
        ShoppingListDoAddDeleteTask shoppingListDoAddDeleteTask =
                new ShoppingListDoAddDeleteTask<>(this,
                        MobileApiUrl.getBaseAPIUrl() + "sl-add-item/", selectedShoppingListNames,
                        ShoppingListOption.ADD_TO_LIST);
        shoppingListDoAddDeleteTask.execute();
    }
}