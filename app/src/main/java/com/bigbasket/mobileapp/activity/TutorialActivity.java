package com.bigbasket.mobileapp.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

public class TutorialActivity extends BaseActivity {

    private ImageView mImageView;
    private int[] drawableIdArray = new int[]{R.drawable.tutorial_1, R.drawable.tutorial_2,
            R.drawable.tutorial_3, R.drawable.tutorial_4};
    private int mCurrentTutorialIndex;
    private TextView lblSkip;
    private TextView lblNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uiv3_tutorial_layout);
        setCurrentScreenName(TrackEventkeys.NC_TUTORIAL_SCREEN);
        showTutorial();
    }

    private void showTutorial() {
        mImageView = (ImageView) findViewById(R.id.pager);
        UIUtil.displayAsyncImage(mImageView, drawableIdArray[mCurrentTutorialIndex], true);
        lblSkip = (TextView) findViewById(R.id.lblSkip);
        lblNext = (TextView) findViewById(R.id.lblNext);

        lblSkip.setTypeface(faceRobotoLight);
        lblNext.setTypeface(faceRobotoMedium);

        lblSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTutorialComplete();
            }
        });
        lblNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentTutorialIndex >= (drawableIdArray.length - 1)) {
                    onTutorialComplete();
                } else {
                    slideNext();
                }
            }
        });
    }

    private void slideNext() {
        int nextPosition = ++mCurrentTutorialIndex;
        if (nextPosition < drawableIdArray.length) {
            UIUtil.displayAsyncImage(mImageView, drawableIdArray[nextPosition], true);
            if (nextPosition == drawableIdArray.length - 1) {
                lblSkip.setVisibility(View.GONE);
                lblNext.setText(R.string.startShopping);
            }

        } else {
            onTutorialComplete();
        }
    }

    @Override
    public void onBackPressed() {
        //Ignore back press, To make sure user presses skip/next
    }

    private void onTutorialComplete() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constants.TUTORIAL_SEEN, true);
        editor.apply();
        finish();
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

    @Override
    protected void onStop() {
        super.onStop();
        System.gc();
    }
}
