package com.bigbasket.mobileapp.view.uiv2;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.util.Constants;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by jugal on 6/8/14.
 */
public class UpgradeAppDialog {
    private BaseActivity activity;
    private String serverAppExpireDate;
    private SharedPreferences prefer;
    private SharedPreferences.Editor editor;
    private int popUpShownTimes;

    public UpgradeAppDialog(BaseActivity activity, String serverAppExpireDate) {
        this.activity = activity;
        this.serverAppExpireDate = serverAppExpireDate.replace("-", "/");
        prefer = PreferenceManager.getDefaultSharedPreferences(activity);
        editor = prefer.edit();
    }

    public int showUpdateMsgDialog() {
        int daysDiff = 0;
        DateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_FOR_APP_UPGRADE_POPUP);
        Calendar cal = Calendar.getInstance();
        if (prefer.getString(Constants.LAST_POPUP_SHOWN_DATE, "").equals("")) {
            updateLastPopShownDate(dateFormat.format(cal.getTime()));
            incrementPopupShownTimes();
            Log.i("When preference is null ", "popUpShownTimes=>" + popUpShownTimes + " daysDiff=>" + daysDiff);
            return Constants.SHOW_APP_UPDATE_POPUP;

        }
        String lastPopUpShownDateString = prefer.getString(Constants.LAST_POPUP_SHOWN_DATE, null);
        popUpShownTimes = prefer.getInt(Constants.APP_EXPIRE_POPUP_SHOWN_TIMES, 0);
        daysDiff = getDaysDiffFromGivenDate(serverAppExpireDate, lastPopUpShownDateString); // check for null

        if (isMoreThanXDays(lastPopUpShownDateString, 1)) {
            if (popUpShownTimes < 3 && daysDiff >= 0) {  // show popup first three times
                incrementPopupShownTimes();
                Log.e("When popUp shown is less than 3 ", "popUpShownTimes=>" + popUpShownTimes + " daysDiff=>" + daysDiff);
                return Constants.SHOW_APP_UPDATE_POPUP;
            } else {
                if (daysDiff < 0) {                    // defensive check
                    Log.e("When daysDiff is in Negative", "popUpShownTimes=>" + popUpShownTimes + " daysDiff=>" + daysDiff);
                    assert false : "Error in Api update is force Update";
                    return Constants.SHOW_APP_EXPIRE_POPUP;
                }
                if (isMoreThanXDays(lastPopUpShownDateString, 6)) {  // show popup after 6 days
                    return Constants.SHOW_APP_UPDATE_POPUP;
                }
                Log.e("Don't show popup because last popup shown time less than Seven day", "");
                return Constants.DONT_SHOW_APP_UPDATE_POPUP;
            }
        }
        Log.e("Don't show popup because last popup shown time less than one day", "");
        return Constants.DONT_SHOW_APP_UPDATE_POPUP;
    }

    private void updateLastPopShownDate(String lastPopShownDate) {
        editor.putString(Constants.LAST_POPUP_SHOWN_DATE, lastPopShownDate);
        editor.commit();
    }

    private void incrementPopupShownTimes() {
        editor.putInt(Constants.APP_EXPIRE_POPUP_SHOWN_TIMES, popUpShownTimes + 1);
        editor.commit();
    }

    private boolean isMoreThanXDays(String lastPopUpShownDateString, int days) {
        Log.e("************* is more than one day", lastPopUpShownDateString);
        if (getDaysDiffFromCurrentDate(lastPopUpShownDateString) > days)
            return true;
        return false;
    }


    private int getDaysDiffFromGivenDate(String serverDateString, String clientDateString) {
        try {
            DateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT_FOR_APP_UPGRADE_POPUP); //MM/dd/yyyy
            Date serverDate = format.parse(serverDateString);
            Date clientDate = format.parse(clientDateString);
            long dateDiff = serverDate.getTime() - clientDate.getTime();
            return (int) dateDiff / (24 * 60 * 60 * 1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getDaysDiffFromCurrentDate(String dateSting) {
        try {
            DateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT_FOR_APP_UPGRADE_POPUP);
            Calendar cal = Calendar.getInstance();
            Date date = format.parse(dateSting);
            long dateDiff = date.getTime() - format.getCalendar().getTime().getTime();
            Log.e("************* getDaysDiffFromCurrentDate current date", format.getCalendar().getTime().getTime() + "");
            Log.e("************* getDaysDiffFromCurrentDate current date using cal", cal.getTime() + "");
            Log.e("************* getDaysDiffFromCurrentDate ", (int) dateDiff / (24 * 60 * 60 * 1000) + "");
            return (int) dateDiff / (24 * 60 * 60 * 1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void showPopUp() {
        DateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT_FOR_APP_UPGRADE_POPUP);
        Calendar cal = Calendar.getInstance();
        updateLastPopShownDate(format.format(cal.getTime()));
        activity.showAlertDialog(activity.getResources().getString(R.string.appUpdatedMsg));

    }
}
