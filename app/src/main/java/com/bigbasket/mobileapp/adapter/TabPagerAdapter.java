package com.bigbasket.mobileapp.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.BBTab;

import java.util.ArrayList;

public class TabPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<BBTab> bbTabs;
    private Context ctx;

    public TabPagerAdapter(Context ctx, FragmentManager fm, ArrayList<BBTab> bbTabs) {
        super(fm);
        this.bbTabs = bbTabs;
        this.ctx = ctx;
    }

    @Override
    public Fragment getItem(int i) {
        BBTab bbTab = bbTabs.get(i);
        Fragment fragment = Fragment.instantiate(ctx, bbTab.getFragmentClass().getName(),
                bbTab.getArgs());

        UIUtil.addNavigationContextToBundle(fragment, ((ActivityAware) ctx).getCurrentActivity().getNextScreenNavigationContext());
        return fragment;
    }

    @Override
    public int getCount() {
        return bbTabs.size();
    }

//    public View getTabView(int position) {
//        View v = LayoutInflater.from(ctx).inflate(R.layout.custom_tab, null);
//        TextView txtTitle = (TextView) v.findViewById(R.id.textView_title);
//        txtTitle.setText(bbTabs.get(position).getTabTitle());
//        ImageView imgIcon = (ImageView) v.findViewById(R.id.imageView_icon);
//        imgIcon.setImageResource(bbTabs.get(position).getArgs().getInt(Constants.TAB_IMG));
//        return v;
//    }

    @Override
    public CharSequence getPageTitle(int position) {
        return bbTabs.get(position).getTabTitle();
    }
}
