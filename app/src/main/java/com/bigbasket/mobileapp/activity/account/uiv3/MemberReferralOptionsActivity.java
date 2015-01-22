package com.bigbasket.mobileapp.activity.account.uiv3;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.BaseReferralDialog;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.sunny.allphonebookcontactssdk.AllPhoneBookContactUtils;
import com.sunny.allphonebookcontactssdk.ContactData;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MemberReferralOptionsActivity extends BackButtonActivity {

    private ArrayList<Integer> referralImageArrayList = null;
    private ArrayList<String> referralStringArrayList = null;
    //private String refLink, refLinkFB, referralMsg, productDesc, productRefImage;
    private String referralMsg, playStoreLink, productRefImage;
    private int maxMsgCharLength, maxEmailLength, maxMsgLen;
    private UiLifecycleHelper uiHelper;
    private FacebookDialog.MessageDialogBuilder builder = null;
    private ReferralDialog memberRefDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Refer Friends");
        populateReferralOptions();
        Intent intent = getIntent();
        //refLink = intent.getStringExtra(Constants.REF_LINK);
        //refLinkFB = intent.getStringExtra(Constants.REF_LINK_FB);
        maxMsgCharLength = intent.getIntExtra(Constants.MAX_MSG_LEN, 100);
        maxMsgLen = intent.getIntExtra(Constants.MAX_MSG_CHAR_LEN, 10);
        maxEmailLength = intent.getIntExtra(Constants.MAX_EMAIL_LEN, 10);
        referralMsg = intent.getStringExtra(Constants.REFERRAL_MSG);
        playStoreLink = intent.getStringExtra(Constants.PLAY_STORE_LINK);
        //productDesc = intent.getStringExtra(Constants.P_DESC);
        productRefImage = intent.getStringExtra(Constants.REF_IMAGE_URL);
        renderMemberReferralList();

        uiHelper = new UiLifecycleHelper(getCurrentActivity(), null);
        uiHelper.onCreate(savedInstanceState);
    }


    public void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2 ?
                android.R.layout.simple_dropdown_item_1line : android.R.layout.simple_list_item_1;
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getCurrentActivity(), layout, emailAddressCollection);

        if (memberRefDialog != null)
            memberRefDialog.getAutoCompleteEditTextView().setAdapter(adapter);
    }


    private static final String[] PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Email.DATA
    };

    private void getEmailFromContacts(List<String> emailAddressCollection) {
        ContentResolver cr = getContentResolver();
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
        addEmailsToAutoComplete(emailAddressCollection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    private void populateReferralOptions() {
        referralImageArrayList = new ArrayList<>();
        referralStringArrayList = new ArrayList<>();

        referralImageArrayList.add(R.drawable.ref_msg);
        referralStringArrayList.add(Constants.FREE_MSG);

        if (isFacebookAvailable()) {
            referralImageArrayList.add(R.drawable.fb_icon);
            referralStringArrayList.add(Constants.FACEBOOK);

        }

        List<PackageInfo> matches = getPackageManager().getInstalledPackages(0);
        for (final PackageInfo app : matches) {
            if (app.applicationInfo.packageName.toLowerCase().startsWith(Constants.WHATS_APP_PACKAGE_NAME)) {
                referralImageArrayList.add(R.drawable.whatsapp);
                referralStringArrayList.add(Constants.WHATS_APP);
            }
            if (app.applicationInfo.packageName.toLowerCase().startsWith(Constants.GOOGLE_PLUS_APP_PACKAGE_NAME)) {
                referralImageArrayList.add(R.drawable.g_plus);
                referralStringArrayList.add(Constants.G_PLUS);
            }
        }

        referralImageArrayList.add(R.drawable.ref_email);
        referralStringArrayList.add(Constants.REF_EMAIL);

        referralImageArrayList.add(R.drawable.ref_share);
        referralStringArrayList.add(Constants.SHARE_VIA_OTHER);
    }


    private void renderMemberReferralList() {
        FrameLayout base = (FrameLayout) findViewById(R.id.content_frame);
        LinearLayout contentView = new LinearLayout(this);
        contentView.setOrientation(LinearLayout.VERTICAL);
        base.addView(contentView);
        contentView.removeAllViews();

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i < referralImageArrayList.size(); i++) {
            View view = inflater.inflate(R.layout.uiv3_list_icon_and_text_row, null);
            final RelativeLayout layoutRow = (RelativeLayout) view.findViewById(R.id.layoutRow);
            layoutRow.setTag(referralStringArrayList.get(i));

            ImageView itemImg = (ImageView) view.findViewById(R.id.itemImg);
            itemImg.setImageResource(referralImageArrayList.get(i));
            TextView itemTitle = (TextView) view.findViewById(R.id.itemTitle);
            itemTitle.setText(referralStringArrayList.get(i));
            layoutRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    messageHandler(view);
                }
            });
            contentView.addView(view);
        }

    }

    private boolean isFacebookAvailable() {
        builder = new FacebookDialog.MessageDialogBuilder(this)
                .setLink(playStoreLink)
                .setName("Bigbasket referral")
                //.setCaption("Build great social apps that engage your friends.") //subheading
                .setDescription(referralMsg)
                .setPicture(productRefImage)
                .setDataErrorsFatal(true);
        if (builder.canPresent()) {
            return true;
        } else if (FacebookDialog.canPresentShareDialog(getApplicationContext(),
                FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
            return true;
        } else {
            return false;
        }
    }

    private void messageHandler(View view) {
        switch ((String) view.getTag()) {
            case Constants.FREE_MSG:
                sendFreeSMS();
                break;
            case Constants.WHATS_APP:
                sendWhatsAppMsg(referralMsg + "\n" + playStoreLink);
                break;
            case Constants.FACEBOOK:
                useFacebookReferral();
                break;
            case Constants.REF_EMAIL:
                useBBmail();
                break;
            case Constants.G_PLUS:
                useGplus(referralMsg + "\n" + playStoreLink);
                break;
            case Constants.SHARE_VIA_OTHER:
                useOther(referralMsg + "\n" + playStoreLink);
                break;
        }
    }


    private void sendFreeSMS() {
        createContactList();
    }

    private void createContactList() {
        Intent intent = new Intent(getCurrentActivity(), ContactListActivity.class);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        isActivitySuspended = false;
        switch (resultCode) {
            case (NavigationCodes.CONTACT_NUMBER_SELECTED):
                ArrayList<String> selectedContactNumbers = (ArrayList<String>) data.getSerializableExtra(Constants.CONTACT_SELECTED);
                getMobileNumberFromIds(selectedContactNumbers);
                break;
            default:
                facebookCallBack(reqCode, resultCode, data);
                break;
        }
    }

    private void facebookCallBack(int requestCode, int resultCode, Intent data) {
        uiHelper.onActivityResult(requestCode, resultCode, data,
                new FacebookDialog.Callback() {

                    @Override
                    public void onError(FacebookDialog.PendingCall pendingCall,
                                        Exception error, Bundle data) {
                        Toast.makeText(
                                getApplicationContext(),
                                "Error Occured\nMost Common Errors:\n1. Device not connected to Internet\n2.Faceboook APP Id is not changed in Strings.xml",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onComplete(
                            FacebookDialog.PendingCall pendingCall, Bundle data) {
                        Toast.makeText(getApplicationContext(), "Done!!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void getMobileNumberFromIds(ArrayList<String> selectedContactNos) {
        int selectedContactNumbersSize = selectedContactNos.size();
        showToast("selectedContactIdsSize=> " + selectedContactNumbersSize);
        if (selectedContactNumbersSize == 0) {
            showToast("No, mobile number selected");
            return;
        } else if (selectedContactNumbersSize > maxMsgLen) {
            showAlertDialog("More than "+maxMsgLen+" mobile numbers are not allowed.");
        }
        sendServerPhoneNumber(UIUtil.sentenceJoin(selectedContactNos, ","));

        /*
        ArrayList<String> correctContactNumbers = new ArrayList<>();
        ArrayList<String> rejectedContactNumbers = new ArrayList<>();
        for (String contactId : selectedContactIds) {
            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
            while (phones.moveToNext()) {
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                int numberType = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                int numberLen = phoneNumber.length();
                if (numberLen >= 10 && numberLen <= 13 &&
                        (numberType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE ||
                                numberType == ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE)) {
                    correctContactNumbers.add(phoneNumber);
                } else {
                    rejectedContactNumbers.add(phoneNumber);
                }
            }
            phones.close();
        }
        if (correctContactNumbers.size() > 0) {
            sendServerPhoneNumber(UIUtil.sentenceJoin(correctContactNumbers, ","), rejectedContactNumbers);
        } else {
            if (selectedContactIdsSize == 1)
                showAlertDialog("Selected mobile number is not valid");
            else
                showAlertDialog("Selected mobile number are not valid");
        }
        */
    }

    private void sendServerPhoneNumber(String selectedContactNumbers) {
        if (getCurrentActivity() == null) return;
        if (!DataUtil.isInternetAvailable(getCurrentActivity())) return;
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.postReferralSms("message", selectedContactNumbers,
                new Callback<ApiResponse>() {
                    @Override
                    public void success(ApiResponse postReferralApiResponseCallback, Response response) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        if (postReferralApiResponseCallback.status == 0) {
                            showToast(postReferralApiResponseCallback.message);
//                            if (rejectedContactNumbers.size() > 0) {
//                                String rejectedNumberString = UIUtil.sentenceJoin(rejectedContactNumbers);
//                                if (rejectedContactNumbers.size() == 1)
//                                    showAlertDialog("This number is rejected " + rejectedNumberString);
//                                else
//                                    showAlertDialog("These number are rejected " + rejectedNumberString);
//                            }
                        } else {
                            showAlertDialog(postReferralApiResponseCallback.message);
                        }

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        handler.handleRetrofitError(error);
                    }
                });

    }

    public void sendWhatsAppMsg(String message) {
        Intent waIntent = new Intent(Intent.ACTION_SEND);
        waIntent.setType("text/plain");
        waIntent.setPackage(Constants.WHATS_APP_PACKAGE_NAME);
        if (waIntent != null) {
            waIntent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(waIntent, null));
        } else {
            Toast.makeText(this, "WhatsApp not found", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void useFacebookReferral() {
        facebookDialogPresent();
    }

    private void facebookDialogPresent() {
        if (builder != null && builder.canPresent()) {
            FacebookDialog dialog = builder.build();
            uiHelper.trackPendingDialogCall(dialog.present());
        } else {
            FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
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

    private void useBBmail() {
        memberRefDialog = new ReferralDialog(getCurrentActivity(), this);
        memberRefDialog.show(getCurrentActivity().getSupportFragmentManager(),
                Constants.REF_DIALOG_FLAG);
    }

    public static class ReferralDialog extends BaseReferralDialog {
        private MemberReferralOptionsActivity memberReferralOptionsActivity;

        public ReferralDialog() {
        }

        @SuppressLint("ValidFragment")
        public ReferralDialog(BaseActivity baseActivity,
                              MemberReferralOptionsActivity memberReferralOptionsActivity) {
            super(baseActivity, faceRobotoRegular);
            this.memberReferralOptionsActivity = memberReferralOptionsActivity;
        }

        @Override
        public void sendEmailList(String emailList, String message, int emailLen) {
            if(memberReferralOptionsActivity.isMessageAndMailLenValid(emailLen, message.length()))
                memberReferralOptionsActivity.sendEmailMsgToServer(emailList, message, "email");
        }

        @Override
        public void populateAutoComplete() {
            List<String> emailAddressCollection = new ArrayList<>();
            memberReferralOptionsActivity.getEmailFromContacts(emailAddressCollection);
        }
    }

    private boolean isMessageAndMailLenValid(int emailLen, int messageLen){
        if(emailLen>maxEmailLength){
            showAlertDialog("More than "+maxEmailLength+" mobile numbers are not allowed.");
            return false;
        }
        if(messageLen>maxMsgCharLength) {
            showAlertDialog("Message length shouldn't be more than" + maxMsgCharLength);
            return false;
        }
        return true;
    }

    private void sendEmailMsgToServer(String emailList, String message, String refType) {
        // call to server
        if (getCurrentActivity() == null) return;
        if (!DataUtil.isInternetAvailable(getCurrentActivity())) return;
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.postProduct(emailList, message, refType,
                new Callback<ApiResponse>() {
                    @Override
                    public void success(ApiResponse postReferralApiResponseCallback, Response response) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        if (postReferralApiResponseCallback.status == 0) {
                            showToast(postReferralApiResponseCallback.message);
                        } else {
                            showAlertDialog(postReferralApiResponseCallback.message);
                        }

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        handler.handleRetrofitError(error);
                    }
                });
    }

    private void useGplus(String message) {
        Intent gplusIntent = new Intent(Intent.ACTION_SEND);
        gplusIntent.setType("text/plain");
        gplusIntent.setPackage(Constants.GOOGLE_PLUS_APP_PACKAGE_NAME);
        if (gplusIntent != null) {
            gplusIntent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(gplusIntent, null));
        } else {
            Toast.makeText(this, "WhatsApp not found", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void useOther(String message) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    /*
    Example : USE
//Whatsapp
intent.setPackage("com.whatsapp");
//Linkedin
intent.setPackage("com.linkedin.android");
//Twitter
 intent.setPackage("com.twitter.android");
//Facebook
 intent.setPackage("com.facebook.katana");
//GooglePlus
intent.setPackage("com.google.android.apps.plus");
     */




       /*
    private Session.StatusCallback callback = new Session.StatusCallback() {

        @Override
        public void call(Session session, SessionState state, Exception exception) {

            if (state.isOpened()) {
                publishFeedDialog();
            } else {
                Toast.makeText(getCurrentActivity(), "Unable to open facebook session", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    };

    private void publishFeedDialog() {
        Bundle params = new Bundle();
        params.putString("name", "Bigbasket referral");
        //params.putString("caption", "Build great social apps and get more installs.");
        params.putString("description", referralMsg);
        params.putString("link", "https://bigbasket.com/register/"); //todo refLinkFB
        params.putString("picture", productRefImage);

        WebDialog feedDialog = (
                new WebDialog.FeedDialogBuilder(getCurrentActivity(),
                        Session.getActiveSession(),
                        params))
                .setOnCompleteListener(new WebDialog.OnCompleteListener() {

                    @Override
                    public void onComplete(Bundle values,
                                           FacebookException error) {
                        if (error == null) {
                            // When the story is posted, echo the success
                            // and the post Id.
                            final String postId = values.getString("post_id");
                            if (postId != null) {
                                Toast.makeText(getCurrentActivity(),
                                        "Posted story, id: " + postId,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // User clicked the Cancel button
                                Toast.makeText(getCurrentActivity().getApplicationContext(),
                                        "Publish cancelled",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else if (error instanceof FacebookOperationCanceledException) {
                            // User clicked the "x" button
                            Toast.makeText(getCurrentActivity().getApplicationContext(),
                                    "Publish cancelled",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Generic, ex: network error
                            Toast.makeText(getCurrentActivity().getApplicationContext(),
                                    "Error posting story",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                })
                .build();
        feedDialog.show();
    }
    */

}
