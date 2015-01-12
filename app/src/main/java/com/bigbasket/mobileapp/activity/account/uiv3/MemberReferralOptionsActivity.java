package com.bigbasket.mobileapp.activity.account.uiv3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.BaseReferralDialog;
import com.facebook.UiLifecycleHelper;
import com.facebook.android.Facebook;
import com.facebook.widget.FacebookDialog;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by jugal on 23/12/14.
 */
public class MemberReferralOptionsActivity extends BackButtonActivity {

    private static final ArrayList<Integer> referralImageArrayList = new ArrayList<>();
    private static final ArrayList<String> referralStringArrayList = new ArrayList<>();
    private String refLink, refLinkFB, referralMsg, productDesc, productRefImage;
    private int maxMsgLength, maxEmailLength;
    private UiLifecycleHelper uiHelper;

    static {
        referralImageArrayList.add(R.drawable.ic_action_chat);
        referralImageArrayList.add(R.drawable.whatsapp_icon);
        referralImageArrayList.add(R.drawable.com_facebook_inverse_icon);
        referralImageArrayList.add(R.drawable.ic_action_email);
        referralImageArrayList.add(R.drawable.btn_gplus_normal);
        referralImageArrayList.add(R.drawable.main_nav_login_arrow);
    }

    static {
        referralStringArrayList.add("Free SMS");
        referralStringArrayList.add("WhatsApp");
        referralStringArrayList.add("Facebook");
        referralStringArrayList.add("BigBasket Email");
        referralStringArrayList.add("Google+");
        referralStringArrayList.add("Share via Other");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Refer Friends"); //ContactsMultiPicker
        Intent  intent = getIntent();
        refLink = intent.getStringExtra(Constants.REF_LINK);
        refLinkFB = intent.getStringExtra(Constants.REF_LINK_FB);
        maxMsgLength = intent.getIntExtra(Constants.MAX_MSG_LEN, 1000);
        maxEmailLength = intent.getIntExtra(Constants.MAX_EMAIL_LEN, 100);
        referralMsg = intent.getStringExtra(Constants.REFERRAL_MSG);
        productDesc = intent.getStringExtra(Constants.P_DESC);
        productRefImage = intent.getStringExtra(Constants.REF_IMAGE_URL);
        renderMemberReferralList();

        uiHelper = new UiLifecycleHelper(getCurrentActivity(), null);
        uiHelper.onCreate(savedInstanceState);
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


    private void renderMemberReferralList() {
        FrameLayout base = (FrameLayout) findViewById(R.id.content_frame);
        LinearLayout contentView = new LinearLayout(this);
        contentView.setOrientation(LinearLayout.VERTICAL);
        base.addView(contentView);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i < referralImageArrayList.size(); i++) {
            View view = inflater.inflate(R.layout.uiv3_list_icon_and_text_row, null);
            final RelativeLayout layoutRow = (RelativeLayout) view.findViewById(R.id.layoutRow);
            layoutRow.setId(i);

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

    private void messageHandler(View view) {
        switch (view.getId()) {
            case 0:
                sendFreeSMS();
                break;
            case 1:
                sendWhatsAppMsg(referralMsg + "\n" + refLink);
                break;
            case 2:
                useFacebookReferral();
                break;
            case 3:
                useBBmail();
                break;
            case 4:
                useGplus(referralMsg);
                break;
            case 5:
                useOther(referralMsg);
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
        super.onActivityResult(reqCode, resultCode, data);
        switch (resultCode) {
            case (NavigationCodes.CONTACT_NUMBER_SELECTED):
                ArrayList<String> selectedContactIds = (ArrayList<String>) data.getSerializableExtra(Constants.CONTACT_SELECTED);
                getMobileNumberFromIds(selectedContactIds);
                break;
            case 34:
                //facebookCallBack();
                break;
            default:
                super.onActivityResult(reqCode, resultCode, data);
        }
    }

//    private void facebookCallBack(){
//        uiHelper.onActivityResult(requestCode, resultCode, data,
//                new FacebookDialog.Callback() {
//
//                    @Override
//                    public void onError(FacebookDialog.PendingCall pendingCall,
//                                        Exception error, Bundle data) {
//                        Toast.makeText(
//                                getApplicationContext(),
//                                "Error Occured\nMost Common Errors:\n1. Device not connected to Internet\n2.Faceboook APP Id is not changed in Strings.xml",
//                                Toast.LENGTH_LONG).show();
//                    }
//
//                    @Override
//                    public void onComplete(
//                            FacebookDialog.PendingCall pendingCall, Bundle data) {
//                        Toast.makeText(getApplicationContext(), "Done!!",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }


    private void getMobileNumberFromIds(ArrayList<String> selectedContactIds){
        int selectedContactIdsSize = selectedContactIds.size();
        showToast("selectedContactIdsSize=> "+selectedContactIdsSize);
        if(selectedContactIdsSize==0){
            showToast("No, mobile number selected");
            return;
        }else if(selectedContactIdsSize>10){
            showAlertDialog("More than 10 mobile numbers are not allowed.");
        }

        ArrayList<String> correctContactNumbers = new ArrayList<>();
        ArrayList<String> rejectedContactNumbers = new ArrayList<>();
        for(String contactId:selectedContactIds){
            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
            while (phones.moveToNext()) {
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                int numberType = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                int numberLen = phoneNumber.length();
                if(numberLen>=10 && numberLen<=13  &&
                        (numberType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE ||
                        numberType == ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE)){
                    correctContactNumbers.add(phoneNumber);
                }else {
                    rejectedContactNumbers.add(phoneNumber);
                }
            }
            phones.close();
        }
        if(correctContactNumbers.size()>0){
                sendServerPhoneNumber(UIUtil.sentenceJoin(correctContactNumbers, ","), rejectedContactNumbers);
        }else {
            if(selectedContactIdsSize==1)
                showAlertDialog("Selected mobile number is not valid");
            else
                showAlertDialog("Selected mobile number are not valid");
        }
    }

    private void sendServerPhoneNumber(String selectedContactNumbers, final ArrayList<String> rejectedContactNumbers) {
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
                            if(rejectedContactNumbers.size()>0){
                                String rejectedNumberString = UIUtil.sentenceJoin(rejectedContactNumbers);
                                if(rejectedContactNumbers.size()==1)
                                    showAlertDialog("This number is rejected "+rejectedNumberString);
                                else
                                    showAlertDialog("These number are rejected "+rejectedNumberString);
                            }
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
        waIntent.setPackage("com.whatsapp");
        if (waIntent != null) {
            waIntent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(waIntent, message));
        } else {
            Toast.makeText(this, "WhatsApp not found", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void useFacebookReferral() {
        facebookDialogPresent();
    }

    private void facebookDialogPresent(){
        FacebookDialog.MessageDialogBuilder builder = new FacebookDialog.MessageDialogBuilder(this)
                .setLink("https://bigbasket.com/register/") // valid refLinkFB
                .setName("Bigbasket referral")
                .setCaption("Build great social apps that engage your friends.")
                .setDescription(referralMsg)
                .setPicture(productRefImage)
                .setDataErrorsFatal(true);
        if (builder.canPresent()) {
            Log.e("******************* facebook chat app available", "");
            FacebookDialog dialog = builder.build();
            uiHelper.trackPendingDialogCall(dialog.present());
        }  else {
            Intent fbIntent = new Intent(Intent.ACTION_SEND);
            fbIntent.setType("text/plain");
            fbIntent.setPackage("com.facebook.katana");
            if (fbIntent != null) {
                Log.e("******************* facebook app available", "");
                fbIntent.putExtra(Intent.EXTRA_SUBJECT, "Bigbasket referral");
                //intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(resources.getString(R.string.share_email_gmail)));
                fbIntent.putExtra(Intent.EXTRA_TEXT, referralMsg +"\n"+refLinkFB);
                startActivity(Intent.createChooser(fbIntent, "Bigbasket referral"));
            } else {
                Log.e("******************* facebook app not available", "");
                Toast.makeText(this, "Facebook not found", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /*
    OnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String urlToShare = "www.google.com";
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                // intent.putExtra(Intent.EXTRA_SUBJECT, "Foo bar"); // NB:
                // has no effect!
                intent.putExtra(Intent.EXTRA_TEXT, urlToShare);

                // See if official Facebook app is found
                boolean facebookAppFound = false;
                List<ResolveInfo> matches = getPackageManager()
                        .queryIntentActivities(intent, 0);
                for (ResolveInfo info : matches) {
                    if (info.activityInfo.packageName.toLowerCase()
                            .startsWith("com.facebook.katana")) {
                        intent.setPackage(info.activityInfo.packageName);
                        facebookAppFound = true;
                        break;
                    }
                }

                // As fallback, launch sharer.php in a browser
                if (!facebookAppFound) {
                    String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u="
                            + urlToShare;
                    intent = new Intent(Intent.ACTION_VIEW, Uri
                            .parse(sharerUrl));
                }

                startActivity(intent);
            }
        });
     */

    private void useBBmail() {
        // open email form dialog
        ReferralDialog memberRefDialog = new ReferralDialog(getCurrentActivity(), this);
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
        public void sendEmailList(String emailList, String message) {
            memberReferralOptionsActivity.sendEmailMsgToServer(emailList, message, "email");
        }
    }

    private void sendEmailMsgToServer(String emailList, String message, String refType){
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
        gplusIntent.setPackage("com.google.android.apps.plus");
        if (gplusIntent != null) {
            gplusIntent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(gplusIntent, message));
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
}
