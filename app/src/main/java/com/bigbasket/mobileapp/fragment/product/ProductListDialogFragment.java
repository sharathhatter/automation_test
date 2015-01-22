package com.bigbasket.mobileapp.fragment.product;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.adapter.product.ProductListRecyclerAdapter;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;

public class ProductListDialogFragment extends DialogFragment {

    private ArrayList<Product> products;
    private int productCount;
    private String baseImgUrl;

    public static ProductListDialogFragment newInstance(ArrayList<Product> products, int productCount, String baseImgUrl) {
        ProductListDialogFragment productListDialogFragment = new ProductListDialogFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(Constants.PRODUCTS, products);
        args.putInt(Constants.PRODUCT_COUNT, productCount);
        if (!TextUtils.isEmpty(baseImgUrl)) {
            args.putString(Constants.BASE_IMG_URL, baseImgUrl);
        }
        productListDialogFragment.setArguments(args);
        return productListDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        products = getArguments().getParcelableArrayList(Constants.PRODUCTS);
        productCount = getArguments().getInt(Constants.PRODUCT_COUNT);
        baseImgUrl = getArguments().getString(Constants.BASE_IMG_URL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.uiv3_product_list_dialog, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        UIUtil.configureRecyclerView(recyclerView, getActivity(), 1, 1);

        AuthParameters authParameters = AuthParameters.getInstance(getActivity());
        ProductViewDisplayDataHolder productViewDisplayDataHolder = new ProductViewDisplayDataHolder.Builder()
                .setCommonTypeface(BaseActivity.faceRobotoRegular)
                .setRupeeTypeface(BaseActivity.faceRupee)
                .setHandler(((HandlerAware) getActivity()).getHandler())
                .setLoggedInMember(!authParameters.isAuthTokenEmpty())
                .setShowShoppingListBtn(false)
                .setShowBasketBtn(true)
                .setShowShopListDeleteBtn(false)
                .build();

        ProductListRecyclerAdapter productListRecyclerAdapter = new ProductListRecyclerAdapter(products,
                baseImgUrl, productViewDisplayDataHolder, ((ActivityAware) getActivity()),
                productCount, TrackEventkeys.SECTION);

        recyclerView.setAdapter(productListRecyclerAdapter);

        Button btnViewAllProducts = (Button) view.findViewById(R.id.btnViewAllProducts);
        btnViewAllProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return view;
    }
}
