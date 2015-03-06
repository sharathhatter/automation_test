package com.bigbasket.mobileapp.fragment.shoppinglist;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.GetShoppingListSummaryApiResponse;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListSummary;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ShoppingListSummaryFragment extends BaseFragment {

    private ShoppingListName mShoppingListName;
    private ArrayList<ShoppingListSummary> mShoppingListSummaries;
    private static final HashMap<String, Integer> categoryImageMap = new HashMap<>();

    static {
        categoryImageMap.put("fruits-vegetables", R.drawable.nav_f_v);
        categoryImageMap.put("grocery-staples", R.drawable.nav_g_s);
        categoryImageMap.put("bread-dairy-eggs", R.drawable.nav_bd_e);
        categoryImageMap.put("beverages", R.drawable.nav_beverages);
        categoryImageMap.put("branded-foods", R.drawable.nav_brandedfoods);
        categoryImageMap.put("personal-care", R.drawable.nav_personalcare);
        categoryImageMap.put("household", R.drawable.nav_household);
        categoryImageMap.put("imported-gourmet", R.drawable.nav_i_g);
        categoryImageMap.put("meat", R.drawable.nav_meat);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.uiv3_list_container, container, false);
        view.setBackgroundColor(getResources().getColor(R.color.uiv3_list_bkg_light_color));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        mShoppingListName = bundle.getParcelable(Constants.SHOPPING_LIST_NAME);
        if (mShoppingListName.getSlug().equalsIgnoreCase(Constants.SMART_BASKET_SLUG)) {
            setTitle(Constants.SMART_BASKET);
        }
        if (savedInstanceState != null) {
            mShoppingListSummaries = savedInstanceState.getParcelableArrayList(Constants.SHOPPING_LIST_SUMMARY);
            if (mShoppingListSummaries != null) {
                renderShoppingListSummary();
                return;
            }
        }

        loadShoppingListCategories();
    }

    @Override
    public void onBackResume() {
        super.onBackResume();
        loadShoppingListCategories();
    }

    private void loadShoppingListCategories() {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressView();
        bigBasketApiService.getShoppingListSummary(mShoppingListName.getSlug(), new Callback<GetShoppingListSummaryApiResponse>() {
            @Override
            public void success(GetShoppingListSummaryApiResponse getShoppingListSummaryApiResponse, Response response) {
                if (isSuspended()) return;
                hideProgressView();
                switch (getShoppingListSummaryApiResponse.status) {
                    case Constants.OK:
                        mShoppingListSummaries = getShoppingListSummaryApiResponse.shoppingListSummaries;
                        renderShoppingListSummary();
                        break;
                    default:
                        handler.sendEmptyMessage(getShoppingListSummaryApiResponse.getErrorTypeAsInt(),
                                getShoppingListSummaryApiResponse.message);
                        break;
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (isSuspended()) return;
                hideProgressView();
                handler.handleRetrofitError(error, true);
            }
        });
    }

    private void showNoShoppingListView(LinearLayout contentView){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_empty_data_text, contentView, false);
        ImageView imgEmptyPage = (ImageView) base.findViewById(R.id.imgEmptyPage);
        imgEmptyPage.setImageResource(R.drawable.empty_smart_basket);

        TextView txtEmptyMsg1 = (TextView) base.findViewById(R.id.txtEmptyMsg1);
        txtEmptyMsg1.setText(R.string.noSmartBasketProducts);
        TextView txtEmptyMsg2 = (TextView) base.findViewById(R.id.txtEmptyMsg2);
        txtEmptyMsg2.setVisibility(View.GONE);
        Button btnBlankPage = (Button) base.findViewById(R.id.btnBlankPage);
        btnBlankPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseActivity)getActivity()).goToHome(false);
            }
        });
        contentView.addView(base);
    }


    private void renderShoppingListSummary() {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;
        contentView.removeAllViews();
        if (mShoppingListSummaries == null || mShoppingListSummaries.size() == 0) {
            if (mShoppingListName.getSlug().equalsIgnoreCase(Constants.SMART_BASKET_SLUG)) {
//                LayoutInflater inflater = getActivity().getLayoutInflater();
//                TextView txtMsg = (TextView) inflater.inflate(R.layout.uiv3_msg_text, contentView, false);
//                txtMsg.setTypeface(faceRobotoRegular);
//                txtMsg.setText(R.string.smartBasketEmpty);
//                contentView.addView(txtMsg);

                showNoShoppingListView(contentView);
            } else {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                RelativeLayout relativeLayout = (RelativeLayout) inflater.inflate(R.layout.shopping_list_empty, contentView, false);
                TextView txtShoppingListMsg1 = (TextView) relativeLayout.findViewById(R.id.txtShoppingListMsg1);
                txtShoppingListMsg1.setTypeface(faceRobotoRegular);
                TextView txtShoppingListMsg2 = (TextView) relativeLayout.findViewById(R.id.txtShoppingListMsg2);
                txtShoppingListMsg2.setTypeface(faceRobotoRegular);
                TextView txtSearchProducts = (TextView) relativeLayout.findViewById(R.id.txtSearchProducts);
                txtSearchProducts.setTypeface(faceRobotoRegular);
                TextView txtSearchResultMsg1 = (TextView) relativeLayout.findViewById(R.id.txtSearchResultMsg1);
                txtSearchResultMsg1.setTypeface(faceRobotoRegular);
                TextView txtSearchResultMsg2 = (TextView) relativeLayout.findViewById(R.id.txtSearchResultMsg2);
                txtSearchResultMsg2.setTypeface(faceRobotoRegular);
                contentView.addView(relativeLayout);
            }
            return;
        }
        GridView shoppingListSummaryView = new GridView(getActivity());
        shoppingListSummaryView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        shoppingListSummaryView.setNumColumns(3);
        shoppingListSummaryView.setColumnWidth((int) getResources().getDimension(R.dimen.shopping_list_col_width));
        shoppingListSummaryView.setGravity(Gravity.CENTER);
        shoppingListSummaryView.setHorizontalSpacing((int) getResources().getDimension(R.dimen.padding_small));
        shoppingListSummaryView.setColumnWidth((int) getResources().getDimension(R.dimen.category_tile_width));
        ShoppingListSummaryAdapter shoppingListSummaryAdapter = new ShoppingListSummaryAdapter(mShoppingListSummaries);
        shoppingListSummaryView.setAdapter(shoppingListSummaryAdapter);
        shoppingListSummaryView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                trackEvent(TrackEventkeys.SMART_BASKET + "." + mShoppingListSummaries.get(position).getTopCategoryName() + "View", null);
                ShoppingListSummary shoppingListSummary = mShoppingListSummaries.get(position);
                Intent intent = new Intent(getActivity(), BackButtonActivity.class);
                intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SHOPPING_LIST_PRODUCTS);
                intent.putExtra(Constants.SHOPPING_LIST_NAME, mShoppingListName);
                intent.putExtra(Constants.TOP_CAT_SLUG, shoppingListSummary.getTopCategorySlug());
                intent.putExtra(Constants.TOP_CATEGORY_NAME, shoppingListSummary.getTopCategoryName());
                startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
            }
        });
        contentView.addView(shoppingListSummaryView);
        if (mShoppingListName.getSlug().equalsIgnoreCase(Constants.SMART_BASKET_SLUG)) {
            trackEvent(TrackingAware.SMART_BASKET_SUMMARY_SHOWN, null);
        } else {
            trackEvent(TrackingAware.SHOP_LST_SUMMARY_SHOWN, null);
        }

    }

    private class ShoppingListSummaryAdapter extends BaseAdapter {

        private List<ShoppingListSummary> shoppingListSummaries;
        private int size;

        private ShoppingListSummaryAdapter(List<ShoppingListSummary> shoppingListSummaries) {
            this.shoppingListSummaries = shoppingListSummaries;
            this.size = shoppingListSummaries.size();
        }

        @Override
        public int getCount() {
            return this.size;
        }

        @Override
        public Object getItem(int position) {
            return shoppingListSummaries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ShoppingListSummaryHolder shoppingListSummaryHolder;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.uiv3_shopping_list_summary, parent, false);
                shoppingListSummaryHolder = new ShoppingListSummaryHolder(row);
                row.setTag(shoppingListSummaryHolder);
            } else {
                shoppingListSummaryHolder = (ShoppingListSummaryHolder) row.getTag();
            }

            ShoppingListSummary shoppingListSummary = shoppingListSummaries.get(position);

            TextView txtCategoryName = shoppingListSummaryHolder.getTxtCategoryName();
            ImageView imgCategory = shoppingListSummaryHolder.getImgCategory();
            if (categoryImageMap.containsKey(shoppingListSummary.getTopCategorySlug())) {
                int drawableId = categoryImageMap.get(shoppingListSummary.getTopCategorySlug());
                imgCategory.setImageResource(drawableId);
            } else {
                imgCategory.setImageResource(R.drawable.bb_logo_transparent_bkg);
            }
            TextView txtNumItems = shoppingListSummaryHolder.getTxtNumItems();

            txtCategoryName.setText(shoppingListSummary.getTopCategoryName());
            txtNumItems.setText(shoppingListSummary.getNumItemsDisplay());
            return row;
        }

        private class ShoppingListSummaryHolder {
            private View base;
            private ImageView imgCategory;
            private TextView txtCategoryName;
            private TextView txtNumItems;

            private ShoppingListSummaryHolder(View base) {
                this.base = base;
            }

            public ImageView getImgCategory() {
                if (imgCategory == null) {
                    imgCategory = (ImageView) base.findViewById(R.id.imgCategory);
                }
                return imgCategory;
            }

            public TextView getTxtCategoryName() {
                if (txtCategoryName == null) {
                    txtCategoryName = (TextView) base.findViewById(R.id.txtCategoryName);
                    txtCategoryName.setTypeface(faceRobotoRegular);
                }
                return txtCategoryName;
            }

            public TextView getTxtNumItems() {
                if (txtNumItems == null) {
                    txtNumItems = (TextView) base.findViewById(R.id.txtNumItems);
                    txtNumItems.setTypeface(faceRobotoRegular);
                }
                return txtNumItems;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mShoppingListSummaries != null) {
            outState.putParcelableArrayList(Constants.SHOPPING_LIST_SUMMARY, mShoppingListSummaries);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public String getTitle() {
        return "Shopping list summary";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return ShoppingListSummaryFragment.class.getName();
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.SHOPPING_LIST_CATEGORY_LISTING_SCREEN;
    }
}