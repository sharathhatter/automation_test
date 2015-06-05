package com.bigbasket.mobileapp.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.interfaces.PagerNavigationAware;
import com.bigbasket.mobileapp.util.Constants;

public class TutorialItemFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_tutorial_item_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getView() == null) return;
        Bundle args = getArguments();
        int imgDrawableId = args.getInt(Constants.IMAGE_NAME);
        if (imgDrawableId == 0) return;
        ImageView imgTutorial = (ImageView) getView().findViewById(R.id.imgTutorial);
        imgTutorial.setImageDrawable(ContextCompat.getDrawable(getActivity(), imgDrawableId));
        getView().findViewById(R.id.viewNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PagerNavigationAware) getActivity()).slideNext();
            }
        });
        getView().findViewById(R.id.viewSkip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PagerNavigationAware) getActivity()).skip();
            }
        });
    }
}
