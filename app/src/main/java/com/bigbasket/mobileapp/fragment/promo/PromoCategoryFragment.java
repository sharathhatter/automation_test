package com.bigbasket.mobileapp.fragment.promo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.product.PromoCategoryAdapter;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.model.promo.Promo;
import com.bigbasket.mobileapp.model.promo.PromoCategory;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.ExceptionUtil;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.ParserUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;


public class PromoCategoryFragment extends BaseFragment {

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
        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.BROWSE_PROMO_CAT,
                null, false, true, null);
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        String url = httpOperationResult.getUrl();
        if (url.contains(Constants.BROWSE_PROMO_CAT)) {
            String responseJson = httpOperationResult.getReponseString();
            JsonObject jsonObject = new JsonParser().parse(responseJson).getAsJsonObject();
            int status = jsonObject.get(Constants.STATUS).getAsInt();
            switch (status) {
                case 0:
                    JsonArray promoCategories = jsonObject.get(Constants.RESPONSE).
                            getAsJsonObject().get(Constants.PROMO_CATS).getAsJsonArray();
                    if (promoCategories != null && promoCategories.size() > 0) {
                        mPromoCategoryList = ParserUtil.parsePromoCategory(promoCategories);
                        mPromoCategoryList = filterPromoCategories();
                        if (mPromoCategoryList.size() > 0)
                            renderPromoCategories();
                        else {
                            // TODO : Improve error handling
                            showErrorMsg(getResources().getString(R.string.no_promo_cat));
                        }
                    } else {
                        showErrorMsg(getResources().getString(R.string.no_promo_cat));
                    }
                    break;
                case ExceptionUtil.PROMO_CATEGORY_NOT_EXIST:
                    showErrorMsg(getResources().getString(R.string.no_promo_cat));
                    break;
            }
        } else {
            super.onAsyncTaskComplete(httpOperationResult);
        }
    }

    private ArrayList<PromoCategory> filterPromoCategories() {
        String[] allPromoTypes = Promo.getAllTypes();
        ArrayList<PromoCategory> filteredPromoCatList = new ArrayList<>();
        for (PromoCategory promoCategory : mPromoCategoryList) {
            ArrayList<Promo> newPromos = new ArrayList<>();

            for (Promo promo : promoCategory.getPromos()) {
                if (ArrayUtils.contains(allPromoTypes, promo.getPromoType())) {
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

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_promo_category, null);
        ListView promoCategoryList = (ListView) base.findViewById(R.id.lstPromoCategory);
        PromoCategoryAdapter promoCategoryAdapter = new PromoCategoryAdapter(getActivity(),
                promoConsolidatedList, faceRobotoRegular);
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