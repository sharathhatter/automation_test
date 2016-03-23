package com.bigbasket.mobileapp.adapter.communicationhub;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.bigbasket.mobileapp.BuildConfig;
import com.bigbasket.mobileapp.activity.CommunicationHubActivity;
import com.bigbasket.mobileapp.util.Constants;
import com.moe.pushlibrary.providers.MoEDataContract;
import com.moe.pushlibrary.utils.MoEHelperConstants;
import com.moe.pushlibrary.utils.MoEHelperUtils;
import com.moengage.push.MoEngageNotificationUtils;
import com.moengage.push.PushMessageListener;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by manu on 18/1/16.
 */
public class PushNotificationListener extends PushMessageListener {


    @Override
    public boolean isNotificationRequired(Context context, Bundle extras) {

        if (extras == null) return false;
        if (BuildConfig.DEBUG) {
            MoEHelperUtils.dumpIntentExtras(extras);
        }
        if (TextUtils.isEmpty(MoEngageNotificationUtils.getMessageTagsIfAny(extras))) {
            if (MoEngageNotificationUtils.isChatMessage(extras)) {
                extras.putString(Constants.NOTIFICATION_MSG_TAG, "chat");
            } else if (extras.containsKey(Constants.COMMUNICATION_HUB_BB_KEY)) {
                try {
                    JSONObject obj = new JSONObject(extras.getString(Constants.COMMUNICATION_HUB_BB_KEY));
                    if (obj.has("type")) {
                        extras.putString(Constants.NOTIFICATION_MSG_TAG, obj.getString("type"));
                    }
                } catch (JSONException e) {
                    //Ignore
                }
            }
        }
        return super.isNotificationRequired(context, extras);
    }

    @Override
    protected void onPostNotificationReceived(Context context, Bundle extras) {
        super.onPostNotificationReceived(context, extras);
        if (!MoEngageNotificationUtils.isChatMessage(extras)) {
            //Work around to notify Alert/Offers loaders
            context.getContentResolver()
                    .notifyChange(MoEDataContract.MessageEntity.getContentUri(context), null);
        }
    }

    @Override
    public void onHandleRedirection(Activity activity, Bundle extras) {
        /**
         * adding the deep-link if the notification has isChat key.
         */
        if (MoEngageNotificationUtils.isChatMessage(extras)) {
            //Override gcm_web
            extras.putString(MoEHelperConstants.GCM_EXTRA_WEB_URL, "bigbasket://" + Constants.INBOX + "/");
            extras.putString(MoEHelperConstants.GCM_EXTRA_ACTIVITY_NAME, CommunicationHubActivity.class.getName());
        }
        super.onHandleRedirection(activity, extras);
    }
}
