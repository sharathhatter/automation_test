package com.bigbasket.mobileapp.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.handler.click.OnSectionItemClickListener;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.model.section.DestinationInfo;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.AbstractDialogFragment;

public class StoreDetailsDialogFragment extends AbstractDialogFragment {

    Bundle detailsBundle;

    public StoreDetailsDialogFragment() {

    }

    public static StoreDetailsDialogFragment newInstance(String title, @Nullable String desc,
                                                         @Nullable String imgUrl,
                                                         @Nullable String category) {
        StoreDetailsDialogFragment storeDetailsDialogFragment = new StoreDetailsDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.STORE_NAME, title);
        bundle.putString(Constants.STORE_DESC, desc);
        bundle.putString(Constants.STORE_IMG, imgUrl);
        bundle.putString(Constants.STORE_CATEGORY, category);
        storeDetailsDialogFragment.setArguments(bundle);
        return storeDetailsDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            detailsBundle = bundle;
        }
    }

/*    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.store_details_layout, container, false);
    }*/

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.store_details_layout, null, false);

        String title = detailsBundle.getString(Constants.STORE_NAME);
        TextView titleView = (TextView) view.findViewById(R.id.dialogTitle);
        titleView.setTypeface(FontHolder.getInstance(getActivity()).getFaceRobotoRegular(), Typeface.BOLD);
        if (!TextUtils.isEmpty(title)) {
            titleView.setText(title);
        } else {
            titleView.setText(R.string.speciality_shop_title);
        }

        ImageView storeIcon = (ImageView) view.findViewById(R.id.storeIcon);
        TextView storeDesc = (TextView) view.findViewById(R.id.txtStoreDesc);
        storeDesc.setTypeface(FontHolder.getInstance(getActivity()).getFaceRobotoRegular());

        String desc = detailsBundle.getString(Constants.STORE_DESC);
        if (!TextUtils.isEmpty(desc)) {
            storeDesc.setText(desc);
        }
        String imgUrl = detailsBundle.getString(Constants.STORE_IMG);
        UIUtil.displayAsyncImage(storeIcon, imgUrl);

        builder.setView(view);

        builder.setNegativeButton(R.string.dismiss, null);

        builder.setPositiveButton(R.string.visit_store, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String storeCategory = detailsBundle.getString(Constants.STORE_CATEGORY);
                if (!TextUtils.isEmpty(storeCategory)) {
                    DestinationInfo destinationInfo = new DestinationInfo(DestinationInfo.STORE_LIST, storeCategory);
                    if (getDialog().isShowing())
                        getDialog().dismiss();
                    new OnSectionItemClickListener<>((AppOperationAware) getActivity()).handleDestinationClick(destinationInfo);
                }
            }
        });
        builder.setCancelable(true);
        return builder.create();
    }

    /*@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getView();
        if (view != null && detailsBundle != null) {
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
                    txtDismiss.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
                    if (getDialog().isShowing())
                        getDialog().dismiss();
                }
            });
            txtVisitStore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String storeCategory = detailsBundle.getString(Constants.STORE_CATEGORY);
                    if (!TextUtils.isEmpty(storeCategory)) {
                        DestinationInfo destinationInfo = new DestinationInfo(DestinationInfo.STORE_LIST, storeCategory);
                        if (getDialog().isShowing())
                            getDialog().dismiss();
                        new OnSectionItemClickListener<>((AppOperationAware) getActivity()).handleDestinationClick(destinationInfo);
                    }
                }
            });
            String title = detailsBundle.getString(Constants.STORE_NAME);
            if (!TextUtils.isEmpty(title)) {
                dialogTitle.setText(title);
            }
            String desc = detailsBundle.getString(Constants.STORE_DESC);
            if (!TextUtils.isEmpty(desc)) {
                storeDesc.setText(desc);
            }
            String imgUrl = detailsBundle.getString(Constants.STORE_IMG);
            UIUtil.displayAsyncImage(storeIcon, imgUrl);
        }
    }*/

    @Override
    protected String getScreenTag() {
        return TrackEventkeys.SPECIALITY_STORE_DETAILS_DIALOG;
    }
}
