package com.bigbasket.mobileapp.adapter.account;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.MemberReferralTCActivity;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;

import java.util.ArrayList;

/**
 * Created by jugal on 21/1/15.
 */
public class MemberReferralGridAdapter<T> extends BaseAdapter{

    private T context;
    private LayoutInflater layoutInflater;
    private Typeface faceRobotoRegular;
    private ArrayList<Integer> referralImageArrayList;
    private ArrayList<String> referralStringArrayList;

    public MemberReferralGridAdapter(T context, ArrayList<Integer> referralImageArrayList,
                                     ArrayList<String> referralStringArrayList,
                                     Typeface faceRobotoRegular) {
        layoutInflater = LayoutInflater.from((Activity)context);
        this.context = context;
        this.referralImageArrayList = referralImageArrayList;
        this.referralStringArrayList = referralStringArrayList;
        this.faceRobotoRegular = faceRobotoRegular;
    }

    @Override
    public int getCount() {
        return referralImageArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return referralImageArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.image_text, null);
        ViewHolder holder = new ViewHolder(convertView);
        ImageView itemImg = (ImageView) convertView.findViewById(R.id.itemImg);
        itemImg.setImageResource(referralImageArrayList.get(position));

        TextView itemTitle = (TextView) convertView.findViewById(R.id.itemTitle);
        itemTitle.setText(referralStringArrayList.get(position));

        RelativeLayout layoutRow = holder.getLayoutRow();
        layoutRow.setTag(referralStringArrayList.get(position));
        layoutRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MemberReferralTCActivity)context).messageHandler(v);
            }
        });
        return convertView;
    }

    private class ViewHolder {
        private ImageView itemImg;
        private TextView itemTitle;
        private View base;
        private RelativeLayout layoutRow;

        private ViewHolder(View base) {
            this.base = base;
        }

        public TextView getItemTitle() {
            if (itemTitle == null) {
                itemTitle = (TextView) base.findViewById(R.id.itemTitle);
                itemTitle.setTypeface(faceRobotoRegular);
            }
            return itemTitle;
        }

        public ImageView getItemImg() {
            if (itemImg == null) {
                itemImg = (ImageView) base.findViewById(R.id.itemImg);
            }
            return itemImg;
        }

        public RelativeLayout getLayoutRow() {
            if (layoutRow == null) {
                layoutRow = (RelativeLayout) base.findViewById(R.id.layoutRow);
            }
            return layoutRow;
        }
    }

}
