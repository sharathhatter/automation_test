package com.bigbasket.mobileapp.handler;

import android.view.View;

import com.bigbasket.mobileapp.apiservice.models.response.SpecialityStoresInfoModel;
import com.bigbasket.mobileapp.fragment.StoreDetailsDialogFragment;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;

/**
 * Created by jugal on 13/11/15.
 */
public class OnSpecialityShopIconClickListener<T> implements View.OnClickListener {

    private T context;
    private SpecialityStoresInfoModel specialityStoresInfoModel;
    private static final String DIALOG_TAG = "store_details_flag";

    public OnSpecialityShopIconClickListener(T context,
                                             SpecialityStoresInfoModel specialityStoresInfoModel) {
        this.context = context;
        this.specialityStoresInfoModel = specialityStoresInfoModel;
    }

    @Override
    public void onClick(View view) {
        if (context == null) return;
        StoreDetailsDialogFragment storeDetailsDialogFragment = StoreDetailsDialogFragment.newInstance(specialityStoresInfoModel.getStoreName(),
                specialityStoresInfoModel.getStoreDesc(), specialityStoresInfoModel.getStoreLogo(), specialityStoresInfoModel.getStoreCategory());
        if (!storeDetailsDialogFragment.isVisible()) {
            storeDetailsDialogFragment.show(((AppOperationAware) context).getCurrentActivity().getSupportFragmentManager(),
                    DIALOG_TAG);
        }
    }
}
