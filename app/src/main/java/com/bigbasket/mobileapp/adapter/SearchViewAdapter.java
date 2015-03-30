package com.bigbasket.mobileapp.adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.SearchableActivity;
import com.bigbasket.mobileapp.adapter.db.MostSearchesAdapter;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.SearchUtil;

public class SearchViewAdapter<T> extends CursorAdapter implements Filterable {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    private LayoutInflater inflater;
    private FontHolder fontHolder;

    public SearchViewAdapter(T context, Cursor contactCursor) {
        super(((ActivityAware) context).getCurrentActivity(), contactCursor, false);
        this.inflater = LayoutInflater.from(((ActivityAware) context).getCurrentActivity());
        this.fontHolder = FontHolder.getInstance(((ActivityAware) context).getCurrentActivity());
    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {
        int viewType = getItemViewType(cursor);
        String termString = cursor.getString(1);
        if (viewType == VIEW_TYPE_ITEM) {
            RowViewHolder rowViewHolder = (RowViewHolder) view.getTag();
            TextView txtTerm = rowViewHolder.getTxtTerm();
            txtTerm.setText(termString);

            ImageView imgSearchListIcon = rowViewHolder.getImgSearchListIcon();
            imgSearchListIcon.setImageDrawable(getItemLeftIcon(cursor).equals(SearchUtil.SEARCH_LEFT_ICON) ?
                    context.getResources().getDrawable(R.drawable.ic_search_black_24dp) :
                    context.getResources().getDrawable(R.drawable.ic_restore_black_24dp));


            ImageView imgRemoveTerm = rowViewHolder.getImgRemoveTerm();
            if(getItemRightIcon(cursor)!=null && getItemRightIcon(cursor).equals(SearchUtil.CROSS_ICON)){
                imgRemoveTerm.setVisibility(View.VISIBLE);
                imgRemoveTerm.setTag(termString);
                imgRemoveTerm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String deleteTerm = String.valueOf(v.getTag());
                        if(!TextUtils.isEmpty(deleteTerm)) {
                            MostSearchesAdapter mostSearchesAdapter = new MostSearchesAdapter(context);
                            mostSearchesAdapter.deleteTerm(deleteTerm);
                            ((SearchableActivity)context).notifySearchTermAdapter();
                        }
                    }
                });
            }else {
                imgRemoveTerm.setVisibility(View.GONE);
            }

        } else {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) view.getTag();
            TextView txtTermHeader = headerViewHolder.getTxtTermHeader();
            txtTermHeader.setText(termString);
        }
    }

    private class RowViewHolder {
        private TextView txtTerm;
        private View itemRow;
        private ImageView imgRemoveTerm;
        private ImageView imgSearchListIcon;

        private RowViewHolder(View itemRow) {
            this.itemRow = itemRow;
        }

        public TextView getTxtTerm() {
            if (txtTerm == null) {
                txtTerm = (TextView) itemRow.findViewById(R.id.txtTerm);
                txtTerm.setTypeface(fontHolder.getFaceRobotoRegular());
            }
            return txtTerm;
        }

        public ImageView getImgRemoveTerm() {
            if (imgRemoveTerm == null) {
                imgRemoveTerm = (ImageView) itemRow.findViewById(R.id.imgRemoveTerm);
            }
            return imgRemoveTerm;
        }

        public ImageView getImgSearchListIcon() {
            if (imgSearchListIcon == null) {
                imgSearchListIcon = (ImageView) itemRow.findViewById(R.id.imgSearchListIcon);
            }
            return imgSearchListIcon;
        }
    }

    private class HeaderViewHolder {
        private TextView txtTermHeader;
        private View itemRow;

        private HeaderViewHolder(View itemRow) {
            this.itemRow = itemRow;
            itemRow.setClickable(false);
            itemRow.setFocusable(false);
        }

        public TextView getTxtTermHeader() {
            if (txtTermHeader == null) {
                txtTermHeader = (TextView) itemRow.findViewById(R.id.txtTermHeader);
                txtTermHeader.setTypeface(fontHolder.getFaceRobotoRegular());
            }
            return txtTermHeader;
        }
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor) getItem(position);
        return getItemViewType(cursor);
    }

    public int getItemViewType(Cursor cursor) {
        if (cursor.getString(4) == null) {
            return VIEW_TYPE_HEADER;
        }
        return VIEW_TYPE_ITEM;
    }

    public String getItemLeftIcon(Cursor cursor){
        return cursor.getString(5);
    }

    public String getItemRightIcon(Cursor cursor){
        return cursor.getString(6);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor);
        if (viewType == VIEW_TYPE_ITEM) {
            View view = inflater.inflate(R.layout.search_row, parent, false);
            RowViewHolder rowViewHolder = new RowViewHolder(view);
            view.setTag(rowViewHolder);
            return view;
        } else {
            View view = inflater.inflate(R.layout.search_row_header, parent, false);
            HeaderViewHolder headerViewHolder = new HeaderViewHolder(view);
            view.setTag(headerViewHolder);
            return view;
        }
    }
}
