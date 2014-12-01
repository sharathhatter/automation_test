package com.bigbasket.mobileapp.fragment.base;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.product.ProductListRecyclerAdapter;
import com.bigbasket.mobileapp.interfaces.FilterDisplayAware;
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
     * This method is called by onActivityCreated and can be used to load products on fresh start or restore state,
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

    @Override
    public void onBackResume() {
        super.onBackResume();

        if (productListData != null) {
            ((FilterDisplayAware) getActivity()).setFilterView(productListData.getFilterOptions(),
                    productListData.getFilteredOn(), getFragmentTxnTag());
        } else {
            ((FilterDisplayAware) getActivity()).setFilterView(null, null, getFragmentTxnTag());
        }
    }

    private void setProductListView() {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;
        if (productListData == null) return;

        ((FilterDisplayAware) getActivity()).setFilterView(productListData.getFilterOptions(),
                productListData.getFilteredOn(), getFragmentTxnTag());

        RecyclerView productRecyclerView = UIUtil.getResponsiveRecyclerView(getActivity(), 2, 3);

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
        List<Product> currentProducts = productListData.getProducts();
        currentProducts.addAll(nextPageProducts);
        mProductListRecyclerAdapter.notifyDataSetChanged();
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

    public void onFilterApplied(Map<String, Set<String>> filteredOn) {
        if (productListData != null) {
            productListData.setFilteredOn(filteredOn);
            loadProducts();
        }
    }

    public void onSortViewRequested() {
        SortProductDialog sortProductDialog = SortProductDialog.newInstance(productListData.getSortedOn(),
                productListData.getSortOptions());
        sortProductDialog.setTargetFragment(getFragment(), 0);
        sortProductDialog.show(getFragmentManager(), Constants.SORT_ON);
    }
}