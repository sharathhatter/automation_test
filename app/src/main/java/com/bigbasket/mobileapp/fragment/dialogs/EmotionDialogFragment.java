package com.bigbasket.mobileapp.fragment.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.AbstractDialogFragment;
import com.crashlytics.android.Crashlytics;

public class EmotionDialogFragment extends AbstractDialogFragment {

    private boolean isOutSideTouch = true;

    public EmotionDialogFragment() {

    }

    public static EmotionDialogFragment newInstance() {
        return new EmotionDialogFragment();
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.uiv3_emotions_layout, null, false);

        LinearLayout layoutHappy = (LinearLayout) view.findViewById(R.id.layoutHappy);
        LinearLayout layoutSad = (LinearLayout) view.findViewById(R.id.layoutSad);

        ImageView imgHappy = (ImageView) view.findViewById(R.id.imgHappy);
        ImageView imgSad = (ImageView) view.findViewById(R.id.imgSad);
        UIUtil.displayAsyncImage(imgHappy, R.drawable.ic_happy, true);
        UIUtil.displayAsyncImage(imgSad, R.drawable.ic_sad, true);

        layoutHappy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TrackingAware) getActivity()).trackEvent(TrackingAware.HAPPY_CLICKED, null);
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag("rating_happy_flag");
                if (f != null) {
                    ft.remove(f);
                }
                HappyRatingFragment happyRatingFragment = HappyRatingFragment.newInstance();
                try {
                    happyRatingFragment.show(ft, "rating_happy_flag");
                    isOutSideTouch = false;
                    dismiss();
                } catch (Exception ex) {
                    Crashlytics.logException(ex);
                }
            }
        });
        layoutSad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TrackingAware) getActivity()).trackEvent(TrackingAware.SAD_CLICKED, null);
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag("rating_sad_flag");
                if (f != null) {
                    ft.remove(f);
                }
                SadFeedbackFragment sadFeedbackFragment = SadFeedbackFragment.newInstance();
                try {
                    sadFeedbackFragment.show(ft, "rating_sad_flag");
                    isOutSideTouch = false;
                    dismiss();
                } catch (Exception ex) {
                    Crashlytics.logException(ex);
                }
            }
        });
        builder.setView(view);
        setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        return alertDialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        try {
            if (isOutSideTouch) {
                UIUtil.updateRatingPref(getContext(), false);
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        super.onDismiss(dialog);
    }

    @Override
    protected String getScreenTag() {
        return TrackEventkeys.EMOTION_DIALOG_SCREEN;
    }
}
