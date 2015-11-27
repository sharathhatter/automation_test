package com.bigbasket.mobileapp.handler.click;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.models.response.SpecialityStoresInfoModel;
import com.bigbasket.mobileapp.fragment.StoreDetailsDialogFragment;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.crashlytics.android.Crashlytics;

import java.util.HashMap;

/**
 * Created by jugal on 13/11/15.
 */
public class OnSpecialityShopIconClickListener<T extends AppOperationAware> implements View.OnClickListener {

    private T context;
    private HashMap<String, SpecialityStoresInfoModel> specialityStoreInfoHashMap;

    public OnSpecialityShopIconClickListener(T context,
                                             HashMap<String, SpecialityStoresInfoModel> specialityStoreInfoHashMap) {
        this.context = context;
        this.specialityStoreInfoHashMap = specialityStoreInfoHashMap;
    }

    @Override
    public void onClick(View view) {
        if (context == null) return;
        String pStoreId = (String) view.getTag(R.id.speciality_store_id);
        if (TextUtils.isEmpty(pStoreId)) return;
        SpecialityStoresInfoModel specialityStoresInfoModel = specialityStoreInfoHashMap.get(pStoreId);
        if (specialityStoresInfoModel == null) return;
        FragmentTransaction ft = context.getCurrentActivity().getSupportFragmentManager().beginTransaction();
        Fragment f = context.getCurrentActivity().getSupportFragmentManager().findFragmentByTag("store_details_flag");
        if (f != null) {
            ft.remove(f);
        }
        StoreDetailsDialogFragment storeDetailsDialogFragment = StoreDetailsDialogFragment.newInstance(specialityStoresInfoModel.getStoreName(),
                specialityStoresInfoModel.getStoreDesc(), specialityStoresInfoModel.getStoreLogo(), specialityStoresInfoModel.getStoreCategory());
        try {
            storeDetailsDialogFragment.show(ft, "store_details_flag");
        } catch (IllegalStateException ex) {
            //Ignore
            Crashlytics.logException(ex);
        }

    }
}
