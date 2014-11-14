package com.bigbasket.mobileapp.fragment.shoppinglist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.bigbasket.mobileapp.view.ProductListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;


public class ShoppingListProductFragment extends ProductListAwareFragment {

    private String topCatName;
    private String topCatSlug;
    private ShoppingListName shoppingListName;

    @Override
    public void loadProducts() {
        loadShoppingListProducts();
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
        topCatSlug = bundle.getString(Constants.TOP_CAT_SLUG);
        topCatName = bundle.getString(Constants.TOP_CATEGORY_NAME);
        shoppingListName = bundle.getParcelable(Constants.SHOPPING_LIST_NAME);

        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.SLUG, shoppingListName.getSlug());
        if (shoppingListName.getSlug().equalsIgnoreCase(Constants.SMART_BASKET_SLUG)) {
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
                        String baseImgUrl = responseJsonObj.getString(Constants.BASE_IMG_URL);
                        JSONArray shoppingListItemsJsonArray = responseJsonObj.getJSONArray(Constants.SHOPPING_LIST_ITEMS);
                        renderShoppingListItems(baseImgUrl, shoppingListItemsJsonArray);
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

    private void renderShoppingListItems(String baseImgUrl, JSONArray shoppingListItemsJsonArray) {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;
        contentView.removeAllViews();
        try {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            for (int i = 0; i < shoppingListItemsJsonArray.length(); i++) {
                JSONObject jsonObject = shoppingListItemsJsonArray.getJSONObject(i);
                JSONArray productJsonArray = jsonObject.getJSONArray(Constants.ITEMS);
                String topname = jsonObject.getString(Constants.TOP_CATEGORY_NAME);
                View shopListHeaderLayout = inflater.inflate(R.layout.my_shopping_list_listheader, null);
                TextView brandNameTxt = (TextView) shopListHeaderLayout.findViewById(R.id.brandNameTxt);
                brandNameTxt.setTypeface(faceRobotoRegular);
                topname = topname.replaceAll("<br/>", " ");
                brandNameTxt.setText(UIUtil.abbreviate(topname, 25));
                List<Product> productList = ParserUtil.parseProductJsonArray(productJsonArray, baseImgUrl);
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

    private void renderProducts(List<Product> productList) {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        ProductListView listViewShopListProducts = new ProductListView(getActivity());
        ProductViewDisplayDataHolder productViewDisplayDataHolder = new
                ProductViewDisplayDataHolder(faceRobotoRegular, faceRobotoRegular, faceRobotoRegular, faceRupee,
                !AuthParameters.getInstance(getBaseActivity()).isAuthTokenEmpty(), true, true,
                !shoppingListName.isSystem(),
                new MessageHandler(getBaseActivity()), shoppingListName);
        ProductListAdapter productListAdapter = new ProductListAdapter(productList, null,
                getBaseActivity(), productViewDisplayDataHolder, this, 1);
        listViewShopListProducts.setAdapter(productListAdapter);
        contentView.addView(listViewShopListProducts);
    }

    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
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