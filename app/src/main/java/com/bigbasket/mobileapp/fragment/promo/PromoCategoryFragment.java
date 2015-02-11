package com.bigbasket.mobileapp.fragment.promo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.product.PromoCategoryAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.BrowsePromoCategoryApiResponseContent;
import com.bigbasket.mobileapp.fragment.base.BaseSectionFragment;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.promo.Promo;
import com.bigbasket.mobileapp.model.promo.PromoCategory;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class PromoCategoryFragment extends BaseSectionFragment {

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

        View sectionLayout = getSectionView();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_promo_category, contentView, false);
        ListView promoCategoryList = (ListView) base.findViewById(R.id.lstPromoCategory);
        PromoCategoryAdapter promoCategoryAdapter = new PromoCategoryAdapter(getActivity(),
                promoConsolidatedList, faceRobotoRegular);
        if (sectionLayout != null) {
            promoCategoryList.addHeaderView(sectionLayout);
        }
        promoCategoryList.setAdapter(promoCategoryAdapter);
        promoCategoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (getActivity() == null) return;
                Object possiblePromoObj = promoConsolidatedList.get(position);
                if (possiblePromoObj instanceof Promo) {
                    loadPromoDetail((Promo) possiblePromoObj);
                }
            }
        });
        contentView.addView(base);
    }

    private void loadPromoDetail(Promo promo) {
        Intent intent = new Intent(getActivity(), BackButtonActivity.class);
        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PROMO_DETAIL);
        intent.putExtra(Constants.PROMO_ID, promo.getId());
        intent.putExtra(Constants.PROMO_CATS, promo.getPromoCategory());
        intent.putExtra(Constants.PROMO_NAME, promo.getPromoName());
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
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