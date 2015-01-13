package com.bigbasket.mobileapp.adapter.product;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.model.promo.Promo;
import com.bigbasket.mobileapp.model.promo.PromoCategory;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class PromoCategoryAdapter extends BaseAdapter {

    private Context context;
    private List<Object> promoConsolidatedList;
    private Typeface typeface;
    private ImageLoader imageLoader = ImageLoader.getInstance();

    public static final int VIEW_TYPE_PROMO = 0;
    public static final int VIEW_TYPE_CATEGORY = 1;

    public PromoCategoryAdapter(Context context, List<Object> promoConsolidatedList, Typeface typeface) {
        this.context = context;
        this.typeface = typeface;
        this.promoConsolidatedList = promoConsolidatedList;

    }

    @Override
    public int getItemViewType(int position) {
        return promoConsolidatedList.get(position) instanceof Promo ? VIEW_TYPE_PROMO : VIEW_TYPE_CATEGORY;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return promoConsolidatedList.size();
    }

    @Override
    public Object getItem(int position) {
        return promoConsolidatedList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == VIEW_TYPE_PROMO) {
            return getPromoView(position, convertView, parent);
        }
        return getPromoCategoryView(position, convertView, parent);
    }

    private View getPromoView(int position, View convertView, ViewGroup parent) {
        final Promo promo = (Promo) getItem(position);
        View row = convertView;
        PromoCatItemHolder promoCatItemHolder;
        if (row == null) {
            LayoutInflater layoutInflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.uiv3_promo_category_list_item, parent, false);
            promoCatItemHolder = new PromoCatItemHolder(row);
            row.setTag(promoCatItemHolder);
        } else {
            promoCatItemHolder = (PromoCatItemHolder) row.getTag();
        }

        ImageView imgPromoIcon = promoCatItemHolder.getImgPromoIcon();
        TextView txtPromoName = promoCatItemHolder.getTxtPromoName();
        TextView txtPromoDescLine1 = promoCatItemHolder.getTxtPromoDescLine1();
        TextView txtPromoDescLine2 = promoCatItemHolder.getTxtPromoDescLine2();

        imageLoader.displayImage(promo.getPromoIcon(), imgPromoIcon);
        txtPromoName.setText(promo.getPromoName());
        txtPromoDescLine1.setText(promo.getPromoDescLine1());
        txtPromoDescLine2.setText(promo.getPromoDescLine2());
        return row;
    }

    private View getPromoCategoryView(int position, View convertView, ViewGroup parent) {
        final PromoCategory promoCategory = (PromoCategory) getItem(position);
        View row = convertView;
        PromoCatViewHolder promoCatViewWrapper;
        if (row == null) {
            LayoutInflater layoutInflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.uiv3_promo_category_list_header, parent, false);
            promoCatViewWrapper = new PromoCatViewHolder(row);
            row.setTag(promoCatViewWrapper);
            row.setEnabled(false);
        } else {
            promoCatViewWrapper = (PromoCatViewHolder) row.getTag();
        }
        TextView txtPromoCatHeader = promoCatViewWrapper.getTxtPromoCatHeader();
        txtPromoCatHeader.setTypeface(typeface);
        txtPromoCatHeader.setText(promoCategory.getName());

        if (promoCategory.getDescription() != null
                && promoCategory.getDescription().length() > 0) {
            TextView txtPromoCatDesc = promoCatViewWrapper.getTxtPromoCatDesc();
            txtPromoCatDesc.setTypeface(typeface);
            txtPromoCatDesc.setText(promoCategory.getDescription());
        }
        return row;
    }

    private class PromoCatItemHolder {
        private View base;
        private ImageView imgPromoIcon;
        private TextView txtPromoName;
        private TextView txtPromoDescLine1;
        private TextView txtPromoDescLine2;

        private PromoCatItemHolder(View base) {
            this.base = base;
        }

        public ImageView getImgPromoIcon() {
            if (imgPromoIcon == null) {
                imgPromoIcon = (ImageView) base.findViewById(R.id.imgPromoIcon);
            }
            return imgPromoIcon;
        }

        public TextView getTxtPromoName() {
            if (txtPromoName == null) {
                txtPromoName = (TextView) base.findViewById(R.id.txtPromoName);
                txtPromoName.setTypeface(typeface);
            }
            return txtPromoName;
        }

        public TextView getTxtPromoDescLine1() {
            if (txtPromoDescLine1 == null) {
                txtPromoDescLine1 = (TextView) base.findViewById(R.id.txtPromoDescLine1);
            }
            return txtPromoDescLine1;
        }

        public TextView getTxtPromoDescLine2() {
            if (txtPromoDescLine2 == null) {
                txtPromoDescLine2 = (TextView) base.findViewById(R.id.txtPromoDescLine2);
            }
            return txtPromoDescLine2;
        }
    }

    private class PromoCatViewHolder {
        private TextView txtPromoCatHeader;
        private TextView txtPromoCatDesc;
        private View base;

        public PromoCatViewHolder(View base) {
            this.base = base;
        }

        private TextView getTxtPromoCatDesc() {
            if (txtPromoCatDesc == null)
                txtPromoCatDesc = (TextView) base.findViewById(R.id.txtPromoCatDesc);
            return txtPromoCatDesc;
        }

        private TextView getTxtPromoCatHeader() {
            if (txtPromoCatHeader == null)
                txtPromoCatHeader = (TextView) base.findViewById(R.id.txtPromoCatHeader);
            return txtPromoCatHeader;
        }
    }
}
