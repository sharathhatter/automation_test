package com.bigbasket.mobileapp.fragment.gift;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.gift.GiftItemMessageRecyclerAdapter;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.gift.GiftItemAware;
import com.bigbasket.mobileapp.model.product.gift.Gift;
import com.bigbasket.mobileapp.util.UIUtil;


public class GiftMessageFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_giftmessage_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        renderGiftMessages();
    }

    private void renderGiftMessages() {
        View base = getView();
        if (base == null) return;
        RadioButton rbtnCommonMsg = (RadioButton) base.findViewById(R.id.rbtnCommonMsg);
        RadioButton rbtnIndividualMsg = (RadioButton) base.findViewById(R.id.rbtnIndividualMsg);

        int sp16 = (int) getResources().getDimension(R.dimen.primary_text_size);
        int sp12 = (int) getResources().getDimension(R.dimen.small_text_size);

        String commonMsg = getString(R.string.commonMsg) + "\n";
        String commonMsgDesc = getString(R.string.commonMsgDesc);
        SpannableString spannableStringCommonMsg = new SpannableString(commonMsg + commonMsgDesc);
        spannableStringCommonMsg.setSpan(new AbsoluteSizeSpan(sp16), 0, commonMsg.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableStringCommonMsg.setSpan(new AbsoluteSizeSpan(sp12), commonMsg.length(),
                spannableStringCommonMsg.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        rbtnCommonMsg.setText(spannableStringCommonMsg);
        rbtnCommonMsg.setChecked(true);
        rbtnCommonMsg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setUpGiftMsgFragment(isChecked);
            }
        });
        setUpGiftMsgFragment(true);

        String individualMsg = getString(R.string.individualMsg) + "\n";
        String individualMsgDesc = getString(R.string.commonMsgDesc);
        SpannableString spannableStringIndividualMsg = new SpannableString(individualMsg + individualMsgDesc);
        spannableStringIndividualMsg.setSpan(new AbsoluteSizeSpan(sp16), 0, individualMsg.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableStringIndividualMsg.setSpan(new AbsoluteSizeSpan(sp12), individualMsg.length(),
                spannableStringIndividualMsg.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        rbtnIndividualMsg.setText(spannableStringIndividualMsg);

        TextView txtChooseYourMsgOption = (TextView) base.findViewById(R.id.txtChooseYourMsgOption);
        txtChooseYourMsgOption.setTypeface(faceRobotoRegular);
        txtChooseYourMsgOption.setText(getString(R.string.chooseYourMsg));
    }

    private void setUpGiftMsgFragment(boolean useCommonMsg) {
        View base = getView();
        if (base == null) return;
        RecyclerView recyclerViewGiftMsgs = (RecyclerView) base.findViewById(R.id.recyclerViewGiftMsgs);
        UIUtil.configureRecyclerView(recyclerViewGiftMsgs, getActivity(), 1, 1);

        Gift gift = ((GiftItemAware) getActivity()).getGifts();

        GiftItemMessageRecyclerAdapter giftItemMessageRecyclerAdapter =
                new GiftItemMessageRecyclerAdapter<>(this, gift.getGiftItems(), useCommonMsg);
        recyclerViewGiftMsgs.setAdapter(giftItemMessageRecyclerAdapter);
    }

    public void redrawGiftMessageRecyclerView(int position) {
        View base = getView();
        if (base == null) return;
        RecyclerView recyclerViewGiftMsgs = (RecyclerView) base.findViewById(R.id.recyclerViewGiftMsgs);
        recyclerViewGiftMsgs.getAdapter().notifyItemChanged(position);
    }

    @Override
    public String getTitle() {
        return getString(R.string.giftOptions);
    }

    @Nullable
    @Override
    public ViewGroup getContentView() {
        return null;
    }

    @Override
    public String getScreenTag() {
        return null;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return GiftMessageFragment.class.getName();
    }
}
