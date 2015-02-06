package com.bigbasket.mobileapp.fragment.product;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.adapter.product.ProductListRecyclerAdapter;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProductListDialogAware;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;
import java.util.List;

public class ProductListDialogFragment extends DialogFragment {

    public static final int SHOW_ALL = -1;
    private ArrayList<Product> products;
    private int totalProductCount;
    private String baseImgUrl;
    private String title;
    private int normalListCount;
    private int tabletListCount;

    public static ProductListDialogFragment newInstance(@Nullable String title,
                                                        ArrayList<Product> products, int productCount,
                                                        @Nullable String baseImgUrl, int normalListCount,
                                                        int tabletListCount) {
        ProductListDialogFragment productListDialogFragment = new ProductListDialogFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(Constants.PRODUCTS, products);
        args.putInt(Constants.PRODUCT_COUNT, productCount);
        if (!TextUtils.isEmpty(baseImgUrl)) {
            args.putString(Constants.BASE_IMG_URL, baseImgUrl);
        }
        if (!TextUtils.isEmpty(title)) {
            args.putString(Constants.TITLE, title);
        }
        args.putInt(Constants.NORMAL_COUNT, normalListCount);
        args.putInt(Constants.TABLET_COUNT, tabletListCount);
        productListDialogFragment.setArguments(args);
        return productListDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        products = getArguments().getParcelableArrayList(Constants.PRODUCTS);
        totalProductCount = getArguments().getInt(Constants.PRODUCT_COUNT);
        baseImgUrl = getArguments().getString(Constants.BASE_IMG_URL);
        normalListCount = getArguments().getInt(Constants.NORMAL_COUNT);
        tabletListCount = getArguments().getInt(Constants.TABLET_COUNT);
        title = getArguments().getString(Constants.TITLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (!TextUtils.isEmpty(title) && getDialog() != null) {
            getDialog().setTitle(title);
        }
        View view = inflater.inflate(R.layout.uiv3_product_list_dialog, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        UIUtil.configureRecyclerView(recyclerView, getActivity(), 2, 2);

        List<Product> shortenedProductsList;
        int productCount;
        if (normalListCount > SHOW_ALL && tabletListCount > SHOW_ALL) {
            productCount = recyclerView.getLayoutManager() instanceof LinearLayoutManager ? normalListCount : tabletListCount;
            if (products.size() > productCount) {
                shortenedProductsList = products.subList(0, productCount);
            } else {
                shortenedProductsList = products;
            }
        } else {
            shortenedProductsList = products;
            productCount = totalProductCount;
        }
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

        ProductListRecyclerAdapter productListRecyclerAdapter = new ProductListRecyclerAdapter(shortenedProductsList,
                baseImgUrl, productViewDisplayDataHolder, ((ActivityAware) getActivity()),
                productCount, TrackEventkeys.SECTION);

        recyclerView.setAdapter(productListRecyclerAdapter);

        TextView txtCancel = (TextView) view.findViewById(R.id.txtCancel);
        TextView txtViewAllProducts = (TextView) view.findViewById(R.id.txtViewAllProducts);
        if (getShowsDialog()) {
            txtCancel.setTypeface(BaseActivity.faceRobotoRegular);
            txtViewAllProducts.setTypeface(BaseActivity.faceRobotoRegular);
            txtCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            txtViewAllProducts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ProductListDialogAware) getActivity()).showDialog(title, products,
                            totalProductCount, baseImgUrl, false, getTag());
                    dismiss();
                }
            });
        } else {
            LinearLayout layoutProductDialogButtons = (LinearLayout) view.findViewById(R.id.layoutProductDialogButtons);
            layoutProductDialogButtons.setVisibility(View.GONE);
        }
        return view;
    }
}
