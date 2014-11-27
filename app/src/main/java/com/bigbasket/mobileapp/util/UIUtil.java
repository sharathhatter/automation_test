package com.bigbasket.mobileapp.util;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BulletSpan;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class UIUtil {

    /**
     * @param pixel : Value in Pixel, which should be scaled according to screen density
     * @return : Scaled Pixel Value
     */
    public static int scaleToScreenIndependentPixel(int pixel, Context ctx) {
        final float SCALE = ctx.getResources().getDisplayMetrics().density;
        return (int) (pixel * SCALE + 0.5f);
    }

    public static String round(double x) {
        return String.format("%.2f", x);
    }

    public static String roundOrInt(double x) {
        return x == (int) x ? String.valueOf((int) x) : round(x);
    }

    public static TextView getPromoMsgTextView(Context context) {
        TextView textView = new TextView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(scaleToScreenIndependentPixel(30, context),
                scaleToScreenIndependentPixel(2, context), scaleToScreenIndependentPixel(10, context),
                scaleToScreenIndependentPixel(5, context));
        textView.setLayoutParams(layoutParams);
        textView.setTextColor(context.getResources().getColor(R.color.redColor));
        textView.setTextSize(scaleToScreenIndependentPixel(14, context));
        return textView;
    }


    public static List<Spannable> createBulletSpannableList(ArrayList<String> criteriaMsgs) {
        ArrayList<Spannable> criteriaSpannableList = null;
        if (criteriaMsgs != null && criteriaMsgs.size() > 0) {
            criteriaSpannableList = new ArrayList<>();
            for (String criteriaMsg : criteriaMsgs) {
                if (criteriaMsg != null) {
                    Spannable spannable = new SpannableString(criteriaMsg);
                    spannable.setSpan(new BulletSpan(15), 0, criteriaMsg.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    spannable.setSpan(new AbsoluteSizeSpan(14, true), 0, criteriaMsg.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    criteriaSpannableList.add(spannable);
                }
            }
        }
        return criteriaSpannableList;
    }

    public static String sentenceJoin(List<String> lst) {
        return sentenceJoin(lst, "and");
    }

    public static String sentenceJoin(List<String> lst, String separator) {
        if (lst == null || lst.size() == 0)
            return "";
        int lenLst = lst.size();
        switch (lenLst) {
            case 1:
                return lst.get(0);
            case 2:
                return lst.get(0) + " " + separator + " " + lst.get(1);
            default:
                return strJoin(lst.subList(0, lenLst - 1), ", ") + " " + separator + " " + lst.get(lenLst - 1);
        }
    }

    public static String strJoin(List<String> stringList, String separator) {
        StringBuilder sbr = new StringBuilder();
        int len = stringList.size();
        for (int i = 0; i < len; i++) {
            sbr.append(stringList.get(i));
            if (i != len - 1) {
                sbr.append(separator);
            }
        }
        return sbr.toString();
    }


    public static String strJoin(String[] stringArray, String separator) {
        StringBuilder sbr = new StringBuilder();
        int len = stringArray.length;
        for (int i = 0; i < len; i++) {
            sbr.append(stringArray[i]);
            if (i != len - 1) {
                sbr.append(separator);
            }
        }
        return sbr.toString();
    }

    public static String abbreviate(String txt, int sz) {
        if (txt.length() <= sz) {
            return txt;
        }
        String abbreviatedTxt = txt.substring(0, sz - 4);
        return abbreviatedTxt + "...";
    }

    public static boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static Spannable asRupeeSpannable(String amtTxt, Typeface faceRupee) {
        String rupeeSym = "`";
        SpannableString spannableString = new SpannableString(rupeeSym + " " + amtTxt);
        spannableString.setSpan(new CustomTypefaceSpan("", faceRupee), 0, rupeeSym.length(),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        return spannableString;
    }

    public static Spannable asRupeeSpannable(double amt, Typeface faceRupee) {
        return asRupeeSpannable(formatAsMoney(amt), faceRupee);
    }

    public static String formatAsMoney(Double amount) {
        int amountInt = amount.intValue();
        if (amountInt == amount)
            return String.valueOf(amountInt);
        final NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        nf.setGroupingUsed(false);
        return (nf.format(amount).equals("0.00") || nf.format(amount).equals("0.0")) ? "0" : nf.format(amount);
    }

    public static Button getPrimaryButton(Context ctx) {
        Button btn = new Button(ctx);
        btn.setTextSize(ctx.getResources().getDimension(R.dimen.bb_button_text_size));
        btn.setTextColor(ctx.getResources().getColor(R.color.uiv3_primary_text_color));
        return btn;
    }

    public static RecyclerView getResponsiveRecyclerView(Context context, int portraitModeGridCount,
                                                         int landscapeModeGridCount) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.recyclerview_layout, null);
        configureRecyclerView(recyclerView, context, portraitModeGridCount, landscapeModeGridCount);
        return recyclerView;
    }

    public static void configureRecyclerView(RecyclerView recyclerView,
                                             Context context, int portraitModeGridCount,
                                             int landscapeModeGridCount) {
        recyclerView.setHasFixedSize(false);
        Configuration configuration = context.getResources().getConfiguration();
        int screenLayout = configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        if (screenLayout == Configuration.SCREENLAYOUT_SIZE_SMALL ||
                screenLayout == Configuration.SCREENLAYOUT_SIZE_NORMAL ||
                screenLayout == Configuration.SCREENLAYOUT_SIZE_UNDEFINED ||
                portraitModeGridCount == 1) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(linearLayoutManager);
        } else {
            int rowCount = configuration.orientation == Configuration.ORIENTATION_PORTRAIT ?
                    portraitModeGridCount : landscapeModeGridCount;
            StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(rowCount,
                    StaggeredGridLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(staggeredGridLayoutManager);
        }
    }
}
