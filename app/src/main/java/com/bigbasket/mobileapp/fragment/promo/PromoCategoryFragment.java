package com.bigbasket.mobileapp.fragment.promo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.product.PromoCategoryAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.BrowsePromoCategoryApiResponseContent;
import com.bigbasket.mobileapp.fragment.base.BaseSectionFragment;
import com.bigbasket.mobileapp.interfaces.PromoDetailNavigationAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.promo.Promo;
import com.bigbasket.mobileapp.model.promo.PromoCategory;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class PromoCategoryFragment extends BaseSectionFragment implements PromoDetailNavigationAware {

    private ArrayList<PromoCategory> mPromoCategoryList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.uiv3_list_container, container, false);
        view.setBackgroundColor(getResources().getColor(R.color.uiv3_list_bkg_light_color));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mPromoCategoryList = savedInstanceState.getParcelableArrayList(Constants.PROMO_CATS);
            if (mPromoCategoryList != null && mPromoCategoryList.size() > 0) {
                renderPromoCategories();
                return;
            }
        }
        getPromoCategories();
    }

    private void getPromoCategories() {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressView();
        bigBasketApiService.browsePromoCategory(new Callback<ApiResponse<BrowsePromoCategoryApiResponseContent>>() {
            @Override
            public void success(ApiResponse<BrowsePromoCategoryApiResponseContent> browsePromoCategoryApiResponse, Response response) {
                hideProgressView();
                switch (browsePromoCategoryApiResponse.status) {
                    case 0:
                        if (browsePromoCategoryApiResponse.apiResponseContent.promoCategories != null
                                && browsePromoCategoryApiResponse.apiResponseContent.promoCategories.size() > 0) {
                            mPromoCategoryList = browsePromoCategoryApiResponse.apiResponseContent.promoCategories;
                            mPromoCategoryList = filterPromoCategories();
                            mSectionData = browsePromoCategoryApiResponse.apiResponseContent.sectionData;
                            if (mPromoCategoryList.size() > 0) {
                                renderPromoCategories();
                                trackEvent(TrackingAware.PROMO_CATEGORY_LIST, null);
                            } else {
                                handler.sendEmptyMessage(browsePromoCategoryApiResponse.status);
                            }
                        } else {
                            handler.sendEmptyMessage(browsePromoCategoryApiResponse.status);
                        }
                        break;
                    case ApiErrorCodes.PROMO_CATEGORY_NOT_EXIST:
                        showErrorMsg(getResources().getString(R.string.no_promo_cat));
                        break;
                    default:
                        handler.sendEmptyMessage(browsePromoCategoryApiResponse.status);
                        break;
                }
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgressView();
            }
        });
    }

    private ArrayList<PromoCategory> filterPromoCategories() {
        Set<String> allPromoTypes = Promo.getAllTypes();
        ArrayList<PromoCategory> filteredPromoCatList = new ArrayList<>();
        for (PromoCategory promoCategory : mPromoCategoryList) {
            ArrayList<Promo> newPromos = new ArrayList<>();

            for (Promo promo : promoCategory.getPromos()) {
                if (allPromoTypes.contains(promo.getPromoType())) {
                    newPromos.add(promo);
                }
            }
            if (newPromos.size() > 0) {
                PromoCategory newPromoCategory = new PromoCategory(promoCategory);
                newPromoCategory.setPromos(newPromos);
                filteredPromoCatList.add(newPromoCategory);
            }
        }
        return filteredPromoCatList;
    }

    private void renderPromoCategories() {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;
        contentView.removeAllViews();
        final List<Object> promoConsolidatedList = new ArrayList<>();
        for (PromoCategory promoCategory : mPromoCategoryList) {
            promoConsolidatedList.add(promoCategory);
            for (Promo promo : promoCategory.getPromos()) {
                promoConsolidatedList.add(promo);
                promo.setPromoCategory(promoCategory);
            }
        }

        final View sectionLayout = getSectionView();

        ObservableRecyclerView promoCategoryListRecyclerView =
                UIUtil.getResponsiveObservableRecyclerView(getActivity(), 1, 1, contentView);
        PromoCategoryAdapter promoCategoryAdapter = new PromoCategoryAdapter<>(this,
                promoConsolidatedList, faceRobotoRegular);
        if (sectionLayout != null) {
            contentView.addView(sectionLayout);
            promoCategoryListRecyclerView.setScrollViewCallbacks(new ObservableScrollViewCallbacks() {
                @Override
                public void onScrollChanged(int i, boolean b, boolean b2) {

                }

                @Override
                public void onDownMotionEvent() {

                }

                @Override
                public void onUpOrCancelMotionEvent(ScrollState scrollState) {
                    if (scrollState == ScrollState.UP) {
                        sectionLayout.setVisibility(View.GONE);
                    } else if (scrollState == ScrollState.DOWN) {
                        sectionLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
        promoCategoryListRecyclerView.setAdapter(promoCategoryAdapter);
        contentView.addView(promoCategoryListRecyclerView);
    }

    @Override
    public void loadPromoDetail(Promo promo) {
        PromoDetailFragment promoDetailFragment = new PromoDetailFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.PROMO_ID, promo.getId());
        args.putParcelable(Constants.PROMO_CATS, promo.getPromoCategory());
        promoDetailFragment.setArguments(args);
        changeFragment(promoDetailFragment);
    }

    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public String getTitle() {
        return "Promotions";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return PromoCategoryFragment.class.getName();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPromoCategoryList != null && mPromoCategoryList.size() > 0) {
            outState.putParcelableArrayList(Constants.PROMO_CATS, mPromoCategoryList);
        }
        super.onSaveInstanceState(outState);
    }
}