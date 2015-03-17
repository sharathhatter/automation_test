package com.bigbasket.mobileapp.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.util.FontHolder;

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
    public void bindView(View view, Context context, Cursor cursor) {
        int viewType = getItemViewType(cursor);
        String termString = cursor.getString(1);
        if (viewType == VIEW_TYPE_ITEM) {
            RowViewHolder rowViewHolder = (RowViewHolder) view.getTag();
            TextView txtTerm = rowViewHolder.getTxtTerm();
            txtTerm.setText(termString);
        } else {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) view.getTag();
            TextView txtTermHeader = headerViewHolder.getTxtTermHeader();
            txtTermHeader.setText(termString);
        }
    }

    private class RowViewHolder {
        private TextView txtTerm;
        private View itemRow;

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
