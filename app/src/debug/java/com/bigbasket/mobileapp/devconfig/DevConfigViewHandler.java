package com.bigbasket.mobileapp.devconfig;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

/**
 * Created by bigbasket on 4/11/15.
 */
public class DevConfigViewHandler implements View.OnClickListener, Handler.Callback {
    private final View mView;
    private boolean mIsDeveloper;
    private Handler mHandler;
    private int clickCount = 0 ;
    private Toast mToast;
    public static final int DEFAULT_CLICK_COUNT = 7;

    private DevConfigViewHandler(View view) {
        mView = view;
        view.setOnClickListener(this);
        mHandler = new Handler(this);
        mIsDeveloper = DeveloperConfigs.isDeveloper(view.getContext());
    }

    public static void setView(View view) {
        if(view != null) {
            //TODO: Potential candidate for GC, Keep a reference to it
            new DevConfigViewHandler(view);
        }
    }

    @Override
    public void onClick(View v) {
        Context context = mView.getContext();
        if(mIsDeveloper) {
            startDeveloperConfigurations(context);
            return;
        }
        if(clickCount >= DEFAULT_CLICK_COUNT) {
            DeveloperConfigs.setIsDeveloper(context,true);
            mIsDeveloper = true;
            showToastMessage(context, "You are now a developer!!!");
            startDeveloperConfigurations(context);
        } else if (clickCount >= 5) {
            showToastMessage(context, (DEFAULT_CLICK_COUNT - clickCount) + " clicks away from becoming a developer");
        }
        clickCount++;
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessageDelayed(0, 300);
    }

    private void showToastMessage(Context context, String message){
        if(mToast != null){
            mToast.cancel();
        }
        mToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        mToast.show();
    }


    private void startDeveloperConfigurations(Context context){
        context.startActivity(new Intent(context, BBDevConfigActivity.class));
    }


    @Override
    public boolean handleMessage(Message msg) {
        clickCount = 0;
        return false;
    }
}
