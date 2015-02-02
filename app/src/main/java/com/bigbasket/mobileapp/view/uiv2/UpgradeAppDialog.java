package com.bigbasket.mobileapp.view.uiv2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.KeyEvent;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jugal on 6/8/14.
 */
public class UpgradeAppDialog<T> {

    private T ctx;
    private SharedPreferences prefer;
    private SharedPreferences.Editor editor;

    public UpgradeAppDialog(T ctx) {
        this.ctx = ctx;
        prefer = PreferenceManager.getDefaultSharedPreferences(((ActivityAware) ctx).getCurrentActivity());
        editor = prefer.edit();
    }

    public int showUpdateMsgDialog(String serverAppExpireDate) {
        if (prefer.getLong(Constants.LAST_POPUP_SHOWN_TIME, 0) == 0)
            return Constants.SHOW_APP_UPDATE_POPUP;

        long lastPopUpShownTime = prefer.getLong(Constants.LAST_POPUP_SHOWN_TIME, 0);
        Date serverAppExpireTime = null;
        try {
            serverAppExpireTime = new SimpleDateFormat(Constants.DATE_FORMAT_FOR_APP_UPGRADE_POPUP).parse(serverAppExpireDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return Constants.DONT_SHOW_APP_UPDATE_POPUP;
        }

        if (serverAppExpireTime == null) return Constants.DONT_SHOW_APP_UPDATE_POPUP;
        int popUpShownTimes = prefer.getInt(Constants.APP_EXPIRE_POPUP_SHOWN_TIMES, 0);
        int daysDiff = (int) (serverAppExpireTime.getTime() - lastPopUpShownTime) / (24 * 60 * 60 * 1000);

        if (daysDiff >= 0) {
            if (isMoreThanXDays(lastPopUpShownTime, Constants.ONE_DAY)) {
                if (popUpShownTimes < 3) {
                    return Constants.SHOW_APP_UPDATE_POPUP;
                } else {
                    if (isMoreThanXDays(lastPopUpShownTime, Constants.SIX_DAYS)) {
                        return Constants.SHOW_APP_UPDATE_POPUP;
                    } else return Constants.DONT_SHOW_APP_UPDATE_POPUP;
                }
            } else return Constants.DONT_SHOW_APP_UPDATE_POPUP;
        } else return Constants.SHOW_APP_EXPIRE_POPUP;
    }

    private void updateLastPopShownDate(long lastPopShownTime) {
        incrementPopupShownTimes();
        editor.putLong(Constants.LAST_POPUP_SHOWN_TIME, lastPopShownTime);
        editor.commit();
    }

    private void incrementPopupShownTimes() {
        int popUpShownTimes = prefer.getInt(Constants.APP_EXPIRE_POPUP_SHOWN_TIMES, 0);
        editor.putInt(Constants.APP_EXPIRE_POPUP_SHOWN_TIMES, popUpShownTimes + 1);
        editor.commit();
    }

    private boolean isMoreThanXDays(long lastPopUpShownTime, int days) {
        return (int) (System.currentTimeMillis() - lastPopUpShownTime) / (24 * 60 * 60 * 1000) > days;
    }

    public void showPopUp() {
        updateLastPopShownDate(System.currentTimeMillis());
        AlertDialog.Builder builder = new AlertDialog.Builder(((ActivityAware) ctx).getCurrentActivity());
        builder.setTitle(R.string.update);
        builder.setMessage(R.string.appUpdatedMsg)
                .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UIUtil.openPlayStoreLink(((ActivityAware) ctx).getCurrentActivity());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    (((ActivityAware) ctx).getCurrentActivity()).finish();
                }
                return true;
            }
        });
        alertDialog.show();
    }
}
