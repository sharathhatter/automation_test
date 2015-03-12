package com.bigbasket.mobileapp.fragment.product;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.ArrayList;

public class SearchFragment extends ProductListAwareFragment {

    @Nullable
    @Override
    public ArrayList<NameValuePair> getInputForApi() {
        String searchQuery = getArguments().getString(Constants.SEARCH_QUERY);
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new NameValuePair(Constants.TYPE, ProductListType.SEARCH.get()));
        nameValuePairs.add(new NameValuePair(Constants.SLUG, searchQuery));
        return nameValuePairs;
    }

    @Override
    @Nullable
    public String getTitle() {
        return getArguments() != null ? getArguments().getString(Constants.SEARCH_QUERY):
                "Search Products";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return SearchFragment.class.getName();
    }

    @Override
    public void updateData() {
        super.updateData();
    }

    @Override
    public String getNavigationCtx() {
        return TrackEventkeys.NAVIGATION_CTX_PRODUCT_SEARCH;
    }

    @Override

    protected void showNoProductsFoundView(LinearLayout contentView){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View emptyPageView = inflater.inflate(R.layout.uiv3_empty_data_text, contentView, false);
        ImageView imgEmptyPage = (ImageView) emptyPageView.findViewById(R.id.imgEmptyPage);
        imgEmptyPage.setVisibility(View.INVISIBLE);
        TextView txtEmptyMsg1 = (TextView) emptyPageView.findViewById(R.id.txtEmptyMsg1);
        txtEmptyMsg1.setText(R.string.noProductsSearchMsg);
        TextView txtEmptyMsg2 = (TextView) emptyPageView.findViewById(R.id.txtEmptyMsg2);
        txtEmptyMsg2.setVisibility(View.GONE);
        Button btnBlankPage = (Button) emptyPageView.findViewById(R.id.btnBlankPage);
        btnBlankPage.setText(R.string.continue_shopping_txt);
        btnBlankPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseActivity)getActivity()).goToHome(false);
            }
        });
        contentView.addView(emptyPageView);
    }
}
