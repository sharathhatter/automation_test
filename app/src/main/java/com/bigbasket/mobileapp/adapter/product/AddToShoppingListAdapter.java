package com.bigbasket.mobileapp.adapter.product;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;

import java.util.ArrayList;
import java.util.List;

public class AddToShoppingListAdapter<T> extends BaseAdapter {
    private static final int VIEW_TYPE_CREATE = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private T activityAware;
    private List<ShoppingListName> shoppingListNames;
    private int textColor;
    private Drawable drawable;
    private int dp8;
    private int dp16;
    private Typeface typeface;

    public AddToShoppingListAdapter(T activityAware, List<ShoppingListName> shoppingListNames,
                                    Typeface typeface) {
        this.activityAware = activityAware;
        this.shoppingListNames = shoppingListNames != null ? shoppingListNames :
                new ArrayList<ShoppingListName>();
        Context context = ((AppOperationAware) activityAware).getCurrentActivity();
        textColor = context.getResources().getColor(R.color.uiv3_secondary_text_color);
        drawable = ContextCompat.getDrawable(context, R.drawable.shopping_list);
        this.dp8 = (int) context.getResources().getDimension(R.dimen.padding_normal);
        this.dp16 = (int) context.getResources().getDimension(R.dimen.padding_large);
        this.typeface = typeface;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_CREATE : VIEW_TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return shoppingListNames.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        return position == 0 ? new Object() : shoppingListNames.get(getActualListPosition(position));
    }

    private int getActualListPosition(int position) {
        return position - 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Using same view for both the view types
        if (convertView == null) {
            convertView = ((AppOperationAware) activityAware).getCurrentActivity().
                    getLayoutInflater().inflate(R.layout.add_to_shopping_list_item, parent, false);
        }

        convertView.setPadding(dp16, position == 0 ? dp16 : dp8, dp16,
                position == getCount() - 1 ? dp16 : dp8);

        TextView textShoppingListItemName = (TextView) convertView.findViewById(R.id.textShoppingListItemName);
        textShoppingListItemName.setTypeface(typeface);

        if (getItemViewType(position) == VIEW_TYPE_CREATE) {
            Context context = ((AppOperationAware) activityAware).getCurrentActivity();
            textShoppingListItemName.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(context,
                            R.drawable.red_plus), null, null, null);
            textShoppingListItemName.setText(context.getString(R.string.createNewList));
            textShoppingListItemName.setTextColor(context.getResources().getColor(R.color.uiv3_dialog_header_text_bkg));
        } else {
            ShoppingListName shoppingListName = shoppingListNames.get(getActualListPosition(position));
            textShoppingListItemName.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            textShoppingListItemName.setTextColor(textColor);
            textShoppingListItemName.setText(shoppingListName.getName());
        }
        textShoppingListItemName.setCompoundDrawablePadding(dp8);
        return convertView;
    }

}
