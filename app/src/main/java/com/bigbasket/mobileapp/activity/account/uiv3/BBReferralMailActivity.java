package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.view.uiv3.BBDrawerLayout;

/**
 * Created by jugal on 22/1/15.
 */
public class BBReferralMailActivity extends BackButtonActivity {


    private EditText editTextListEmailAddress;
    private EditText editTextMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Compose");
        renderBBMailUI();
    }

    private void renderBBMailUI() {
        FrameLayout base = (FrameLayout) findViewById(R.id.content_frame);
        LinearLayout contentView = new LinearLayout(this);
        contentView.setOrientation(LinearLayout.VERTICAL);
        base.addView(contentView);

        contentView.removeAllViews();
        contentView.setBackgroundColor(getResources().getColor(R.color.uiv3_list_bkg_color));

        LayoutInflater inflater = getCurrentActivity().getLayoutInflater();
        View bbMailView = inflater.inflate(R.layout.bb_referral_mail, null);
        contentView.addView(bbMailView);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String memberEmail = preferences.getString(Constants.MEMBER_EMAIL_KEY, null);
        TextView txtFromEmail = (TextView) bbMailView.findViewById(R.id.txtFromEmail);
        txtFromEmail.setText(memberEmail);

        editTextListEmailAddress = (EditText) bbMailView.findViewById(R.id.editTextListEmailAddress);
        BaseActivity.showKeyboard(editTextListEmailAddress);

        editTextMessage = (EditText) bbMailView.findViewById(R.id.editTextMessage);
        trackEvent(TrackingAware.MEMBER_REFERRAL_FACEBOOK_SHOWN, null);
    }

    @Override
    protected void setOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.referral_menu, menu);
        final MenuItem sendMailMenu = menu.findItem(R.id.action_send_mail);
        sendMailMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String[] emailLen = editTextListEmailAddress.getText().toString().trim().split(" ");
                String CommaSeparatedEmail = editTextListEmailAddress.getText().toString().trim().replaceFirst(" ", ",");
                if (CommaSeparatedEmail.length() > 0) {
                    sendEmailList(CommaSeparatedEmail, editTextMessage.getText().toString(), emailLen.length);
                    BaseActivity.hideKeyboard(getCurrentActivity(), editTextListEmailAddress);
                } else {
                    reportFormInputFieldError(editTextListEmailAddress, getString(R.string.error_field_required));
                }
                return true;
            }
        });

        BBDrawerLayout drawerLayout = (BBDrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    public void sendEmailList(String emailList, String message, int emailLen) {
        Intent data = new Intent();
        data.putExtra(Constants.REF_EMAIL_LIST, emailList);
        data.putExtra(Constants.MESSAGE, message);
        data.putExtra(Constants.REF_EMAIL_LEN, emailLen);
        setResult(NavigationCodes.REF_EMAIL_LIST, data);
    }

    @Override
    public String getScreenTag(){
        return TrackEventkeys.BB_REFERRAL_MAIL_SCREEN;
    }
}
