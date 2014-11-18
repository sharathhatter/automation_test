package com.bigbasket.mobileapp.activity.account.uiv3;

import android.os.Bundle;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.TabActivity;
import com.bigbasket.mobileapp.fragment.account.SignInFragment;
import com.bigbasket.mobileapp.fragment.account.SignUpFragment;
import com.bigbasket.mobileapp.view.uiv3.BBTab;

import java.util.ArrayList;


public class SignInSignUpActivity extends TabActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
    }

    @Override
    public ArrayList<BBTab> getTabs() {
        ArrayList<BBTab> bbTabs = new ArrayList<>();

        bbTabs.add(new BBTab<>(getString(R.string.signInHeader), SignInFragment.class));
        bbTabs.add(new BBTab<>(getString(R.string.signUpHeader), SignUpFragment.class));

        return bbTabs;
    }
}