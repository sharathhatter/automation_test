package com.bigbasket.mobileapp.fragment.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.AbstractDialogFragment;
import com.crashlytics.android.Crashlytics;

public class HappyRatingFragment extends AbstractDialogFragment {

    public HappyRatingFragment() {

    }

    public static HappyRatingFragment newInstance() {
        return new HappyRatingFragment();
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog alertDialog = new Dialog(getActivity(), R.style.CustomDialog);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.uiv3_ratings_screen, null, false);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbarMain);
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.bigbasket));
            toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
            setHasOptionsMenu(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        UIUtil.updateRatingPref(getContext(), false);
                        dismiss();
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                    }
                }
            });
        }

        ImageView imgRatings = (ImageView) view.findViewById(R.id.imgRatings);
        UIUtil.displayAsyncImage(imgRatings, R.drawable.ic_rating, true);

        setCancelable(false);

        alertDialog.setContentView(view);
        alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        FontHolder fontHolder = FontHolder.getInstance(getContext());
        Typeface faceRobotoMedium = fontHolder.getFaceRobotoMedium();
        Typeface typeRobotoBold = fontHolder.getFaceRobotoBold();

        int size = (int) getResources().getDimension(R.dimen.heading_1_text_size);
        int primarySize = (int) getResources().getDimension(R.dimen.primary_text_size);

        TextView txtDialogTitle = (TextView) view.findViewById(R.id.txtDialogTitle);
        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(getString(R.string.thank_u));
        int start = spannableBuilder.length();
        spannableBuilder.setSpan(new CustomTypefaceSpan("", typeRobotoBold), 0, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableBuilder.setSpan(new AbsoluteSizeSpan(size), 0, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableBuilder.append(getString(R.string.happy_to_happy));
        int end = spannableBuilder.length();
        spannableBuilder.setSpan(new AbsoluteSizeSpan(primarySize), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        txtDialogTitle.setText(spannableBuilder);

        TextView txtDialogMsg = (TextView) view.findViewById(R.id.txtDialogMsg);
        SpannableStringBuilder spannableBuilderNew = new SpannableStringBuilder(getString(R.string.rate_us_on_playstore));
        int startNew = spannableBuilderNew.length();
        spannableBuilderNew.setSpan(new CustomTypefaceSpan("", faceRobotoMedium), 0, startNew, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableBuilderNew.setSpan(new AbsoluteSizeSpan(size), 0, startNew, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableBuilderNew.append(getString(R.string.tell_others));
        int endNew = spannableBuilderNew.length();
        spannableBuilderNew.setSpan(new AbsoluteSizeSpan(primarySize), startNew, endNew, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        txtDialogMsg.setText(spannableBuilderNew);

        view.findViewById(R.id.btnGoToPlayStore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtil.openPlayStoreLink(getContext());
                updatePrefAfterPlayStoreLaunch();
                try {
                    dismiss();
                } catch (Exception e) {
                    Crashlytics.logException(e);
                }
            }
        });

        return alertDialog;
    }

    private void updatePrefAfterPlayStoreLaunch() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constants.HAS_USER_GIVEN_RATING, true);
        editor.apply();
    }

    @Override
    protected String getScreenTag() {
        return TrackEventkeys.RATINGS_DIALOG_SCREEN;
    }
}
