package com.bigbasket.mobileapp.fragment.shoppinglist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.product.ProductListAdapter;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.handler.MessageHandler;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.ParserUtil;
import com.bigbasket.mobileapp.util.UIUtil;
import com.etsy.android.grid.StaggeredGridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class ShoppingListProductFragment extends ProductListAwareFragment {

    private ShoppingListName mShoppingListName;
    private JSONArray mShoppingListItemsInfoJsonArray;
    private String mBaseImgUrl;

    @Override
    public void loadProducts() {
        loadShoppingListProducts();
    }

    @Override
    public void loadMoreProducts() {
        // Do nothing
    }

    @Override
    public void updateData() {
        // Do nothing
    }

    @Override
    public void restoreProductList(Bundle savedInstanceState) {
        mShoppingListName = getArguments().getParcelable(Constants.SHOPPING_LIST_NAME);
        if (savedInstanceState != null) {
            String shoppingListItemsJsonStr = savedInstanceState.getString(Constants.SHOPPING_LIST_ITEMS);
            if (shoppingListItemsJsonStr != null) {
                try {
                    mShoppingListItemsInfoJsonArray = new JSONArray(shoppingListItemsJsonStr);
                    mBaseImgUrl = savedInstanceState.getString(Constants.BASE_IMG_URL);
                    renderShoppingListItems();
                    return;
                } catch (JSONException e) {
                }
            }
        }
        loadProducts();
    }

    @Override
    public String getProductListSlug() {
        return null;
    }

    @Override
    public String getProductQueryType() {
        return null;
    }

    private void loadShoppingListProducts() {
        Bundle bundle = getArguments();
        String topCatSlug = bundle.getString(Constants.TOP_CAT_SLUG);

        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.SLUG, mShoppingListName.getSlug());
        if (mShoppingListName.getSlug().equalsIgnoreCase(Constants.SMART_BASKET_SLUG)) {
            setTitle("Smart Basket Products");
        }
        params.put(Constants.TOP_CAT_SLUG, topCatSlug);
        String url = MobileApiUrl.getBaseAPIUrl() + Constants.SL_GET_LIST_DETAILS;
        startAsyncActivity(url, params, true, true, null);
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        try {
            String url = httpOperationResult.getUrl();
            if (url.contains(Constants.SL_GET_LIST_DETAILS)) {
                JSONObject httpResponseJsonObj = new JSONObject(httpOperationResult.getReponseString());
                int status = httpResponseJsonObj.getInt(Constants.STATUS);
                switch (status) {
                    case 0:
                        JSONObject responseJsonObj = httpResponseJsonObj.getJSONObject(Constants.RESPONSE);
                        mBaseImgUrl = responseJsonObj.getString(Constants.BASE_IMG_URL);
                        mShoppingListItemsInfoJsonArray = responseJsonObj.getJSONArray(Constants.SHOPPING_LIST_ITEMS);
                        renderShoppingListItems();
                        break;
                    default:
                        showErrorMsg("Server Error");
                        break;
                }
            } else {
                super.onAsyncTaskComplete(httpOperationResult);
            }
        } catch (JSONException e) {
            // TODO : Improved json handling
            showErrorMsg("Server Error");
        }
    }

    private void renderShoppingListItems() {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;
        contentView.removeAllViews();
        try {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            for (int i = 0; i < mShoppingListItemsInfoJsonArray.length(); i++) {
                JSONObject jsonObject = mShoppingListItemsInfoJsonArray.getJSONObject(i);
                JSONArray productJsonArray = jsonObject.getJSONArray(Constants.ITEMS);
                String topname = jsonObject.getString(Constants.TOP_CATEGORY_NAME);
                View shopListHeaderLayout = inflater.inflate(R.layout.my_shopping_list_listheader, null);
                TextView brandNameTxt = (TextView) shopListHeaderLayout.findViewById(R.id.brandNameTxt);
                brandNameTxt.setTypeface(faceRobotoRegular);
                topname = topname.replaceAll("<br/>", " ");
                brandNameTxt.setText(UIUtil.abbreviate(topname, 25));
                ArrayList<Product> productList = ParserUtil.parseProductJsonArray(productJsonArray, mBaseImgUrl);
                Button btnAddAllToBasket = (Button) shopListHeaderLayout.findViewById(R.id.btnAddAllToBasket);
                if (Product.areAllProductsOutOfStock(productList)) {
                    btnAddAllToBasket.setVisibility(View.GONE);
                } else {
                    btnAddAllToBasket.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (checkInternetConnection()) {
//                            showAlertDialog(getCurrentActivity(), null, "Are you sure you want to add all products to the basket?",
//                                    "addall");
                            } else {
                                Toast.makeText(getActivity(), "No network connection", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                contentView.addView(shopListHeaderLayout);
                renderProducts(productList);
            }
        } catch (JSONException e) {
            Toast.makeText(getActivity(), "No products in this list", Toast.LENGTH_LONG).show();
        }
    }

    private void renderProducts(ArrayList<Product> productList) {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View base = inflater.inflate(R.layout.product_list, null);
        AbsListView productListView = (AbsListView) base.findViewById(R.id.lstProducts);

        ProductViewDisplayDataHolder productViewDisplayDataHolder = new
                ProductViewDisplayDataHolder(faceRobotoRegular, faceRobotoRegular, faceRobotoRegular, faceRupee,
                !AuthParameters.getInstance(getBaseActivity()).isAuthTokenEmpty(), true, true,
                !mShoppingListName.isSystem(),
                new MessageHandler(getBaseActivity()), mShoppingListName);
        ProductListAdapter productListAdapter = new ProductListAdapter(productList, null,
                getBaseActivity(), productViewDisplayDataHolder, this, 1);

        // AbsListView doesn't have setAdapter below API 11, hence doing this way
        if (productListView instanceof ListView) {
            ((ListView) productListView).setAdapter(productListAdapter);
        } else if (productListView instanceof StaggeredGridView) {
            ((StaggeredGridView) productListView).setAdapter(productListAdapter);
        }
        contentView.addView(base);
    }

    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public void parcelProductList(Bundle outState) {
        if (mShoppingListItemsInfoJsonArray != null) {
            outState.putString(Constants.SHOPPING_LIST_ITEMS, mShoppingListItemsInfoJsonArray.toString());
            outState.putString(Constants.BASE_IMG_URL, mBaseImgUrl);
        }
    }

    @Override
    public String getTitle() {
        return "Shopping List Products";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return ShoppingListProductFragment.class.getName();
    }
}