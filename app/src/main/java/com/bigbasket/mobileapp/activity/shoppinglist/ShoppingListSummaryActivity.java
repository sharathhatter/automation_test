package com.bigbasket.mobileapp.activity.shoppinglist;

import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.adapter.TabPagerAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetShoppingListSummaryResponse;
import com.bigbasket.mobileapp.fragment.shoppinglist.ShoppingListProductFragment;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListSummary;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.view.uiv3.BBTab;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ShoppingListSummaryActivity extends BBActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadShoppingListSummary();
    }

    private void loadShoppingListSummary() {
        if (!checkInternetConnection()) {
            handler.sendOfflineError(true);
            return;
        }
        final ShoppingListName shoppingListName = getIntent().getParcelableExtra(Constants.SHOPPING_LIST_NAME);
        if (shoppingListName == null) {
            return;
        }
        setTitle(shoppingListName.getName());
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.getShoppingListSummary(shoppingListName.getSlug(), new Callback<ApiResponse<GetShoppingListSummaryResponse>>() {
            @Override
            public void success(ApiResponse<GetShoppingListSummaryResponse> getShoppingListSummaryApiResponse, Response response) {
                if (isSuspended()) return;
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                switch (getShoppingListSummaryApiResponse.status) {
                    case 0:
                        renderShoppingListSummary(shoppingListName,
                                getShoppingListSummaryApiResponse.apiResponseContent.shoppingListSummaries,
                                getShoppingListSummaryApiResponse.apiResponseContent.baseImgUrl);
                        break;
                    default:
                        handler.sendEmptyMessage(getShoppingListSummaryApiResponse.status,
                                getShoppingListSummaryApiResponse.message, true);
                        break;
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (isSuspended()) return;
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                handler.handleRetrofitError(error);
            }
        });
    }

    private void showNoShoppingListView(ViewGroup contentView) {
        LayoutInflater inflater = getLayoutInflater();
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
                if (getCurrentActivity() == null) return;
                getCurrentActivity().goToHome(false);
            }
        });
        contentView.addView(base);
    }

    private void renderShoppingListSummary(ShoppingListName shoppingListName,
                                           ArrayList<ShoppingListSummary> shoppingListSummaries,
                                           String baseImgUrl) {
        FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);
        contentFrame.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();
        if (shoppingListSummaries == null || shoppingListSummaries.size() == 0) {
            if (shoppingListName.getSlug().equals(Constants.SMART_BASKET_SLUG)) {
                showNoShoppingListView(contentFrame);
            } else {
                RelativeLayout relativeLayout = (RelativeLayout) inflater.inflate(R.layout.shopping_list_empty, contentFrame, false);
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
                contentFrame.addView(relativeLayout);
            }
            return;
        }

        if (shoppingListSummaries.size() == 1) {
            Bundle bundle = getBundleForShoppingListProductFragment(shoppingListSummaries.get(0),
                    shoppingListName, baseImgUrl);
            ShoppingListProductFragment shoppingListProductFragment = new ShoppingListProductFragment();
            shoppingListProductFragment.setArguments(bundle);
            addToMainLayout(shoppingListProductFragment);
        } else {
            View base = inflater.inflate(R.layout.uiv3_swipe_tab_view, contentFrame, false);

            ViewPager viewPager = (ViewPager) base.findViewById(R.id.pager);
            FragmentStatePagerAdapter fragmentStatePagerAdapter = new
                    TabPagerAdapter(getCurrentActivity(), getSupportFragmentManager(),
                    getTabs(shoppingListSummaries, shoppingListName, baseImgUrl));
            viewPager.setAdapter(fragmentStatePagerAdapter);

            SmartTabLayout pagerSlidingTabStrip = (SmartTabLayout) base.findViewById(R.id.slidingTabs);
            pagerSlidingTabStrip.setViewPager(viewPager);

            contentFrame.addView(base);
        }
    }

    private ArrayList<BBTab> getTabs(ArrayList<ShoppingListSummary> shoppingListSummaries,
                                     ShoppingListName shoppingListName, String baseImgUrl) {
        ArrayList<BBTab> bbTabs = new ArrayList<>();
        for (ShoppingListSummary shoppingListSummary : shoppingListSummaries) {
            if (shoppingListSummary.getNumProducts() <= 0) {
                continue;
            }
            Bundle bundle = getBundleForShoppingListProductFragment(shoppingListSummary, shoppingListName, baseImgUrl);
            bbTabs.add(new BBTab<>(shoppingListSummary.getTitle(), ShoppingListProductFragment.class, bundle));
        }
        return bbTabs;
    }

    private Bundle getBundleForShoppingListProductFragment(ShoppingListSummary shoppingListSummary,
                                                           ShoppingListName shoppingListName,
                                                           String baseImgUrl) {
        Bundle bundle = new Bundle();
        shoppingListSummary.setShoppingListName(shoppingListName);
        bundle.putParcelable(Constants.SHOPPING_LIST_SUMMARY, shoppingListSummary);
        bundle.putString(Constants.BASE_IMG_URL, baseImgUrl);
        return bundle;
    }
}
