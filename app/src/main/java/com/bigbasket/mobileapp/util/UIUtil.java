package com.bigbasket.mobileapp.util;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BulletSpan;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.adapter.account.AreaPinInfoDbHelper;
import com.bigbasket.mobileapp.adapter.db.DynamicPageDbHelper;
import com.bigbasket.mobileapp.adapter.gift.GiftItemListRecyclerAdapter;
import com.bigbasket.mobileapp.apiservice.models.response.LoginUserDetails;
import com.bigbasket.mobileapp.application.BaseApplication;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.handler.AnalyticsIdentifierKeys;
import com.bigbasket.mobileapp.handler.AppDataSyncHandler;
import com.bigbasket.mobileapp.interfaces.AnalyticsNavigationContextAware;
import com.bigbasket.mobileapp.managers.CityManager;
import com.bigbasket.mobileapp.model.AppDataDynamic;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.product.gift.Gift;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.analytics.LocalyticsWrapper;
import com.bigbasket.mobileapp.util.analytics.MoEngageWrapper;
import com.bigbasket.mobileapp.util.analytics.NewRelicWrapper;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.moe.pushlibrary.MoEHelper;
import com.moe.pushlibrary.utils.MoEHelperConstants;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

public class UIUtil {

    public static final int NONE = 0;
    public static final int CENTER_INSIDE = 1;
    public static final int CENTER_CROP = 2;
    public static final int ONLY_SCALE_DOWN = 3;

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
        textView.setTextColor(ContextCompat.getColor(context, R.color.redColor));
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
        if (stringList == null || stringList.size() == 0) return "";
        if (stringList.size() == 1) return stringList.get(0);
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

    public static boolean isEmpty(String str) {
        return str == null || TextUtils.isEmpty(str.trim());
    }

    public static String strJoin(String[] stringArray, String separator) {
        if (stringArray == null || stringArray.length == 0) return "";
        if (stringArray.length == 1) return stringArray[0];
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

    public static boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static SpannableString asRupeeSpannable(String amtTxt, Typeface faceRupee) {
        String rupeeSym = "`";
        SpannableString spannableString = new SpannableString(rupeeSym + amtTxt);
        spannableString.setSpan(new CustomTypefaceSpan("", faceRupee), 0, rupeeSym.length(),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        return spannableString;
    }

    public static SpannableString asRupeeSpannable(String prefix, String amtTxt, Typeface faceRupee) {
        String rupeeSym = "`";
        SpannableString spannableString = new SpannableString(prefix + rupeeSym + amtTxt);
        spannableString.setSpan(new CustomTypefaceSpan("", faceRupee), prefix.length(),
                prefix.length() + rupeeSym.length(),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        return spannableString;
    }

    public static SpannableString asRupeeSpannable(double amt, Typeface faceRupee) {
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
                (portraitModeGridCount == 1 && configuration.orientation == Configuration.ORIENTATION_PORTRAIT) ||
                landscapeModeGridCount == 1) {
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
        DynamicPageDbHelper.clearAllAsync(ctx);

        AppDataSyncHandler.reset(ctx);
        AppDataDynamic.reset(ctx);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.FIRST_NAME_PREF, userDetails.firstName);
        editor.putString(Constants.MEMBER_FULL_NAME_KEY, userDetails.fullName);
        editor.putString(Constants.MID_KEY, mId);
        editor.putBoolean(Constants.IS_KIRANA, userDetails.isKirana);

        String cityId = preferences.getString(Constants.CITY_ID, "-1");
        if (!TextUtils.isEmpty(cityId) &&
                !cityId.equals(String.valueOf(userDetails.analytics.cityId))) {
            editor.remove(Constants.AREA_INFO_CALL_LAST);
            AreaPinInfoDbHelper.clearAll(ctx);
        }

        if (userDetails.analytics != null) {
            editor.putString(Constants.CITY, userDetails.analytics.city);
            editor.putString(Constants.CITY_ID, String.valueOf(userDetails.analytics.cityId));
            editor.putBoolean(Constants.HAS_USER_CHOSEN_CITY, true);

            // Any key added here, must be cleared when user logs-out
            LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_ID, mId);
            LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_EMAIL, email);
            LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_NAME, userDetails.fullName);
            LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_MOBILE, userDetails.analytics.mobileNumber);
            LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_REGISTERED_ON, userDetails.analytics.createdOn);
            LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_CITY, userDetails.analytics.city);

            MoEHelper moEHelper = MoEngageWrapper.getMoHelperObj(ctx);
            MoEngageWrapper.setUserAttribute(moEHelper, MoEHelperConstants.USER_ATTRIBUTE_UNIQUE_ID, mId);
            MoEngageWrapper.setUserAttribute(moEHelper, Constants.IS_LOGGED_IN, true);
            MoEngageWrapper.setUserAttribute(moEHelper, MoEHelperConstants.USER_ATTRIBUTE_USER_EMAIL, email);
            MoEngageWrapper.setUserAttribute(moEHelper, MoEHelperConstants.USER_ATTRIBUTE_USER_MOBILE, userDetails.analytics.mobileNumber);
            MoEngageWrapper.setUserAttribute(moEHelper, MoEHelperConstants.USER_ATTRIBUTE_USER_FIRST_NAME, userDetails.firstName);
            MoEngageWrapper.setUserAttribute(moEHelper, MoEHelperConstants.USER_ATTRIBUTE_USER_LAST_NAME, userDetails.lastName);
            MoEngageWrapper.setUserAttribute(moEHelper, MoEHelperConstants.USER_ATTRIBUTE_USER_NAME, userDetails.fullName);
            MoEngageWrapper.setUserAttribute(moEHelper, AnalyticsIdentifierKeys.CUSTOMER_REGISTERED_ON, userDetails.analytics.createdOn);
            MoEngageWrapper.setUserAttribute(moEHelper, AnalyticsIdentifierKeys.CUSTOMER_CITY, userDetails.analytics.city);

            NewRelicWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_EMAIL, email);
            NewRelicWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_ID, mId);
            NewRelicWrapper.setIdentifier(AnalyticsIdentifierKeys.USER_NAME, userDetails.fullName);
            NewRelicWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_MOBILE, userDetails.analytics.mobileNumber);
            NewRelicWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_REGISTERED_ON, userDetails.analytics.createdOn);
            NewRelicWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_CITY, userDetails.analytics.city);

            if (!TextUtils.isEmpty(userDetails.analytics.gender)) {
                MoEngageWrapper.setUserAttribute(moEHelper, MoEHelperConstants.USER_ATTRIBUTE_USER_GENDER, userDetails.analytics.gender);
                LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_GENDER, userDetails.analytics.gender);
                NewRelicWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_GENDER, userDetails.analytics.gender);
            }
            if (!TextUtils.isEmpty(userDetails.analytics.hub)) {
                MoEngageWrapper.setUserAttribute(moEHelper, AnalyticsIdentifierKeys.CUSTOMER_HUB, userDetails.analytics.hub);
                LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_HUB, userDetails.analytics.hub);
                NewRelicWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_HUB, userDetails.analytics.hub);
            }
            if (!TextUtils.isEmpty(userDetails.analytics.dateOfBirth)) {
                MoEngageWrapper.setUserAttribute(moEHelper, MoEHelperConstants.USER_ATTRIBUTE_USER_BDAY, userDetails.analytics.dateOfBirth);
                LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_BDAY, userDetails.analytics.dateOfBirth);
                NewRelicWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_BDAY, userDetails.analytics.dateOfBirth);
            }

            if (userDetails.analytics.additionalAttrs != null) {
                for (Map.Entry<String, Object> additionalInfoObj : userDetails.analytics.additionalAttrs.entrySet()) {
                    MoEngageWrapper.setUserAttribute(moEHelper, additionalInfoObj.getKey(), additionalInfoObj.getValue().toString());
                    LocalyticsWrapper.setIdentifier(additionalInfoObj.getKey(), additionalInfoObj.getValue().toString());
                    NewRelicWrapper.setIdentifier(additionalInfoObj.getKey(), additionalInfoObj.getValue().toString());
                }
                editor.putString(Constants.ANALYTICS_ADDITIONAL_ATTRS, new Gson().toJson(userDetails.analytics.additionalAttrs));
            }
            if(TextUtils.isEmpty(mId)) {
                mId = preferences.getString(Constants.VISITOR_ID_KEY, null);
            }
            BaseApplication.updateGAUserId(ctx, mId);
        }
        editor.commit();

        AuthParameters.reset();
    }

    public static boolean showEmotionsDialog(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean hasUserGivenRating = preferences.getBoolean(Constants.HAS_USER_GIVEN_RATING, false);
        String stringLastShownDate = preferences.getString(Constants.DATE_SINCE_RATING_HAS_SHOWN, "");
        if (!hasUserGivenRating) {
            if (preferences.contains(Constants.DATE_SINCE_RATING_HAS_SHOWN)) {
                try {
                    int daysPeriod = preferences.getInt(Constants.DAYS_PERIOD, 0);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_RATINGS, Locale.getDefault());
                    Date lastShownDate = simpleDateFormat.parse(stringLastShownDate);
                    Date currentDate = new Date();
                    long diff = currentDate.getTime() - lastShownDate.getTime();
                    long days = diff / (24 * 60 * 60 * 1000);
                    if ((int) days >= 5 * Math.pow(2, daysPeriod)) {
                        return true;
                    }
                } catch (ParseException e) {
                    updateRatingPref(context, true);
                    Crashlytics.logException(e);
                }
            } else {
                return true;
            }
        }
        return false;
    }

    public static void updateRatingPref(Context context, boolean reset) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        String date = new SimpleDateFormat(Constants.DATE_FORMAT_RATINGS, Locale.getDefault()).format(new Date());
        editor.putBoolean(Constants.HAS_USER_GIVEN_RATING, false);
        editor.putString(Constants.DATE_SINCE_RATING_HAS_SHOWN, date);
        if (reset) {
            editor.putInt(Constants.DAYS_PERIOD, 0);
        } else {
            if (preferences.contains(Constants.DAYS_PERIOD)) {
                int n = preferences.getInt(Constants.DAYS_PERIOD, 0);
                editor.putInt(Constants.DAYS_PERIOD, ++n);
            } else {
                editor.putInt(Constants.DAYS_PERIOD, 0);
            }
        }
        editor.apply();
    }

    public static void reportFormInputFieldError(TextInputLayout textInputLayout, String errMsg) {
        textInputLayout.setErrorEnabled(true);
        textInputLayout.setError(errMsg);
    }

    public static void resetFormInputField(TextInputLayout textInputLayout) {
        textInputLayout.setErrorEnabled(false);
        textInputLayout.setError("");
    }

    public static int parseAsNativeColor(String rgbColorCode) {
        return parseAsNativeColor(rgbColorCode, Color.BLACK);
    }

    public static int parseAsNativeColor(String rgbColorCode, int defaultColor) {
        if (TextUtils.isEmpty(rgbColorCode)) {
            return defaultColor;
        }
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
        return hourDiff >= hour;
    }

    public static void openPlayStoreLink(Context context) {
        if (context == null) return;
        final String appPackageName = Constants.BASE_PKG_NAME;
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public static boolean isMoreThanXDays(long lastPopUpShownTime, int days) {
        return (System.currentTimeMillis() - lastPopUpShownTime) / (24 * 60 * 60 * 1000) > days;
    }

    public static String getToday(String format) {
        Date date = new Date();
        return new SimpleDateFormat(format, Locale.getDefault()).format(date);
    }

    public static void displayProductImage(@Nullable String baseImgUrl, @Nullable String productImgUrl,
                                           ImageView imgProduct) {
        if (productImgUrl != null) {
            String url;
            if (TextUtils.isEmpty(baseImgUrl) || productImgUrl.startsWith("http")) {
                url = productImgUrl;
            } else {
                url = baseImgUrl + productImgUrl;
            }
            UIUtil.displayAsyncImage(imgProduct, url, false,
                    R.drawable.loading_small);
        } else {
            imgProduct.setImageResource(R.drawable.noimage);
        }
    }

    public static void displayAsyncImage(ImageView imageView, String url) {
        displayAsyncImage(imageView, url, false, R.drawable.loading_small);
    }

    public static void displayAsyncImage(ImageView imageView, @DrawableRes int drawableId) {
        displayAsyncImage(imageView, drawableId, false);
    }

    public static void displayAsyncImage(ImageView imageView, @DrawableRes int drawableId,
                                         boolean skipMemoryCache) {
        RequestCreator requestCreator = Picasso.with(imageView.getContext()).load(drawableId);
        if (skipMemoryCache) {
            requestCreator.memoryPolicy(MemoryPolicy.NO_CACHE);
        }
        //Never do disk cache for drawables
        requestCreator.memoryPolicy(MemoryPolicy.NO_STORE);
        requestCreator.into(imageView);
    }

    public static void displayAsyncImage(ImageView imageView, String url, boolean animate,
                                         @DrawableRes int placeHolderDrawableId) {
        displayAsyncImage(imageView, url, animate, placeHolderDrawableId, 0, 0);
    }

    public static void displayAsyncImage(ImageView imageView, String url, boolean animate,
                                         @DrawableRes int placeHolderDrawableId,
                                         int targetImageWidth, int targetImageHeight) {
        displayAsyncImage(imageView, url, animate, placeHolderDrawableId, targetImageWidth,
                targetImageHeight, false);
    }

    public static void displayAsyncImage(ImageView imageView, String url, boolean animate,
                                         @DrawableRes int placeHolderDrawableId,
                                         int targetImageWidth, int targetImageHeight,
                                         boolean skipMemoryCache) {
        displayAsyncImage(imageView, url, animate, placeHolderDrawableId, targetImageWidth,
                targetImageHeight, skipMemoryCache, NONE, null);
    }

    public static void displayAsyncImage(ImageView imageView, String url, boolean animate,
                                         @DrawableRes int placeHolderDrawableId,
                                         int targetImageWidth, int targetImageHeight,
                                         boolean skipMemoryCache, @ImageScaleType int scaleType,
                                         Callback callback) {

        Picasso picasso = Picasso.with(imageView.getContext());
        RequestCreator requestCreator = picasso.load(url)
                .error(R.drawable.noimage);
        if (url == null) {
            requestCreator.into(imageView, callback);
            return;
        } else {
            picasso.cancelRequest(imageView);
        }

        if (placeHolderDrawableId > 0) {
            requestCreator.placeholder(placeHolderDrawableId);
        }
        if (skipMemoryCache) {
            requestCreator.memoryPolicy(MemoryPolicy.NO_CACHE);
        }
        if (targetImageWidth > 0 && targetImageHeight > 0) {
            requestCreator.resize(targetImageWidth, targetImageHeight);
            Log.i(imageView.getContext().getClass().getSimpleName(),
                    "Loading image " + (skipMemoryCache ? "[NO_MEM_CACHE] " : "")
                            + "(" + targetImageWidth + "," + targetImageHeight + ") = " + url);
        } else {
            Log.i(imageView.getContext().getClass().getSimpleName(), "Loading image = " + url);
        }
        if (!animate) {
            requestCreator.noFade();
        }
        switch (scaleType) {
            case CENTER_INSIDE:
                requestCreator.centerInside();
                break;
            case CENTER_CROP:
                requestCreator.centerCrop();
                break;
            case ONLY_SCALE_DOWN:
                requestCreator.onlyScaleDown();
                break;
        }
        try {
            requestCreator.into(imageView, callback);
        } catch (OutOfMemoryError e) {
            System.gc();
        }
    }

    public static void showEmptyProductsView(final Context context, ViewGroup parent, String msg,
                                             @DrawableRes int drawableId) {
        View emptyPageView = LayoutInflater.from(context)
                .inflate(R.layout.uiv3_empty_data_text, parent, false);
        ImageView imgEmptyPage = (ImageView) emptyPageView.findViewById(R.id.imgEmptyPage);
        imgEmptyPage.setImageResource(drawableId);
        TextView txtEmptyMsg1 = (TextView) emptyPageView.findViewById(R.id.txtEmptyMsg1);
        txtEmptyMsg1.setText(msg);
        TextView txtEmptyMsg2 = (TextView) emptyPageView.findViewById(R.id.txtEmptyMsg2);
        txtEmptyMsg2.setVisibility(View.GONE);
        Button btnBlankPage = (Button) emptyPageView.findViewById(R.id.btnBlankPage);
        btnBlankPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseActivity) context).goToHome();
            }
        });
        parent.addView(emptyPageView);
    }

    public static String getScreenDensity(Context context) {
        int densityDpi = context.getResources().getDisplayMetrics().densityDpi;
        if (densityDpi == 493 || densityDpi >= 540) {
            // For devices like Nexus 6/LG G4
            return "xxhdpi";
        }
        switch (densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                return "mdpi";
            case DisplayMetrics.DENSITY_MEDIUM:
                return "mdpi";
            case DisplayMetrics.DENSITY_HIGH:
                return "hdpi";
            case DisplayMetrics.DENSITY_TV:
                return "hdpi";
            case DisplayMetrics.DENSITY_XHIGH:
                return "xhdpi";
            case DisplayMetrics.DENSITY_XXHIGH:
                return "xhdpi";
            case DisplayMetrics.DENSITY_XXXHIGH:
                return "xxhdpi";
            case DisplayMetrics.DENSITY_280:
                return "hdpi";
            case DisplayMetrics.DENSITY_400:
                return "xhdpi";
            case DisplayMetrics.DENSITY_560:
                return "xhdpi";
        }
        return "hdpi";
    }

    public static double getDpiCoefficient(Context context) {
        String dpi = getScreenDensity(context);
        switch (dpi) {
            case "mdpi":
                return .33;
            case "hdpi":
                return .5;
            case "xhdpi":
                return .66;
            default:
                return 1;
        }
    }

    public static int adjustHeightForScreenWidth(int originalWidth, int originalHeight,
                                                 int totalWidthAvailable) {
        double aspectRatio = (double) originalWidth / (double) originalHeight;
        return (int) ((double) totalWidthAvailable / aspectRatio);
    }

    public static View getCheckoutProgressView(Context context, @Nullable ViewGroup parent, String[] array_txtValues,
                                               @Nullable Integer[] array_compPos, int selectedPos) {
        View container = LayoutInflater.from(context).inflate(R.layout.uiv3_checkout_progress_view,
                parent, false);
        LinearLayout layoutGift = (LinearLayout) container.findViewById(R.id.layout_gift);
        ImageView imageViewAddress = (ImageView) container.findViewById(R.id.imageView_address);
        ImageView imageViewGift = (ImageView) container.findViewById(R.id.imageView_gifts);
        ImageView imageViewSlots = (ImageView) container.findViewById(R.id.imageView_slots);
        ImageView imageViewOrder = (ImageView) container.findViewById(R.id.imageView_order);
        TextView textViewAddress = (TextView) container.findViewById(R.id.textView_address);
        TextView textViewGift = (TextView) container.findViewById(R.id.textView_gifts);
        TextView textViewSlots = (TextView) container.findViewById(R.id.textView_slots);
        TextView textViewOrder = (TextView) container.findViewById(R.id.textView_order);

        ArrayList<ImageView> listImageViews = new ArrayList<>();
        listImageViews.add(imageViewAddress);
        listImageViews.add(imageViewGift);
        listImageViews.add(imageViewSlots);
        listImageViews.add(imageViewOrder);

        Integer[] tot;
        if (array_txtValues.length == 4) {
            textViewAddress.setText(array_txtValues[0]);
            textViewGift.setText(array_txtValues[1]);
            textViewSlots.setText(array_txtValues[2]);
            textViewOrder.setText(array_txtValues[3]);
            tot = new Integer[]{0, 1, 2, 3};
        } else {
            layoutGift.setVisibility(View.GONE);
            listImageViews.remove(1);
            textViewAddress.setText(array_txtValues[0]);
            textViewSlots.setText(array_txtValues[1]);
            textViewOrder.setText(array_txtValues[2]);
            tot = new Integer[]{0, 1, 2};
        }

        if (array_compPos != null) {
            for (Integer array_compPo : array_compPos) {
                listImageViews.get(array_compPo).setBackgroundResource(R.drawable.tick_circle_complete);
            }
        }
        Integer[] rem;
        if (array_compPos != null) {
            List<Integer> list = new ArrayList<>(Arrays.asList(tot));
            TreeSet<Integer> set = new TreeSet<>(list);
            set.removeAll(Arrays.asList(array_compPos));
            rem = set.toArray(new Integer[set.size()]);
        } else {
            rem = tot;
        }
        for (Integer aRem : rem)
            if (aRem != selectedPos) {
                listImageViews.get(aRem).setBackgroundResource(R.drawable.tick_circle_pending);
            }
        listImageViews.get(selectedPos).setBackgroundResource(R.drawable.tick_circle_current);

        textViewAddress.setTypeface(FontHolder.getInstance(context).getFaceRobotoRegular());
        textViewSlots.setTypeface(FontHolder.getInstance(context).getFaceRobotoRegular());
        textViewOrder.setTypeface(FontHolder.getInstance(context).getFaceRobotoRegular());
        textViewGift.setTypeface(FontHolder.getInstance(context).getFaceRobotoRegular());
        return container;
    }

    public static void setUpGiftItemListFooter(Gift gift, GiftItemListRecyclerAdapter.GiftItemFooterViewHolder holder,
                                               Context context) {
        Pair<Integer, Double> data = gift.getGiftItemSelectedCountAndTotalPrice();
        int numGiftItemsToWrap = data.first;
        double giftItemTotal = data.second;

        TextView lblTotalGiftItems = holder.getLblTotalGiftItems();
        TextView txtCountGiftItems = holder.getTxtCountGiftItems();
        TextView lblGiftItemTotalPrice = holder.getLblGiftItemTotalPrice();
        TextView txtGiftItemTotalPrice = holder.getTxtGiftItemTotalPrice();

        lblTotalGiftItems.setText(context.getString(numGiftItemsToWrap > 1 ?
                R.string.totalNumOfItemsToGiftWrapPlural : R.string.totalNumOfItemsToGiftWrapSingular));
        txtCountGiftItems.setText(String.valueOf(numGiftItemsToWrap));

        String start = context.getString(R.string.totalCostOfGiftWrapping) + " ";
        String end = context.getString(R.string.willBeAddedToFinalAmount);
        SpannableString spannableString = new SpannableString(start + end);

        lblGiftItemTotalPrice.setText(spannableString);
        if (giftItemTotal > 0) {
            txtGiftItemTotalPrice.setText(UIUtil.asRupeeSpannable(giftItemTotal,
                    FontHolder.getInstance(context).getFaceRupee()));
        } else {
            txtGiftItemTotalPrice.setText("0");
        }
    }

    public static void setUpFooterButton(Context context, ViewGroup checkoutContainer,
                                         @Nullable String total, String actionText,
                                         boolean showNextArrow) {
        TextView txtTotal = (TextView) checkoutContainer.findViewById(R.id.txtTotal);
        txtTotal.setTypeface(FontHolder.getInstance(context).getFaceRobotoBold());
        TextView txtAction = (TextView) checkoutContainer.findViewById(R.id.txtAction);

        if (!showNextArrow) {
            txtAction.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            txtAction.setTypeface(FontHolder.getInstance(context).getFaceRobotoBold());
        } else {
            txtAction.setTypeface(FontHolder.getInstance(context).getFaceRobotoBold());
        }
        if (!TextUtils.isEmpty(total)) {
            String totalLabel = context.getString(R.string.totalMrp) + " "; //toUpperCase(Locale.getDefault())
            String rupeeSym = "`";
            SpannableString spannableString = new SpannableString(totalLabel + rupeeSym +
                    total);
            spannableString.setSpan(new CustomTypefaceSpan("", FontHolder.getInstance(context).getFaceRupee()),
                    totalLabel.length(),
                    totalLabel.length() + rupeeSym.length(),
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            txtTotal.setText(spannableString);
        } else {
            txtTotal.setVisibility(View.GONE);
        }
        txtAction.setText(actionText.toUpperCase(Locale.getDefault()));
    }

    public static View getOrderSummaryRow(LayoutInflater inflater, String label, String text,
                                          int textColor, Typeface typeface) {
        return getOrderSummaryRow(inflater, label, text, textColor, textColor, typeface);
    }

    public static View getOrderSummaryRow(LayoutInflater inflater, String label, Spannable text,
                                          int textColor, Typeface typeface) {
        return getOrderSummaryRow(inflater, label, text, textColor, textColor, typeface);
    }

    public static View getOrderSummaryRow(LayoutInflater inflater, String label, String text,
                                          int labelColor, int valueColor, Typeface typeface) {
        View row = inflater.inflate(R.layout.uiv3_label_value_table_row, null);

        TextView txtLabel = (TextView) row.findViewById(R.id.txtLabel);
        txtLabel.setTypeface(typeface);
        txtLabel.setTextColor(labelColor);

        TextView txtValue = (TextView) row.findViewById(R.id.txtValue);
        txtValue.setTypeface(typeface);
        txtValue.setTextColor(valueColor);

        txtLabel.setText(label);
        txtValue.setText(text);
        return row;
    }

    public static View getOrderSummaryRow(LayoutInflater inflater, String label, Spannable text,
                                          int labelColor, int valueColor, Typeface typeface) {
        View row = inflater.inflate(R.layout.uiv3_label_value_table_row, null);

        TextView txtLabel = (TextView) row.findViewById(R.id.txtLabel);
        txtLabel.setTypeface(typeface);
        txtLabel.setTextColor(labelColor);

        TextView txtValue = (TextView) row.findViewById(R.id.txtValue);
        txtValue.setTypeface(typeface);
        txtValue.setTextColor(valueColor);

        txtLabel.setText(label);
        txtValue.setText(text);
        return row;
    }

    public static boolean isAlphaString(String matchString) {
        return matchString.matches("[a-zA-Z]+( +[a-zA-Z]+)*");
    }

    public static boolean isAlphaNumericString(String matchString) {
        return matchString.matches("[a-zA-Z0-9]+");
    }

    public static boolean isValidDOB(String dob) {
        if (!TextUtils.isEmpty(dob)) {
            DateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT_DATE_PICKER, Locale.getDefault());
            String today = getToday(Constants.DATE_FORMAT_DATE_PICKER);
            Date inputDate, toDaysData, dateBefore1900;
            try {
                inputDate = df.parse(dob);
                toDaysData = df.parse(today);
                dateBefore1900 = df.parse("01/01/1900");
            } catch (java.text.ParseException e1) {
                e1.printStackTrace();
                return false;
            }
            return !(inputDate == null || toDaysData == null) &&
                    !inputDate.after(toDaysData) && !inputDate.before(dateBefore1900);
        }
        return false;
    }

    public static boolean isPhoneWithGoogleAccount(Context context) {
        return AccountManager.get(context).getAccountsByType("com.google").length > 0;
    }

    @Nullable
    public static ArrayList<NameValuePair> getProductQueryParams(String queryParams) {
        if (TextUtils.isEmpty(queryParams)) return null;

        if (queryParams.contains("&") || queryParams.contains("=")) {
            return getAsNameValuePair(queryParams);
        }
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new NameValuePair(Constants.TYPE, queryParams));
        return nameValuePairs;
    }

    public static ArrayList<NameValuePair> getAsNameValuePair(String queryParams) {
        String[] params = queryParams.split("&");
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
        for (String paramData : params) {
            if (paramData.contains("=")) {
                String[] splittedParams = paramData.split("=");
                if (splittedParams.length == 2) {
                    nameValuePairs.add(new NameValuePair(splittedParams[0], splittedParams[1]));
                }
            }
        }
        return nameValuePairs;
    }

    public static void changeStatusBarColor(Context context, @ColorRes int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && context instanceof Activity) {
            DrawerLayout drawerLayout = (DrawerLayout) ((Activity) context).findViewById(R.id.drawer_layout);
            if (drawerLayout != null) {
                drawerLayout.setStatusBarBackground(color);
            }
        }
    }

    public static void addNavigationContextToBundle(Fragment fragment, String mNextScreenNavigationContext) {
        Bundle args = fragment.getArguments();
        String nc = fragment instanceof AnalyticsNavigationContextAware ?
                ((AnalyticsNavigationContextAware) fragment).getCurrentScreenName() : null;

        if (nc == null && fragment.getActivity() == null) // when Fragment's onActivityCreated in not called
            nc = mNextScreenNavigationContext;
        if (nc == null && fragment.getActivity() != null &&
                fragment.getActivity() instanceof AnalyticsNavigationContextAware) {
            // Use activity's current nc
            nc = ((AnalyticsNavigationContextAware) fragment.getActivity()).getPreviousScreenName();
            if (nc == null) {
                nc = ((AnalyticsNavigationContextAware) fragment.getActivity()).
                        getCurrentScreenName();
            }
        }
        if (!TextUtils.isEmpty(nc)) {
            if (args == null) {
                args = new Bundle();
            }
            args.putString(TrackEventkeys.NAVIGATION_CTX, nc);
            fragment.setArguments(args);
        }
    }

    public static RadioButton getPaymentOptionRadioButton(ViewGroup parent, Context context, LayoutInflater inflater,
                                                          int marginTop) {
        RadioButton radioButton = (RadioButton) inflater.inflate(R.layout.uiv3_payment_option_rbtn, parent, false);
        RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, marginTop);
        radioButton.setLayoutParams(layoutParams);
        radioButton.setTypeface(FontHolder.getInstance(context).getFaceRobotoRegular());
        return radioButton;
    }

    public static String getUniqueDeviceIdentifier(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        if (wInfo != null && !TextUtils.isEmpty(wInfo.getMacAddress()))
            return wInfo.getMacAddress();
        return " ";
    }

    public static void dialNumber(String number, Activity activity) {
        try {
            String uri = "tel:" + number;
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse(uri));
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Do nothing
        }
    }

    public static void invokeMailClient(String email, Activity activity) {
        try {
            activity.startActivity(Intent.createChooser(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email)),
                    "BigBasket Customer Service"));
        } catch (ActivityNotFoundException e) {
            // Do nothing
        }
    }

    public static String getCustomerSupportPhoneNumber(Context context) {
        ArrayList<AddressSummary> addressSummaries = AppDataDynamic.getInstance(context).getAddressSummaries();
        String phone = null;
        if (addressSummaries != null && addressSummaries.size() > 0) {
            City city = CityManager.getCity(addressSummaries.get(0).getCityId(), context);
            if (city != null) {
                phone = city.getPhone();
            }
        }
        return phone;
    }


    public static void showPaymentFailureDlg(final BaseActivity activity) {
        String phone = getCustomerSupportPhoneNumber(activity);
        if (!TextUtils.isEmpty(phone)) {
            View dlg = activity.getLayoutInflater().inflate(R.layout.uiv3_msg_text, null);
            TextView txtMsg = (TextView) dlg.findViewById(R.id.txtMsg);
            int dp16 = (int) activity.getResources().getDimension(R.dimen.padding_normal);
            txtMsg.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            txtMsg.setTextColor(ContextCompat.getColor(activity, android.R.color.black));
            String prefix = activity.getString(R.string.txnFailureMsgPrefix) + " ";
            String suffix = activity.getString(R.string.txnFailureMsgSuffix) + " ";
            final String csEmail = "customerservice@bigbasket.com";
            SpannableString spannableString = new SpannableString(prefix + phone + " " +
                    suffix + csEmail);
            final String passedPhoneNum = phone;
            spannableString.setSpan(new ClickableSpan() {
                                        @Override
                                        public void onClick(View widget) {
                                            UIUtil.dialNumber(passedPhoneNum, activity);
                                        }
                                    }, prefix.length(), prefix.length() + phone.length(),
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ClickableSpan() {
                                        @Override
                                        public void onClick(View widget) {
                                            UIUtil.invokeMailClient(csEmail, activity);
                                        }
                                    }, prefix.length() + phone.length() + 1 + suffix.length(),
                    spannableString.length(),
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            txtMsg.setText(spannableString);
            txtMsg.setMovementMethod(LinkMovementMethod.getInstance());
            AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.transactionFailed))
                    .setView(dlg, dp16, dp16, dp16, dp16)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setCancelable(false);
            builder.create().show();
        } else {
            activity.showAlertDialog(activity.getString(R.string.transactionFailed),
                    activity.getString(R.string.txnFailureMsg));
        }
    }

    public static String makeFlatPageUrlAppFriendly(String url) {
        if (url != null) {
            if (!url.contains("source=app")) {
                if (url.contains("?")) {
                    url += "&source=app";
                } else {
                    url += "?source=app";
                }
            }
        }
        return url;
    }

    //to get the dateinmillisec to another format
    // to use pass the format and dateinmillsec
    public static String getTimeStamp(long dateInMillis, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        return formatter.format(new Date(dateInMillis));
    }


    @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NONE, CENTER_INSIDE, CENTER_CROP, ONLY_SCALE_DOWN})
    public @interface ImageScaleType {
    }
}
