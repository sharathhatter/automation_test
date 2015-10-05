package com.bigbasket.mobileapp.adapter.gift;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.model.product.gift.GiftItem;
import com.bigbasket.mobileapp.util.FontHolder;

import java.util.List;

public class GiftItemMessageRecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_GIFT = 0;
    private static final int VIEW_TYPE_GIFT_MSG = 1;

    private T context;
    private List<GiftItem> giftItems;
    private boolean showCommonMsg;
    private Typeface faceRobotoRegular;
    private Typeface faceRobotoMedium;

    public GiftItemMessageRecyclerAdapter(T context, List<GiftItem> giftItems, boolean showCommonMsg) {
        this.context = context;
        this.giftItems = giftItems;
        this.showCommonMsg = showCommonMsg;
        Activity activity = ((ActivityAware) context).getCurrentActivity();
        this.faceRobotoRegular = FontHolder.getInstance(activity).getFaceRobotoRegular();
        this.faceRobotoMedium = FontHolder.getInstance(activity).getFaceRobotoMedium();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = ((ActivityAware) context).getCurrentActivity().getLayoutInflater();
        switch (viewType) {
            case VIEW_TYPE_GIFT:
                View row = inflater.inflate(R.layout.uiv3_gift_item_message_list_row, parent, false);
                return new GiftItemViewHolder(row);
            default:
                row = inflater.inflate(R.layout.uiv3_gift_item_message, parent, false);
                return new GiftItemMsgViewHolder(row);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        GiftItemMsgViewHolder giftItemMsgViewHolder = (GiftItemMsgViewHolder) holder;
        TextView txtTypeYourMsg = giftItemMsgViewHolder.getTxtTypeYourMsg();
        EditText editTextGiftMessage = giftItemMsgViewHolder.getEditTextGiftMessage();
        ViewGroup layoutGiftMsg = giftItemMsgViewHolder.getLayoutGiftMsg();

        editTextGiftMessage.setText("");
        if (getItemViewType(position) == VIEW_TYPE_GIFT) {
            GiftItem giftItem = giftItems.get(position);
            if (showCommonMsg) {
                layoutGiftMsg.setVisibility(View.GONE);
            } else {
                Activity activity = ((ActivityAware) context).getCurrentActivity();
                txtTypeYourMsg.setText(activity.getString(R.string.typeInYourMsg) + " " +
                        giftItem.getMaxNumChars() + " " + activity.getString(R.string.charsColon));
            }
            GiftItemViewHolder giftItemViewHolder = (GiftItemViewHolder) giftItemMsgViewHolder;
            TextView txtSnum = giftItemViewHolder.getTxtSnum();
            TextView txtProductBrand = giftItemViewHolder.getTxtProductBrand();
            TextView txtProductDesc = giftItemViewHolder.getTxtProductDesc();
            TextView txtQty = giftItemViewHolder.getTxtQty();

            txtSnum.setText(position + 1 + ".");
            txtProductBrand.setText(giftItem.getBrand());
            txtProductDesc.setText(giftItem.getDescription());
            txtQty.setText("Qty: " + giftItem.getReservedQty());
        } else {
            layoutGiftMsg.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return showCommonMsg && position == giftItems.size() ? VIEW_TYPE_GIFT_MSG :
                VIEW_TYPE_GIFT;
    }

    private class GiftItemMsgViewHolder extends RecyclerView.ViewHolder {
        private TextView txtTypeYourMsg;
        private EditText editTextGiftMessage;
        private ViewGroup layoutGiftMsg;

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

    @Override
    public int getItemCount() {
        return giftItems.size() + (showCommonMsg ? 1 : 0);
    }
}
