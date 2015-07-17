package com.bigbasket.mobileapp.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.handler.OnSectionItemClickListener;
import com.bigbasket.mobileapp.interfaces.DynamicScreenAware;
import com.bigbasket.mobileapp.model.section.HelpDestinationInfo;
import com.bigbasket.mobileapp.model.section.Renderer;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.model.section.SectionItem;
import com.bigbasket.mobileapp.task.GetDynamicPageTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.SectionView;

import retrofit.RetrofitError;

public class SectionHelpActivity extends BaseActivity implements DynamicScreenAware {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uiv3_help_section_layout);
        Section section = getIntent().getParcelableExtra(Constants.SECTION_INFO);
        SectionItem sectionItem = getIntent().getParcelableExtra(Constants.SECTION_ITEM);
        HelpDestinationInfo helpDestinationInfo = sectionItem.getHelpDestinationInfo();

        ViewGroup layoutCheckoutFooter = (ViewGroup) findViewById(R.id.layoutCheckoutFooter);
        layoutCheckoutFooter.setOnClickListener(new OnSectionItemClickListener<>(this,
                section, sectionItem, Constants.HELP));
        UIUtil.setUpFooterButton(this, layoutCheckoutFooter, null,
                getString(R.string.continueCaps), true);

        showHelp(helpDestinationInfo);
    }

    private void showHelp(HelpDestinationInfo helpDestinationInfo) {
        if (helpDestinationInfo == null) return;
        new GetDynamicPageTask<>(this, helpDestinationInfo.getDestinationSlug(),
                false, true, false, false).startTask();
    }

    private void displaySection(String screenName, SectionData sectionData) {
        SectionView sectionView = new SectionView(this, faceRobotoRegular, sectionData, screenName);
        ViewGroup layoutSectionHelp = (ViewGroup) findViewById(R.id.layoutSectionHelp);
        View sectionViewGenerated = sectionView.getView();
        if (sectionViewGenerated != null) {
            Section firstSection = sectionData.getSections().get(0);
            Renderer sectionRender = sectionData.getRenderersMap() != null ?
                    sectionData.getRenderersMap().get(firstSection.getRenderingId()) : null;
            if (sectionRender != null) {
                layoutSectionHelp.setBackgroundColor(sectionRender.getNativeBkgColor());
            }
            layoutSectionHelp.addView(sectionViewGenerated);
        }
    }

    @Override
    public BaseActivity getCurrentActivity() {
        return this;
    }

    @Override
    public void onChangeFragment(AbstractFragment newFragment) {

    }

    @Override
    public void onChangeTitle(String title) {

    }

    @Override
    public String getScreenTag() {
        return SectionHelpActivity.class.getName();
    }

    @Override
    public void onDynamicScreenSuccess(String screenName, SectionData sectionData) {
        if (!TextUtils.isEmpty(screenName)) {
            setTitle(screenName);
        }
        displaySection(screenName, sectionData);
    }

    @Override
    public void onDynamicScreenFailure(RetrofitError error) {
        handler.handleRetrofitError(error, true);
    }

    @Override
    public void onDynamicScreenFailure(int error, String msg) {
        handler.sendEmptyMessage(error, msg, true);
    }
}
