package com.bigbasket.mobileapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.fragment.FlatPageFragment;
import com.bigbasket.mobileapp.fragment.HelpDynamicScreenFragment;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.handler.OnSectionItemClickListener;
import com.bigbasket.mobileapp.model.section.DestinationInfo;
import com.bigbasket.mobileapp.model.section.HelpDestinationInfo;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionItem;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;

public class SectionHelpActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uiv3_help_section_layout);
        Section section = getIntent().getParcelableExtra(Constants.SECTION_INFO);
        SectionItem sectionItem = getIntent().getParcelableExtra(Constants.SECTION_ITEM);
        HelpDestinationInfo helpDestinationInfo = sectionItem.getHelpDestinationInfo();

        ViewGroup layoutCheckoutFooter = (ViewGroup) findViewById(R.id.layoutCheckoutFooter);
        OnSectionItemClickListener sectionItemClickListener = new OnSectionItemClickListener<>(this,
                section, sectionItem, Constants.HELP);
        layoutCheckoutFooter.setOnClickListener(sectionItemClickListener);
        findViewById(R.id.imgCloseHelp).setOnClickListener(sectionItemClickListener);
        UIUtil.setUpFooterButton(this, layoutCheckoutFooter, null,
                getString(R.string.continueCaps), true);

        showHelp(helpDestinationInfo);
    }

    private void showHelp(HelpDestinationInfo helpDestinationInfo) {
        if (helpDestinationInfo == null) return;
        switch (helpDestinationInfo.getDestinationType()) {
            case DestinationInfo.DYNAMIC_PAGE:
                HelpDynamicScreenFragment dynamicScreenFragment = new HelpDynamicScreenFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.SCREEN, helpDestinationInfo.getDestinationSlug());
                bundle.putBoolean(Constants.HELP, true);
                dynamicScreenFragment.setArguments(bundle);
                onChangeFragment(dynamicScreenFragment);
                break;
            case DestinationInfo.FLAT_PAGE:
                bundle = new Bundle();
                bundle.putString(Constants.WEBVIEW_URL, getIntent().getStringExtra(Constants.WEBVIEW_URL));
                bundle.putString(Constants.WEBVIEW_TITLE, getIntent().getStringExtra(Constants.WEBVIEW_TITLE));
                FlatPageFragment flatPageFragment = new FlatPageFragment();
                flatPageFragment.setArguments(bundle);
                onChangeFragment(flatPageFragment);
                break;
        }
    }

    @Override
    public BaseActivity getCurrentActivity() {
        return this;
    }

    @Override
    public void onChangeFragment(AbstractFragment newFragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.layoutSectionHelp, newFragment, newFragment.getFragmentTxnTag())
                .commit();
    }

    @Override
    public void onChangeTitle(String title) {

    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        finish();  // Die!!!
    }

    @Override
    public String getScreenTag() {
        return SectionHelpActivity.class.getName();
    }
}
