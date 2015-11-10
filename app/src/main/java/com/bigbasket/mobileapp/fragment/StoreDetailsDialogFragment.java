package com.bigbasket.mobileapp.fragment;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.view.uiv3.AbstractDialogFragment;

public class StoreDetailsDialogFragment extends AbstractDialogFragment {

    public StoreDetailsDialogFragment() {

    }

    public static StoreDetailsDialogFragment newInstance() {
        StoreDetailsDialogFragment storeDetailsDialogFragment = new StoreDetailsDialogFragment();
        return storeDetailsDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.store_details_layout, container, false);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getView();
        if (view != null) {
            TextView dialogTitle = (TextView) view.findViewById(R.id.dialogTitle);
            dialogTitle.setTypeface(FontHolder.getInstance(getActivity()).getFaceRobotoRegular(), Typeface.BOLD);
            ImageView storeIcon = (ImageView) view.findViewById(R.id.storeIcon);
            TextView storeDesc = (TextView) view.findViewById(R.id.txtStoreDesc);
            storeDesc.setTypeface(FontHolder.getInstance(getActivity()).getFaceRobotoRegular());
            final TextView txtDismiss = (TextView) view.findViewById(R.id.txtDismiss);
            final TextView txtVisitStore = (TextView) view.findViewById(R.id.txtVisitStore);

            txtDismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    txtDismiss.setBackgroundColor(getResources().getColor(R.color.white));
                    if (getDialog().isShowing())
                        getDialog().dismiss();
                }
            });
            txtVisitStore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }

    @Override
    protected String getScreenTag() {
        return TrackEventkeys.STORE_DETAILS;
    }
}
