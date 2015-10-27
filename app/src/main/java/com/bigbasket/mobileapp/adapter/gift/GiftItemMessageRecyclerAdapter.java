package com.bigbasket.mobileapp.adapter.gift;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.common.FixedLayoutViewHolder;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.product.gift.Gift;
import com.bigbasket.mobileapp.model.product.gift.GiftItem;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.HashMap;

public class GiftItemMessageRecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_GIFT = 0;
    private static final int VIEW_TYPE_GIFT_MSG = 1;
    private static final int VIEW_TYPE_HEADER = 2;
    private static final int VIEW_TYPE_FOOTER = 3;

    private T context;
    private Gift gift;
    private boolean showCommonMsg;
    private Typeface faceRobotoRegular;
    private Typeface faceRobotoMedium;
    private Typeface faceRobotoBold;

    public GiftItemMessageRecyclerAdapter(T context, Gift gift, boolean showCommonMsg) {
        this.context = context;
        this.showCommonMsg = showCommonMsg;
        Activity activity = ((ActivityAware) context).getCurrentActivity();
        this.faceRobotoRegular = FontHolder.getInstance(activity).getFaceRobotoRegular();
        this.faceRobotoMedium = FontHolder.getInstance(activity).getFaceRobotoMedium();
        this.faceRobotoBold = FontHolder.getInstance(activity).getFaceRobotoBold();
        this.gift = gift;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = ((ActivityAware) context).getCurrentActivity().getLayoutInflater();
        switch (viewType) {
            case VIEW_TYPE_GIFT:
                View row = inflater.inflate(R.layout.uiv3_gift_item_message_list_row, parent, false);
                return new GiftItemViewHolder(row);
            case VIEW_TYPE_HEADER:
                row = inflater.inflate(R.layout.uiv3_gift_message_header, parent, false);
                setUpHeaderView(row);
                return new FixedLayoutViewHolder(row);
            case VIEW_TYPE_FOOTER:
                row = inflater.inflate(R.layout.uiv3_gift_list_footer, parent, false);
                return new GiftItemListRecyclerAdapter.GiftItemFooterViewHolder(row, faceRobotoRegular, faceRobotoBold);
            default:
                row = inflater.inflate(R.layout.uiv3_gift_item_message, parent, false);
                int dp16 = (int) ((ActivityAware) context).getCurrentActivity()
                        .getResources().getDimension(R.dimen.padding_normal);
                row.setPadding(dp16, dp16, dp16, dp16);
                return new GiftItemMsgViewHolder(row);
        }
    }

    private void setUpHeaderView(View base) {
        Context context = ((ActivityAware) this.context).getCurrentActivity();
        RadioButton rbtnCommonMsg = (RadioButton) base.findViewById(R.id.rbtnCommonMsg);
        RadioButton rbtnIndividualMsg = (RadioButton) base.findViewById(R.id.rbtnIndividualMsg);

        int sp14 = (int) context.getResources().getDimension(R.dimen.secondary_text_size);
        int sp12 = (int) context.getResources().getDimension(R.dimen.small_text_size);

        CustomTypefaceSpan typefaceSpan = new CustomTypefaceSpan("", FontHolder.getInstance(context).getFaceRobotoLight());
        AbsoluteSizeSpan ab14 = new AbsoluteSizeSpan(sp14);
        AbsoluteSizeSpan ab12 = new AbsoluteSizeSpan(sp12);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.uiv3_secondary_text_color));

        String commonMsg = context.getString(R.string.commonMsg) + "\n";
        String commonMsgDesc = context.getString(R.string.commonMsgDesc);
        SpannableString spannableStringCommonMsg = new SpannableString(commonMsg + commonMsgDesc);
        spannableStringCommonMsg.setSpan(ab14, 0, commonMsg.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableStringCommonMsg.setSpan(ab12, commonMsg.length(),
                spannableStringCommonMsg.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableStringCommonMsg.setSpan(typefaceSpan,
                commonMsg.length(),
                spannableStringCommonMsg.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableStringCommonMsg.setSpan(colorSpan,
                commonMsg.length(),
                spannableStringCommonMsg.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        rbtnCommonMsg.setText(spannableStringCommonMsg);
        rbtnCommonMsg.setChecked(true);
        rbtnCommonMsg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showCommonMsg = isChecked;
                Pair<Integer, Double> data = gift.getGiftItemSelectedCountAndTotalPrice();
                HashMap<String, String> eventAttribs = new HashMap<>();
                eventAttribs.put(Constants.TYPE, showCommonMsg ? Constants.COMMON_MSG : Constants.INDIVIDUAL_MSG);
                eventAttribs.put(Constants.NO_OF_ITEMS, String.valueOf(data.first));
                eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.GIFT_ADDMESSAGE);
                ((TrackingAware) GiftItemMessageRecyclerAdapter.this.context)
                        .trackEvent(TrackingAware.GIFT_MESSAGE_OPT, eventAttribs);
                notifyDataSetChanged();
            }
        });

        String individualMsg = context.getString(R.string.individualMsg) + "\n";
        String individualMsgDesc = context.getString(R.string.individualMsgDesc);
        SpannableString spannableStringIndividualMsg = new SpannableString(individualMsg + individualMsgDesc);
        spannableStringIndividualMsg.setSpan(ab14, 0, individualMsg.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableStringIndividualMsg.setSpan(ab12, individualMsg.length(),
                spannableStringIndividualMsg.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableStringIndividualMsg.setSpan(typefaceSpan, individualMsg.length(),
                spannableStringIndividualMsg.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableStringIndividualMsg.setSpan(typefaceSpan, individualMsg.length(),
                spannableStringIndividualMsg.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableStringIndividualMsg.setSpan(colorSpan, individualMsg.length(),
                spannableStringIndividualMsg.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        rbtnIndividualMsg.setText(spannableStringIndividualMsg);

        TextView txtChooseYourMsgOption = (TextView) base.findViewById(R.id.txtChooseYourMsgOption);
        txtChooseYourMsgOption.setTypeface(faceRobotoRegular);
        txtChooseYourMsgOption.setText(context.getString(R.string.chooseYourMsg));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_GIFT || viewType == VIEW_TYPE_GIFT_MSG) {
            Activity activity = ((ActivityAware) context).getCurrentActivity();
            GiftItemMsgViewHolder giftItemMsgViewHolder = (GiftItemMsgViewHolder) holder;
            TextView txtTypeYourMsg = giftItemMsgViewHolder.getTxtTypeYourMsg();
            EditText editTextGiftMessage = giftItemMsgViewHolder.getEditTextGiftMessage();
            ViewGroup layoutGiftMsg = giftItemMsgViewHolder.getLayoutGiftMsg();
            removeTextWatcher(giftItemMsgViewHolder);

            if (viewType == VIEW_TYPE_GIFT) {
                int itemPosition = getActualPosition(position);
                GiftItem giftItem = gift.getGiftItems().get(itemPosition);
                GiftItemViewHolder giftItemViewHolder = (GiftItemViewHolder) giftItemMsgViewHolder;
                if (showCommonMsg || giftItem.getReservedQty() == 0) {
                    layoutGiftMsg.setVisibility(View.GONE);
                    editTextGiftMessage.setText("");
                } else {
                    addTextWatcher(giftItemViewHolder, position);
                    layoutGiftMsg.setVisibility(View.VISIBLE);
                    editTextGiftMessage.setText(UIUtil.strJoin(giftItem.getMessages(), "\n"));
                    if (giftItem.getMaxNumChars() > 0) {
                        editTextGiftMessage.setFilters(new InputFilter[]{new InputFilter.LengthFilter(giftItem.getMaxNumChars())});
                    }
                    txtTypeYourMsg.setText(activity.getString(R.string.typeInYourMsg) + " " +
                            giftItem.getMaxNumChars() + " " + activity.getString(R.string.charsColon));
                }
                TextView txtSnum = giftItemViewHolder.getTxtSnum();
                TextView txtProductBrand = giftItemViewHolder.getTxtProductBrand();
                TextView txtProductDesc = giftItemViewHolder.getTxtProductDesc();
                TextView txtQty = giftItemViewHolder.getTxtQty();

                txtSnum.setText(itemPosition + 1 + ".");
                txtProductBrand.setText(giftItem.getBrand());
                txtProductDesc.setText(giftItem.getDescription());
                txtQty.setText("Qty: " + giftItem.getReservedQty());
            } else {
                addTextWatcher(giftItemMsgViewHolder, position);
                editTextGiftMessage.setText(gift.getCommonMsg());
                if (gift.getCommonMsgNumChars() > 0) {
                    editTextGiftMessage.setFilters(new InputFilter[]{new InputFilter.LengthFilter(gift.getCommonMsgNumChars())});
                }
                txtTypeYourMsg.setText(activity.getString(R.string.typeInYourMsg) + " " +
                        gift.getCommonMsgNumChars() + " " + activity.getString(R.string.charsColon));
                layoutGiftMsg.setVisibility(View.VISIBLE);
            }
        } else if (viewType == VIEW_TYPE_FOOTER) {
            UIUtil.setUpGiftItemListFooter(gift, (GiftItemListRecyclerAdapter.GiftItemFooterViewHolder) holder,
                    ((ActivityAware) context).getCurrentActivity());
        }
    }

    private void addTextWatcher(GiftItemMsgViewHolder holder, int position) {
        GiftMessageTextWatcher watcher = new GiftMessageTextWatcher(position);
        EditText editTextGiftMessage = holder.getEditTextGiftMessage();
        editTextGiftMessage.addTextChangedListener(watcher);
        holder.setGiftMessageTextWatcher(watcher);
    }

    private void removeTextWatcher(GiftItemMsgViewHolder holder) {
        GiftMessageTextWatcher watcher = holder.getGiftMessageTextWatcher();
        if (watcher != null) {
            EditText editTextGiftMessage = holder.getEditTextGiftMessage();
            editTextGiftMessage.removeTextChangedListener(watcher);
        }
    }

    private class GiftMessageTextWatcher implements TextWatcher {
        private int position;

        public GiftMessageTextWatcher(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            int viewType = getItemViewType(position);
            if (viewType == VIEW_TYPE_GIFT_MSG) {
                gift.setCommonMsg(s == null ? "" : s.toString());
            } else {
                GiftItem giftItem = gift.getGiftItems().get(getActualPosition(position));
                giftItem.setMessage(s.toString());
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    @Override
    public int getItemViewType(int position) {
        int giftArrayLen = gift.getGiftItems().size();
        if (hasMultipleGiftItems()) {
            if (position == 0) {
                return VIEW_TYPE_HEADER;
            }
            if (showCommonMsg) {
                if (position == giftArrayLen + 1) {
                    return VIEW_TYPE_GIFT_MSG;
                } else if (position == giftArrayLen + 2) {
                    return VIEW_TYPE_FOOTER;
                }
                return VIEW_TYPE_GIFT;
            }
            if (position == giftArrayLen + 1) {
                return VIEW_TYPE_FOOTER;
            }
        } else {
            if (position == giftArrayLen) {
                return VIEW_TYPE_GIFT_MSG;
            }
            if (position == giftArrayLen + 1) {
                return VIEW_TYPE_FOOTER;
            }
        }
        return VIEW_TYPE_GIFT;
    }

    public int getActualPosition(int position) {
        return hasMultipleGiftItems() ? position - 1 : position;
    }

    private class GiftItemMsgViewHolder extends RecyclerView.ViewHolder {
        private TextView txtTypeYourMsg;
        private EditText editTextGiftMessage;
        private ViewGroup layoutGiftMsg;
        private GiftMessageTextWatcher giftMessageTextWatcher;

        public GiftItemMsgViewHolder(View itemView) {
            super(itemView);
        }

        public TextView getTxtTypeYourMsg() {
            if (txtTypeYourMsg == null) {
                txtTypeYourMsg = (TextView) itemView.findViewById(R.id.txtTypeYourMsg);
                txtTypeYourMsg.setTypeface(faceRobotoMedium);
            }
            return txtTypeYourMsg;
        }

        public EditText getEditTextGiftMessage() {
            if (editTextGiftMessage == null) {
                editTextGiftMessage = (EditText) itemView.findViewById(R.id.editTextGiftMessage);
                editTextGiftMessage.setTypeface(faceRobotoRegular);
            }
            return editTextGiftMessage;
        }

        public ViewGroup getLayoutGiftMsg() {
            if (layoutGiftMsg == null) {
                layoutGiftMsg = (ViewGroup) itemView.findViewById(R.id.layoutGiftMsg);
            }
            return layoutGiftMsg;
        }

        public GiftMessageTextWatcher getGiftMessageTextWatcher() {
            return giftMessageTextWatcher;
        }

        public void setGiftMessageTextWatcher(GiftMessageTextWatcher giftMessageTextWatcher) {
            this.giftMessageTextWatcher = giftMessageTextWatcher;
        }
    }

    private class GiftItemViewHolder extends GiftItemMsgViewHolder {
        private TextView txtSnum;
        private TextView txtProductBrand;
        private TextView txtProductDesc;
        private TextView txtQty;

        public GiftItemViewHolder(View itemView) {
            super(itemView);
        }

        public TextView getTxtSnum() {
            if (txtSnum == null) {
                txtSnum = (TextView) itemView.findViewById(R.id.txtSnum);
                txtSnum.setTypeface(faceRobotoRegular);
            }
            return txtSnum;
        }

        public TextView getTxtProductBrand() {
            if (txtProductBrand == null) {
                txtProductBrand = (TextView) itemView.findViewById(R.id.txtProductBrand);
                txtProductBrand.setTypeface(faceRobotoRegular);
            }
            return txtProductBrand;
        }

        public TextView getTxtProductDesc() {
            if (txtProductDesc == null) {
                txtProductDesc = (TextView) itemView.findViewById(R.id.txtProductDesc);
                txtProductDesc.setTypeface(faceRobotoRegular);
            }
            return txtProductDesc;
        }

        public TextView getTxtQty() {
            if (txtQty == null) {
                txtQty = (TextView) itemView.findViewById(R.id.txtQty);
                txtQty.setTypeface(faceRobotoRegular);
            }
            return txtQty;
        }
    }

    public boolean isShowCommonMsg() {
        return showCommonMsg;
    }

    private boolean hasMultipleGiftItems() {
        return gift.getGiftItems() != null && gift.getGiftItems().size() > 1;
    }

    @Override
    public int getItemCount() {
        return gift.getGiftItems().size() + (showCommonMsg && hasMultipleGiftItems() ? 1 : 0) + 2;
    }
}
