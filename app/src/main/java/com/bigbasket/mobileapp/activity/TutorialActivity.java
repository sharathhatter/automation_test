package com.bigbasket.mobileapp.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.fragment.TutorialItemFragment;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.interfaces.PagerNavigationAware;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;

public class TutorialActivity extends BaseActivity implements PagerNavigationAware {

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }

    @Override
    public void slideNext() {
        int currentPosition = mViewPager.getCurrentItem();
        int total = mViewPager.getAdapter().getCount();
        if (currentPosition == total - 1) {
            skip();
        } else {
            mViewPager.setCurrentItem(currentPosition + 1, true);
        }
    }

    @Override
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
