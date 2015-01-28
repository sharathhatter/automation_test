package com.bigbasket.mobileapp.util;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.MemberReferralActivity;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jugal on 21/1/15.
 */
public class MemberReferralUtil<T> {

    private T ctx;
    private UiLifecycleHelper uiHelper;
    private FacebookDialog.MessageDialogBuilder builder = null;
    private String referralMsg, playStoreLink, productRefImage, emailBody;
    private int maxMsgCharLength, maxEmailLength, maxMsgLen;


    public MemberReferralUtil(T ctx, String referralMsg, String playStoreLink, String productRefImage,
                              int maxMsgCharLength, int maxEmailLength, int maxMsgLen, String emailBody,
                              UiLifecycleHelper uiHelper) {
        this.ctx = ctx;
        this.referralMsg = referralMsg;
        this.playStoreLink = playStoreLink;
        this.productRefImage = productRefImage;
        this.maxMsgCharLength = maxMsgCharLength;
        this.maxEmailLength = maxEmailLength;
        this.maxMsgLen = maxMsgLen;
        this.emailBody = emailBody;
        this.uiHelper = uiHelper;
    }

    public ArrayList<Object> populateReferralOptions() {
        ArrayList<Object> listMemberRefOption = new ArrayList<>();
        ArrayList<Integer> referralImageArrayList = new ArrayList<>();
        ArrayList<String> referralStringArrayList = new ArrayList<>();

        referralImageArrayList.add(R.drawable.ref_msg);
        referralStringArrayList.add(Constants.FREE_MSG);

        if (isFacebookAvailable()) {
            referralImageArrayList.add(R.drawable.fb_icon);
            referralStringArrayList.add(Constants.FACEBOOK);

        }

        List<PackageInfo> matches = ((MemberReferralActivity) ctx).getPackageManager().getInstalledPackages(0);
        for (final PackageInfo app : matches) {
            if (app.applicationInfo.packageName.toLowerCase().equals(Constants.WHATS_APP_PACKAGE_NAME)) {
                referralImageArrayList.add(R.drawable.whatsapp);
                referralStringArrayList.add(Constants.WHATS_APP);
            } else if (app.applicationInfo.packageName.toLowerCase().equals(Constants.GOOGLE_PLUS_APP_PACKAGE_NAME)) {
                referralImageArrayList.add(R.drawable.g_plus);
                referralStringArrayList.add(Constants.G_PLUS);//done
            } else if (app.applicationInfo.packageName.toLowerCase().equals(Constants.GMAIL_APP_PACKAGE_NAME)) {
                referralImageArrayList.add(R.drawable.android_gmail);
                referralStringArrayList.add(Constants.GMAIL);//done
            } else if (app.applicationInfo.packageName.toLowerCase().equals(Constants.HIKE_PACKAEG_NAME)) {
                referralImageArrayList.add(R.drawable.bread);
                referralStringArrayList.add(Constants.HIKE);//done
            }
        }

        referralImageArrayList.add(R.drawable.ref_email);
        referralStringArrayList.add(Constants.REF_EMAIL);

        referralImageArrayList.add(R.drawable.ref_share);
        referralStringArrayList.add(Constants.SHARE_VIA_OTHER);

        listMemberRefOption.add(referralImageArrayList);
        listMemberRefOption.add(referralStringArrayList);
        return listMemberRefOption;
    }

    public boolean isFacebookAvailable() {
        builder = new FacebookDialog.MessageDialogBuilder((MemberReferralActivity) ctx)
                .setLink(playStoreLink)
                .setName("Bigbasket referral")
                        //.setCaption("Build great social apps that engage your friends.") //subheading
                .setDescription(referralMsg)
                .setPicture(productRefImage)
                .setDataErrorsFatal(true);
        if (builder.canPresent()) {
            return true;
        } else if (FacebookDialog.canPresentShareDialog(((MemberReferralActivity) ctx).getApplicationContext(),
                FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
            return true;
        } else {
            return false;
        }
    }

    public String getMobileNumberFromIds(ArrayList<String> selectedContactNos) {
        int selectedContactNumbersSize = selectedContactNos.size();
        ((MemberReferralActivity) ctx).showToast("selectedContactIdsSize=> " + selectedContactNumbersSize);
        if (selectedContactNumbersSize == 0) {
            ((MemberReferralActivity) ctx).showToast("No, mobile number selected");
            return null;
        } else if (selectedContactNumbersSize > maxMsgLen) {
            ((MemberReferralActivity) ctx).showAlertDialog("More than " + maxMsgLen + " mobile numbers are not allowed.");
        }
        return UIUtil.sentenceJoin(selectedContactNos, ",");
    }


    public void sendWhatsAppMsg() {
        ((TrackingAware) ctx).trackEvent(TrackingAware.MEMBER_REFERRAL_WHATS_APP_SHOWN, null);
        Intent waIntent = new Intent(Intent.ACTION_SEND);
        waIntent.setType("text/plain");
        waIntent.setPackage(Constants.WHATS_APP_PACKAGE_NAME);
        if (waIntent != null) {
            waIntent.putExtra(Intent.EXTRA_TEXT, referralMsg + "\n" + playStoreLink);
            ((MemberReferralActivity) ctx).startActivity(Intent.createChooser(waIntent, null));
        } else {
            Toast.makeText(((MemberReferralActivity) ctx), "WhatsApp not found", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void useFacebookReferral() {
        ((TrackingAware) ctx).trackEvent(TrackingAware.MEMBER_REFERRAL_BB_MAIL_SHOWN, null);
        facebookDialogPresent();
    }

    private void facebookDialogPresent() {
        if (builder != null && builder.canPresent()) {
            FacebookDialog dialog = builder.build();
            uiHelper.trackPendingDialogCall(dialog.present());
        } else {
            FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(((MemberReferralActivity) ctx))
                    .setLink(playStoreLink)
                    .setName("Bigbasket referral")
                            //.setCaption("Build great social apps that engage your friends.")
                    .setDescription(referralMsg)
                    .setPicture(productRefImage)
                    .setDataErrorsFatal(true)
                    .build();
            uiHelper.trackPendingDialogCall(shareDialog.present());
        }
    }

    public void facebookCallBack(int requestCode, int resultCode, Intent data) {
        uiHelper.onActivityResult(requestCode, resultCode, data,
                new FacebookDialog.Callback() {

                    @Override
                    public void onError(FacebookDialog.PendingCall pendingCall,
                                        Exception error, Bundle data) {
                        Toast.makeText(
                                ((MemberReferralActivity) ctx).getApplicationContext(),
                                "Error Occured\nMost Common Errors:\n1. Device not connected to Internet\n2.Faceboook APP Id is not changed in Strings.xml",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onComplete(
                            FacebookDialog.PendingCall pendingCall, Bundle data) {
                        Toast.makeText(((MemberReferralActivity) ctx).getApplicationContext(), "Done!!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public boolean isMessageAndMailLenValid(int emailLen, int messageLen) {
        if (emailLen > maxEmailLength) {
            ((MemberReferralActivity) ctx).showAlertDialog("More than " + maxEmailLength + " mobile numbers are not allowed.");
            return false;
        }
        if (messageLen > maxMsgCharLength) {
            ((MemberReferralActivity) ctx).showAlertDialog("Message length shouldn't be more than" + maxMsgCharLength);
            return false;
        }
        return true;
    }


    /*
    public void addEmailsToAutoComplete(List<String> emailAddressCollection,
                                        MemberReferralActivity.ReferralDialog memberRefDialog) {
        int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2 ?
                android.R.layout.simple_dropdown_item_1line : android.R.layout.simple_list_item_1;
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>((MemberReferralActivity) ctx, layout, emailAddressCollection);

        if (memberRefDialog != null)
            memberRefDialog.getAutoCompleteEditTextView().setAdapter(adapter);
    }

    private static final String[] PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Email.DATA
    };

    public void getEmailFromContacts(List<String> emailAddressCollection,
                                     MemberReferralActivity.ReferralDialog memberRefDialog) {
        ContentResolver cr = ((MemberReferralActivity) ctx).getContentResolver();
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION, null, null, null);
        if (cursor != null) {
            try {
                final int emailIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
                while (cursor.moveToNext()) {
                    emailAddressCollection.add(cursor.getString(emailIndex));
                }
            } finally {
                cursor.close();
            }
        }
        addEmailsToAutoComplete(emailAddressCollection, memberRefDialog);
    }

    */

    public void useGplus() {
        ((TrackingAware) ctx).trackEvent(TrackingAware.MEMBER_REFERRAL_GOOGLE_PLUS_SHOWN, null);
        Intent gplusIntent = new Intent(Intent.ACTION_SEND);
        gplusIntent.setType("text/plain");
        gplusIntent.setPackage(Constants.GOOGLE_PLUS_APP_PACKAGE_NAME);
        if (gplusIntent != null) {
            gplusIntent.putExtra(Intent.EXTRA_TEXT, referralMsg + "\n" + playStoreLink);
            ((MemberReferralActivity) ctx).startActivity(Intent.createChooser(gplusIntent, null));
        } else {
            Toast.makeText((MemberReferralActivity) ctx, "WhatsApp  messenger not found", Toast.LENGTH_SHORT)
                    .show();
        }
    }


    public void useGmailApp() {
        ((TrackingAware) ctx).trackEvent(TrackingAware.MEMBER_REFERRAL_GOOGLE_APP_SHOWN, null);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(((MemberReferralActivity) ctx));
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setType("text/html");
        sendIntent.setClassName(Constants.GMAIL_APP_PACKAGE_NAME, Constants.GMAIL_APP_CLASS_NAME);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT,
                preferences.getString(Constants.MEMBER_FULL_NAME_KEY, "Your friend") + " has invited you " +
                        "to join BigBasket"
        );
        sendIntent.putExtra(Intent.EXTRA_TEXT, referralMsg + "\n" + playStoreLink);//Html.fromHtml(emailBody)
        ((MemberReferralActivity) ctx).startActivity(sendIntent);
    }

    public void useHikeApp() {
        ((TrackingAware) ctx).trackEvent(TrackingAware.MEMBER_REFERRAL_HIKE_SHOWN, null);
        Intent hikeIntent = new Intent(Intent.ACTION_SEND);
        hikeIntent.setType("text/plain");
        hikeIntent.setPackage(Constants.HIKE_PACKAEG_NAME);
        if (hikeIntent != null) {
            hikeIntent.putExtra(Intent.EXTRA_TEXT, referralMsg + "\n" + playStoreLink);
            ((MemberReferralActivity) ctx).startActivity(Intent.createChooser(hikeIntent, null));
        } else {
            Toast.makeText((MemberReferralActivity) ctx, "Hike messenger not found", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void useOther() {
        ((TrackingAware) ctx).trackEvent(TrackingAware.MEMBER_REFERRAL_OTHER_SHOWN, null);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, referralMsg + "\n" + playStoreLink);
        sendIntent.setType("text/plain");
        ((MemberReferralActivity) ctx).startActivity(sendIntent);
    }
}
