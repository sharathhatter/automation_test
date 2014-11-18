package com.bigbasket.mobileapp.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.ShopMenuOptionAdapter;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.fragment.product.TopCategoryListFragment;
import com.bigbasket.mobileapp.fragment.promo.PromoCategoryFragment;
import com.bigbasket.mobileapp.fragment.shoppinglist.ShoppingListFragment;
import com.bigbasket.mobileapp.fragment.shoppinglist.ShoppingListSummaryFragment;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.shoppinglist.ShopMenuOption;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;

import java.util.List;


public class ShopFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.uiv3_list_container, container, false);
        view.setBackgroundColor(getResources().getColor(R.color.uiv3_list_bkg_color));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadShopList();
    }

    private void loadShopList() {
        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        AuthParameters authParameters = AuthParameters.getInstance(getActivity());
        List<ShopMenuOption> shopMenuOptionList = ShopMenuOption.getShopMenuOptionList(authParameters.getBbAuthToken());
        final ListView shopOptionMenuListView = new ListView(getActivity());
        shopOptionMenuListView.setDivider(null);
        shopOptionMenuListView.setDividerHeight(0);
        shopOptionMenuListView.setAdapter(new ShopMenuOptionAdapter(getActivity(), shopMenuOptionList, faceRobotoRegular));
        shopOptionMenuListView.setOnItemClickListener(new ShopListItemClickListener());
        contentView.addView(shopOptionMenuListView);
    }

    private class ShopListItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            assert view.getTag() != null : "View must have a tag associated";
            String tag = view.getTag().toString();
            switch (tag) {
                case Constants.BROWSE_CAT:
                    changeFragment(new TopCategoryListFragment());
                    break;
                case Constants.BROWSE_OFFERS:
                    changeFragment(new PromoCategoryFragment());
                    break;
                case Constants.SHOP_LST:
                    if (AuthParameters.getInstance(getActivity()).isAuthTokenEmpty()) {
                        showErrorMsg("Please sign-in to view your shopping lists");
                    } else {
                        changeFragment(new ShoppingListFragment());
                    }
                    break;
                case Constants.QUICK_SHOP:
                    // Launch quick shop fragment
                    break;
                case Constants.SMART_BASKET_SLUG:
                    ShoppingListName shoppingListName = new ShoppingListName(Constants.SMART_BASKET,
                            Constants.SMART_BASKET_SLUG, true);
                    ShoppingListSummaryFragment shoppingListSummaryFragment = new ShoppingListSummaryFragment();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(Constants.SHOPPING_LIST_NAME, shoppingListName);
                    shoppingListSummaryFragment.setArguments(bundle);
                    changeFragment(shoppingListSummaryFragment);
                    break;
            }
        }
    }

    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public String getTitle() {
        return "Shop";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return ShopFragment.class.getName();
    }
}