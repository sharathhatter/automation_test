package com.bigbasket.mobileapp.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.model.shoppinglist.ShopMenuOption;

import java.util.List;

public class ShopMenuOptionAdapter extends BaseAdapter {

    private List<ShopMenuOption> shopMenuOptionList;
    private Context context;
    private Typeface faceRobotoRegular;

    public ShopMenuOptionAdapter(Context context, List<ShopMenuOption> results, Typeface faceRobotoRegular) {
        shopMenuOptionList = results;
        this.context = context;
        this.faceRobotoRegular = faceRobotoRegular;
    }

    public int getCount() {
        return shopMenuOptionList.size();
    }

    public Object getItem(int position) {
        return shopMenuOptionList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.uiv3_list_icon_and_two_texts_row, parent, false);
        }
        TextView txtTitle = (TextView) row.findViewById(R.id.itemTitle);
        txtTitle.setTypeface(faceRobotoRegular);
        TextView txtSubTitle = (TextView) row.findViewById(R.id.itemSubTitle);
        txtSubTitle.setTypeface(faceRobotoRegular);
        ImageView imgItem = (ImageView) row.findViewById(R.id.itemImg);
        ShopMenuOption shopMenuOption = shopMenuOptionList.get(position);
        if (shopMenuOption.getTag() != null) {
            row.setTag(shopMenuOption.getTag());
        }
        txtTitle.setText(shopMenuOption.getTitle());
        if (shopMenuOption.getDescription() != null) {
            txtSubTitle.setText(shopMenuOption.getDescription());
        } else {
            txtSubTitle.setVisibility(View.GONE);
        }
        imgItem.setImageResource(shopMenuOption.getImageId());
        return row;
    }
}