package com.bigbasket.mobileapp.adapter.product;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.common.FixedLayoutViewHolder;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.PromoDetailNavigationAware;
import com.bigbasket.mobileapp.model.promo.Promo;
import com.bigbasket.mobileapp.model.promo.PromoCategory;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.List;

public class PromoCategoryAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_PROMO = 0;
    public static final int VIEW_TYPE_CATEGORY = 1;
    public static final int VIEW_TYPE_SECTION = 2;
    private T context;
    private List<Object> promoConsolidatedList;
    private Typeface typeface;
    private View mSectionView;

    public PromoCategoryAdapter(T context, List<Object> promoConsolidatedList, Typeface typeface,
                                View sectionView) {
        this.context = context;
        this.typeface = typeface;
        this.promoConsolidatedList = promoConsolidatedList;
        this.mSectionView = sectionView;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = ((ActivityAware) context).getCurrentActivity().getLayoutInflater();
        switch (viewType) {
            case VIEW_TYPE_CATEGORY:
                View view = inflater.inflate(R.layout.uiv3_promo_category_list_header, parent, false);
                return new PromoCatViewHolder(view);
            case VIEW_TYPE_PROMO:
                view = inflater.inflate(R.layout.uiv3_promo_category_list_item, parent, false);
                return new PromoCatItemHolder(view);
            case VIEW_TYPE_SECTION:
                return new FixedLayoutViewHolder(mSectionView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_PROMO) {
            setPromoView(getActualPosition(position), (PromoCatItemHolder) holder);
        } else if (viewType == VIEW_TYPE_CATEGORY) {
            setPromoCategoryView(getActualPosition(position), (PromoCatViewHolder) holder);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mSectionView != null) {
            if (position == 0) {
                return VIEW_TYPE_SECTION;
            }
            return promoConsolidatedList.get(position - 1) instanceof Promo ? VIEW_TYPE_PROMO : VIEW_TYPE_CATEGORY;
        }
        return promoConsolidatedList.get(position) instanceof Promo ? VIEW_TYPE_PROMO : VIEW_TYPE_CATEGORY;
    }

    public int getActualPosition(int position) {
        if (mSectionView != null) {
            return position - 1;
        }
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return promoConsolidatedList.size() + (mSectionView != null ? 1 : 0);
    }

    private void setPromoView(int position, PromoCatItemHolder promoCatItemHolder) {
        final Promo promo = (Promo) promoConsolidatedList.get(position);
        ImageView imgPromoIcon = promoCatItemHolder.getImgPromoIcon();
        TextView txtPromoName = promoCatItemHolder.getTxtPromoName();
        TextView txtPromoDescLine1 = promoCatItemHolder.getTxtPromoDescLine1();
        TextView txtPromoDescLine2 = promoCatItemHolder.getTxtPromoDescLine2();

        UIUtil.displayAsyncImage(imgPromoIcon, promo.getPromoIcon());
        txtPromoName.setText(promo.getPromoName());
        txtPromoDescLine1.setText(promo.getPromoDescLine1());
        txtPromoDescLine2.setText(promo.getPromoDescLine2());
    }

    private void setPromoCategoryView(int position, PromoCatViewHolder promoCatViewHolder) {
        final PromoCategory promoCategory = (PromoCategory) promoConsolidatedList.get(position);
        TextView txtPromoCatHeader = promoCatViewHolder.getTxtPromoCatHeader();
        txtPromoCatHeader.setTypeface(typeface);
        txtPromoCatHeader.setText(promoCategory.getName());

        if (promoCategory.getDescription() != null
                && promoCategory.getDescription().length() > 0) {
            TextView txtPromoCatDesc = promoCatViewHolder.getTxtPromoCatDesc();
            txtPromoCatDesc.setTypeface(typeface);
            txtPromoCatDesc.setText(promoCategory.getDescription());
        }
    }

    private class PromoCatItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView imgPromoIcon;
        private TextView txtPromoName;
        private TextView txtPromoDescLine1;
        private TextView txtPromoDescLine2;

        private PromoCatItemHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

        }

        public ImageView getImgPromoIcon() {
            if (imgPromoIcon == null) {
                imgPromoIcon = (ImageView) itemView.findViewById(R.id.imgPromoIcon);
            }
            return imgPromoIcon;
        }

        public TextView getTxtPromoName() {
            if (txtPromoName == null) {
                txtPromoName = (TextView) itemView.findViewById(R.id.txtPromoName);
                txtPromoName.setTypeface(typeface);
            }
            return txtPromoName;
        }

        public TextView getTxtPromoDescLine1() {
            if (txtPromoDescLine1 == null) {
                txtPromoDescLine1 = (TextView) itemView.findViewById(R.id.txtPromoDescLine1);
            }
            return txtPromoDescLine1;
        }

        public TextView getTxtPromoDescLine2() {
            if (txtPromoDescLine2 == null) {
                txtPromoDescLine2 = (TextView) itemView.findViewById(R.id.txtPromoDescLine2);
            }
            return txtPromoDescLine2;
        }

        @Override
        public void onClick(View v) {
            Object possiblePromoObj = promoConsolidatedList.get(getPosition());
            if (possiblePromoObj instanceof Promo) {
                ((PromoDetailNavigationAware) context).loadPromoDetail((Promo) possiblePromoObj);
            }
        }
    }

    private class PromoCatViewHolder extends RecyclerView.ViewHolder {
        private TextView txtPromoCatHeader;
        private TextView txtPromoCatDesc;

        public PromoCatViewHolder(View itemView) {
            super(itemView);
        }

        private TextView getTxtPromoCatDesc() {
            if (txtPromoCatDesc == null)
                txtPromoCatDesc = (TextView) itemView.findViewById(R.id.txtPromoCatDesc);
            return txtPromoCatDesc;
        }

        private TextView getTxtPromoCatHeader() {
            if (txtPromoCatHeader == null)
                txtPromoCatHeader = (TextView) itemView.findViewById(R.id.txtPromoCatHeader);
            return txtPromoCatHeader;
        }
    }
}
