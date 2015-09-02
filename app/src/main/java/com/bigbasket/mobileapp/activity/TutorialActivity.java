package com.bigbasket.mobileapp.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.fragment.TutorialItemFragment;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

public class TutorialActivity extends BaseActivity {

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNextScreenNavigationContext(TrackEventkeys.NC_TUTORIAL_SCREEN);
        setContentView(R.layout.uiv3_tutorial_layout);
        showTutorial();
    }

    private void showTutorial() {
        mViewPager = (ViewPager) findViewById(R.id.pager);
        int[] drawableIdArray = new int[]{R.drawable.tutorial_1, R.drawable.tutorial_2,
                R.drawable.tutorial_3, R.drawable.tutorial_4};
        TutorialAdapter tutorialAdapter = new TutorialAdapter(getSupportFragmentManager(),
                drawableIdArray);
        mViewPager.setAdapter(tutorialAdapter);

        final TextView lblSkip = (TextView) findViewById(R.id.lblSkip);
        final TextView lblNext = (TextView) findViewById(R.id.lblNext);
        final TextView lblStartShopping = (TextView) findViewById(R.id.lblStartShopping);

        lblSkip.setTypeface(faceRobotoLight);
        lblNext.setTypeface(faceRobotoMedium);
        lblStartShopping.setTypeface(faceRobotoMedium);

        lblStartShopping.setVisibility(View.GONE);

        lblSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skip();
            }
        });
        lblStartShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skip();
            }
        });
        lblNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideNext();
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == mViewPager.getAdapter().getCount() - 1) {
                    lblSkip.setVisibility(View.GONE);
                    lblNext.setVisibility(View.GONE);
                    lblStartShopping.setVisibility(View.VISIBLE);
                } else {
                    lblSkip.setVisibility(View.VISIBLE);
                    lblNext.setVisibility(View.VISIBLE);
                    lblStartShopping.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void slideNext() {
        int currentPosition = mViewPager.getCurrentItem();
        int total = mViewPager.getAdapter().getCount();
        if (currentPosition == total - 1) {
            skip();
        } else {
            mViewPager.setCurrentItem(currentPosition + 1, true);
        }
    }

    public void skip() {
        finish();
    }

    private class TutorialAdapter extends FragmentStatePagerAdapter {

        int[] drawableIdArray;

        public TutorialAdapter(FragmentManager fm, int[] drawableIdArray) {
            super(fm);
            this.drawableIdArray = drawableIdArray;
        }

        @Override
        public Fragment getItem(int position) {
            TutorialItemFragment tutorialItemFragment = new TutorialItemFragment();
            Bundle args = new Bundle();
            args.putInt(Constants.IMAGE_NAME, drawableIdArray[position]);
            tutorialItemFragment.setArguments(args);
            return tutorialItemFragment;
        }

        @Override
        public int getCount() {
            return drawableIdArray.length;
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
        return getString(R.string.tutorial);
    }

    @Override
    public void finish() {
        setResult(getIntent().getIntExtra(Constants.ACTION_TAB_TAG, NavigationCodes.TUTORIAL_SEEN));
        super.finish();
    }
}
