package com.bigbasket.mobileapp.fragment;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetShopsResponse;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.fragment.product.ShopInShopFragment;
import com.bigbasket.mobileapp.model.general.ShopInShop;
import com.bigbasket.mobileapp.util.Constants;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ShopInShopListFragment extends BaseFragment {

    private ArrayList<ShopInShop> mShopInShopList;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mShopInShopList = savedInstanceState.getParcelableArrayList(Constants.SPECIAL_SHOPS);
            if (mShopInShopList != null) {
                renderShops();
                return;
            }
        }
        loadShops();
    }

    private void loadShops() {
        if (checkInternetConnection()) {
            BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
            showProgressView();
            bigBasketApiService.getShops(new Callback<ApiResponse<GetShopsResponse>>() {
                @Override
                public void success(ApiResponse<GetShopsResponse> getShopsApiResponse, Response response) {
                    if (isSuspended()) return;
                    try {
                        hideProgressView();
                    } catch (IllegalArgumentException e) {
                        return;
                    }
                    switch (getShopsApiResponse.status) {
                        case 0:
                            mShopInShopList = getShopsApiResponse.apiResponseContent.shops;
                            renderShops();
                            break;
                        default:
                            handler.sendEmptyMessage(getShopsApiResponse.status, getShopsApiResponse.message, true);
                            break;
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    if (isSuspended()) return;
                    try {
                        hideProgressView();
                    } catch (IllegalArgumentException e) {
                        return;
                    }
                    handler.handleRetrofitError(error);
                }
            });
        } else {
            handler.sendOfflineError();
        }
    }

    private void renderShops() {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;
        contentView.removeAllViews();

        if (mShopInShopList == null || mShopInShopList.size() == 0) {
            View base = getActivity().getLayoutInflater().inflate(R.layout.uiv3_empty_data_text, contentView, false);
            TextView txtEmptyDataMsg = (TextView) base.findViewById(R.id.txtEmptyDataMsg);
            txtEmptyDataMsg.setTypeface(faceRobotoRegular);
            txtEmptyDataMsg.setText(getString(R.string.shopEmpty));
            contentView.addView(base);
            return;
        }

        ListView listView = new ListView(getActivity());
        listView.setDivider(new ColorDrawable(getResources().getColor(R.color.uiv3_list_separator_color)));
        ShopInShopListAdapter shopInShopListAdapter = new ShopInShopListAdapter();
        listView.setAdapter(shopInShopListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (getCurrentActivity() == null) return;
                ShopInShop shopInShop = mShopInShopList.get(position);
                ShopInShopFragment shopInShopFragment = new ShopInShopFragment();
                Bundle args = new Bundle();
                args.putString(Constants.SLUG, shopInShop.getSlug());
                shopInShopFragment.setArguments(args);
                changeFragment(shopInShopFragment);
            }
        });
        contentView.addView(listView);
    }

    private class ShopInShopListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mShopInShopList.size();
        }

        @Override
        public Object getItem(int position) {
            return mShopInShopList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder viewHolder;
            if (row == null) {
                row = getActivity().getLayoutInflater().inflate(R.layout.uiv3_list_text, parent, false);
                viewHolder = new ViewHolder(row);
                row.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) row.getTag();
            }

            TextView txtListText = viewHolder.getTxtListText();
            txtListText.setText(mShopInShopList.get(position).getName());
            return row;
        }

        private class ViewHolder {
            private View base;
            private TextView txtListText;

            private ViewHolder(View base) {
                this.base = base;
            }

            public TextView getTxtListText() {
                if (txtListText == null) {
                    txtListText = (TextView) base.findViewById(R.id.txtListText);
                    txtListText.setTypeface(faceRobotoRegular);
                }
                return txtListText;
            }
        }
    }

    @Override
    public String getTitle() {
        return getString(R.string.specialShops);
    }

    @Nullable
    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return ShopInShopListFragment.class.getName();
    }
}
