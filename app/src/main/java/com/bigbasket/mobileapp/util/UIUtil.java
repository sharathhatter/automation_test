package com.bigbasket.mobileapp.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BulletSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.models.response.LoginUserDetails;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.handler.AnalyticsIdentifierKeys;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.analytics.LocalyticsWrapper;
import com.bigbasket.mobileapp.util.analytics.MoEngageWrapper;
import com.google.gson.Gson;
import com.moe.pushlibrary.MoEHelper;
import com.moe.pushlibrary.utils.MoEHelperConstants;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

    public static RecyclerView getResponsiveRecyclerView(Context context, int portraitModeGridCount,
                                                         int landscapeModeGridCount, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.recyclerview_layout, parent, false);
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
                (portraitModeGridCount == 1 && configuration.orientation == Configuration.ORIENTATION_PORTRAIT)) {
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

    public static void updateStoredUserDetails(Context ctx, LoginUserDetails userDetails, String email, String mId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.FIRST_NAME_PREF, userDetails.firstName);
        editor.putString(Constants.MEMBER_FULL_NAME_KEY, userDetails.fullName);
        editor.putString(Constants.MID_KEY, mId);

        if (userDetails.analytics != null) {
            editor.putString(Constants.CITY, userDetails.analytics.city);
            editor.putString(Constants.CITY_ID, String.valueOf(userDetails.analytics.cityId));

            // Any key added here, must be cleared when user logs-out
            LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_ID, mId);
            LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_EMAIL, email);
            LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_NAME, userDetails.fullName);
            LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_MOBILE, userDetails.analytics.mobileNumber);
            LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_REGISTERED_ON, userDetails.analytics.createdOn);
            LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_CITY, userDetails.analytics.city);


            MoEHelper moEHelper = MoEngageWrapper.getMoHelperObj(ctx);
            MoEngageWrapper.setUserAttribute(moEHelper, MoEHelperConstants.USER_ATTRIBUTE_UNIQUE_ID, mId);
            MoEngageWrapper.setUserAttribute(moEHelper, MoEHelperConstants.USER_ATTRIBUTE_USER_EMAIL, email);
            MoEngageWrapper.setUserAttribute(moEHelper, MoEHelperConstants.USER_ATTRIBUTE_USER_MOBILE, userDetails.analytics.mobileNumber);
            MoEngageWrapper.setUserAttribute(moEHelper, MoEHelperConstants.USER_ATTRIBUTE_USER_FIRST_NAME, userDetails.firstName);
            MoEngageWrapper.setUserAttribute(moEHelper, MoEHelperConstants.USER_ATTRIBUTE_USER_LAST_NAME, userDetails.lastName);
            MoEngageWrapper.setUserAttribute(moEHelper, MoEHelperConstants.USER_ATTRIBUTE_USER_NAME, userDetails.fullName);
            MoEngageWrapper.setUserAttribute(moEHelper, AnalyticsIdentifierKeys.CUSTOMER_REGISTERED_ON, userDetails.analytics.createdOn);
            MoEngageWrapper.setUserAttribute(moEHelper, AnalyticsIdentifierKeys.CUSTOMER_CITY, userDetails.analytics.city);

            if (!TextUtils.isEmpty(userDetails.analytics.gender)) {
                MoEngageWrapper.setUserAttribute(moEHelper, MoEHelperConstants.USER_ATTRIBUTE_USER_GENDER, userDetails.analytics.gender);
                LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_GENDER, userDetails.analytics.gender);
            }
            if (!TextUtils.isEmpty(userDetails.analytics.hub)) {
                MoEngageWrapper.setUserAttribute(moEHelper, AnalyticsIdentifierKeys.CUSTOMER_HUB, userDetails.analytics.hub);
                LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_HUB, userDetails.analytics.hub);
            }
            if (!TextUtils.isEmpty(userDetails.analytics.dateOfBirth)) {
                MoEngageWrapper.setUserAttribute(moEHelper, MoEHelperConstants.USER_ATTRIBUTE_USER_BDAY, userDetails.analytics.dateOfBirth);
                LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_BDAY, userDetails.analytics.dateOfBirth);
            }

            if (userDetails.analytics.additionalAttrs != null) {
                for (Map.Entry<String, Object> additionalInfoObj : userDetails.analytics.additionalAttrs.entrySet()) {
                    MoEngageWrapper.setUserAttribute(moEHelper, additionalInfoObj.getKey(), additionalInfoObj.getValue().toString());
                    LocalyticsWrapper.setIdentifier(additionalInfoObj.getKey(), additionalInfoObj.getValue().toString());
                }
                editor.putString(Constants.ANALYTICS_ADDITIONAL_ATTRS, new Gson().toJson(userDetails.analytics.additionalAttrs));
            }
        }
        editor.commit();
        AuthParameters.updateInstance(ctx);
    }

    public static void reportFormInputFieldError(EditText editText, String errMsg) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            editText.setError(errMsg);
        } else {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(errMsg);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, errMsg.length(),
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            editText.setError(spannableStringBuilder);
        }
    }

    public static void reportFormInputFieldError(AutoCompleteTextView autoCompleteTextView, String errMsg) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            autoCompleteTextView.setError(errMsg);
        } else {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(errMsg);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, errMsg.length(),
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            autoCompleteTextView.setError(spannableStringBuilder);
        }
    }

    public static int parseAsNativeColor(String rgbColorCode) {
        return parseAsNativeColor(rgbColorCode, Color.BLACK);
    }

    public static int parseAsNativeColor(String rgbColorCode, int defaultColor) {
        try {
            return Color.parseColor(rgbColorCode);
        } catch (IllegalArgumentException e) {
            return defaultColor;
        } catch (StringIndexOutOfBoundsException e) {
            return defaultColor;
        }
    }


    public static boolean isMoreThanXHour(long timeInMiliSeconds, int hour) {
        long timerDiff = System.currentTimeMillis() - timeInMiliSeconds;
        int hourDiff = (int) timerDiff / (60 * 60 * 1000);
        if (hourDiff >= hour)
            return true;
        return false;
    }

    public static void openPlayStoreLink(Activity activity) {
        final String appPackageName = Constants.BASE_PKG_NAME;
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public static boolean isMoreThanXDays(long lastPopUpShownTime, int days) {
        return (System.currentTimeMillis() - lastPopUpShownTime) / (24 * 60 * 60 * 1000) > days;
    }

    public static String getToday(String format) {
        Date date = new Date();
        return new SimpleDateFormat(format).format(date);
    }

    public static int handleUpdateDialog(String serverAppExpireDateString, Activity activity) {
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(activity);
        long lastPopUpShownTime = prefer.getLong(Constants.LAST_POPUP_SHOWN_TIME, 0);
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_FOR_APP_UPGRADE_POPUP);
        String today = getToday(Constants.DATE_FORMAT_FOR_APP_UPGRADE_POPUP);

        Date serverAppExpireDate, toDaysData;
        try {
            serverAppExpireDate = sdf.parse(serverAppExpireDateString);
            toDaysData = sdf.parse(today);
        } catch (ParseException e) {
            e.printStackTrace();
            return Constants.DONT_SHOW_APP_UPDATE_POPUP;
        }

        if (serverAppExpireDate.compareTo(toDaysData) < 0) {
            prefer.edit().putLong(Constants.LAST_APP_DATA_CALL_TIME, 0).apply();
            return Constants.SHOW_APP_EXPIRE_POPUP;
        }
        int popUpShownTimes = prefer.getInt(Constants.APP_EXPIRE_POPUP_SHOWN_TIMES, 0);
        long timeDiff = (serverAppExpireDate.getTime() - lastPopUpShownTime) / (24 * 60 * 60 * 1000);

        if (timeDiff >= 0) {
            if (UIUtil.isMoreThanXDays(lastPopUpShownTime, Constants.ONE_DAY)) {
                if (popUpShownTimes < 3) {
                    return Constants.SHOW_APP_UPDATE_POPUP;
                } else {
                    if (UIUtil.isMoreThanXDays(lastPopUpShownTime, Constants.SIX_DAYS)) {
                        return Constants.SHOW_APP_UPDATE_POPUP;
                    } else return Constants.DONT_SHOW_APP_UPDATE_POPUP;
                }
            } else return Constants.DONT_SHOW_APP_UPDATE_POPUP;
        } else {
            prefer.edit().putLong(Constants.LAST_APP_DATA_CALL_TIME, 0).apply();
            return Constants.SHOW_APP_EXPIRE_POPUP;
        }
    }

    public static void updateLastPopShownDate(long lastPopShownTime, Activity activity) {
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = prefer.edit();
        editor.putLong(Constants.LAST_POPUP_SHOWN_TIME, lastPopShownTime);
        int popUpShownTimes = prefer.getInt(Constants.APP_EXPIRE_POPUP_SHOWN_TIMES, 0);
        editor.putInt(Constants.APP_EXPIRE_POPUP_SHOWN_TIMES, popUpShownTimes + 1);
        editor.apply();
    }


    public static void updateLastAppDataCall(Activity activity) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(Constants.LAST_APP_DATA_CALL_TIME, System.currentTimeMillis());
        editor.apply();
    }

    public static MaterialDialog.Builder getMaterialDialogBuilder(Context context) {
        return new MaterialDialog.Builder(context)
                .positiveColorRes(R.color.uiv3_accept_label_color)
                .negativeColorRes(R.color.dark_black);
    }

    public static void displayAsyncImage(ImageView imageView, String url) {
        Picasso.with(imageView.getContext())
                .load(url)
                .placeholder(R.drawable.loading)
                .error(R.drawable.noimage)
                .into(imageView);
    }
}
