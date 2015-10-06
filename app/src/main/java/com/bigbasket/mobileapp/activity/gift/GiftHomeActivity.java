package com.bigbasket.mobileapp.activity.gift;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.TabPagerAdapterWithFragmentRegistration;
import com.bigbasket.mobileapp.fragment.gift.GiftItemListFragment;
import com.bigbasket.mobileapp.fragment.gift.GiftMessageFragment;
import com.bigbasket.mobileapp.interfaces.gift.GiftItemAware;
import com.bigbasket.mobileapp.model.product.gift.Gift;
import com.bigbasket.mobileapp.model.product.gift.GiftItem;
import com.bigbasket.mobileapp.task.uiv3.PostGiftTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.BBTab;

import java.util.ArrayList;


public class GiftHomeActivity extends BackButtonActivity implements GiftItemAware {

    private Gift gift;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gift = getIntent().getParcelableExtra(Constants.GIFTS);
        /******rendering the gift stage view***********/
        renderCheckOutProgressView();
        /*********rendering the view pager tabs for the gift list and add message tabs***/
        renderViewPagerTabs();

    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_gift_homeactivity_layout;
    }

    private void renderCheckOutProgressView() {
        ViewGroup layoutGiftProgressView = (ViewGroup) findViewById(R.id.layoutGiftProgressView);
        String[] array_txtValues = new String[]{getString(R.string.address),
                getString(R.string.gift), getString(R.string.slots), getString(R.string.order)};
        Integer[] array_compPos = new Integer[]{0};
        int selectedPos = 1;
        View giftView = UIUtil.getCheckoutProgressView(this, null, array_txtValues, array_compPos, selectedPos);
        if (giftView != null) layoutGiftProgressView.addView(giftView);
    }

    private void renderViewPagerTabs() {
        final ViewPager pager = (ViewPager) findViewById(R.id.viewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.slidingTabs);

        ArrayList<BBTab> bbTabs = new ArrayList<>();
        bbTabs.add(new BBTab<>(getString(R.string.selectQtySnum),
                GiftItemListFragment.class));
        bbTabs.add(new BBTab<>(getString(R.string.addMsgSnum),
                GiftMessageFragment.class));

        final TabPagerAdapterWithFragmentRegistration tabPagerAdapter =
                new TabPagerAdapterWithFragmentRegistration(this, getSupportFragmentManager(), bbTabs);
        pager.setAdapter(tabPagerAdapter);
        tabLayout.setupWithViewPager(pager);

        final Button btnFooter = (Button) findViewById(R.id.btnFooter);
        btnFooter.setTypeface(faceRobotoRegular);
        btnFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = pager.getCurrentItem();
                if (currentPosition == 1) {
                    Fragment fragment = tabPagerAdapter.getRegisteredFragment(1);
                    if (fragment != null && fragment instanceof GiftMessageFragment) {
                        postGift(((GiftMessageFragment) fragment).useCommonMsg());
                    } else {
                        postGift(true);
                    }
                } else {
                    pager.setCurrentItem(1);
                }
            }
        });
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    btnFooter.setText(getString(R.string.addMsg));
                } else {
                    btnFooter.setText(getString(R.string.saveAndContinue));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void postGift(boolean useCommonMsg) {
        showProgressDialog(getString(R.string.please_wait));
        if (useCommonMsg) {
            for (GiftItem giftItem : gift.getGiftItems()) {
                giftItem.setMessage(null);
            }
        } else {
            gift.setCommonMsg(null);
        }
        String potentialOrderId = getIntent().getStringExtra(Constants.P_ORDER_ID);
        new PostGiftTask<>(this, potentialOrderId, gift, getCurrentNavigationContext()).startTask();
    }

    @Override
    public Gift getGifts() {
        return gift;
    }
}
