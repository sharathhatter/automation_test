package com.bigbasket.mobileapp.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.db.MostSearchesDbHelper;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.OnSearchTermActionCallback;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.SearchUtil;
import com.bigbasket.mobileapp.util.UIUtil;

import java.lang.ref.WeakReference;

public class SearchViewAdapter<T> extends CursorAdapter {


    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    private LayoutInflater inflater;
    private FontHolder fontHolder;
    private SearchTermActionListener searchTermActionListener;

    public SearchViewAdapter(T context, Cursor contactCursor, OnSearchTermActionCallback onSearchTermActionCallback) {
        super(((AppOperationAware) context).getCurrentActivity(), contactCursor, false);
        this.inflater = LayoutInflater.from(((AppOperationAware) context).getCurrentActivity());
        this.fontHolder = FontHolder.getInstance(((AppOperationAware) context).getCurrentActivity());
        this.searchTermActionListener = new SearchTermActionListener(onSearchTermActionCallback,
                ((AppOperationAware) context).getCurrentActivity());
    }

    private static class SearchTermActionListener implements View.OnClickListener {
        private OnSearchTermActionCallback onSearchTermActionCallback;
        private Context context;

        public SearchTermActionListener(OnSearchTermActionCallback onSearchTermActionCallback, Context context) {
            this.onSearchTermActionCallback = onSearchTermActionCallback;
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            Object tagVal = v.getTag(R.id.search_history_term_tag_id);
            if (tagVal != null) {
                deleteTerm(String.valueOf(tagVal));
            } else {
                tagVal = v.getTag(R.id.search_suggestion_term_tag_id);
                if (tagVal != null) {
                    fillTerm(String.valueOf(tagVal));
                }
            }
        }

        private void deleteTerm(String term) {
            if (!TextUtils.isEmpty(term)) {
                MostSearchesDbHelper mostSearchesDbHelper = new MostSearchesDbHelper(context);
                mostSearchesDbHelper.deleteTerm(term);
                onSearchTermActionCallback.onSearchTermDeleted();
            }
        }

        private void fillTerm(String term) {
            if (!TextUtils.isEmpty(term)) {
                onSearchTermActionCallback.setSearchText(term);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void bindView(final View view, final Context context, Cursor cursor) {
        int viewType = getItemViewType(cursor);
        String termString = cursor.getString(1);
        if (viewType == VIEW_TYPE_ITEM) {
            RowViewHolder rowViewHolder = (RowViewHolder) view.getTag();
            TextView txtTerm = rowViewHolder.getTxtTerm();
            String term = termString.trim();
            int termLength = term.length();
            String constraint = getFilterQuery();
            if (!TextUtils.isEmpty(constraint)) {
                int startIndx = term.indexOf(constraint);
                int endIndx = startIndx > -1 ? startIndx + constraint.length() - 1 : -1;
                if (endIndx > 0) {
                    endIndx = Math.min(endIndx, termLength - 1);
                }
                if (endIndx > startIndx) {
                    SpannableString spannableString = new SpannableString(term);
                    spannableString.setSpan(new StyleSpan(Typeface.BOLD),
                            startIndx, endIndx + 1,
                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    txtTerm.setText(spannableString);
                } else {
                    txtTerm.setText(term);
                }
            } else {
                txtTerm.setText(term);
            }

            ImageView imgSearchTermAction = rowViewHolder.getImgSearchTermAction();
            String itemRightIconKey = getItemRightIcon(cursor);
            if (!TextUtils.isEmpty(itemRightIconKey)) {
                int drawableResId;
                if (itemRightIconKey.equals(SearchUtil.HISTORY_TERM)) {
                    imgSearchTermAction.setTag(R.id.search_history_term_tag_id, term);
                    imgSearchTermAction.setTag(R.id.search_suggestion_term_tag_id, null); // Reset
                    drawableResId = R.drawable.delete_product;
                } else {
                    imgSearchTermAction.setTag(R.id.search_history_term_tag_id, null); // Reset
                    imgSearchTermAction.setTag(R.id.search_suggestion_term_tag_id, term);
                    drawableResId = R.drawable.ic_arrow_angled_grey_24dp;
                }
                UIUtil.displayAsyncImage(imgSearchTermAction, drawableResId);
                imgSearchTermAction.setVisibility(View.VISIBLE);
            } else {
                imgSearchTermAction.setVisibility(View.GONE);
            }

        } else {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) view.getTag();
            TextView txtTermHeader = headerViewHolder.getTxtTermHeader();
            txtTermHeader.setText(termString);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor) getItem(position);
        return getItemViewType(cursor);
    }

    public int getItemViewType(Cursor cursor) {
        if (cursor.getString(5) != null) {
            return VIEW_TYPE_HEADER;
        }
        return VIEW_TYPE_ITEM;
    }

    @Nullable
    private String getFilterQuery() {
        if (getFilterQueryProvider() instanceof SearchFilterQueryProvider) {
            return ((SearchFilterQueryProvider) getFilterQueryProvider()).getConstraint();
        }
        return null;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == VIEW_TYPE_ITEM;
    }

    public String getItemRightIcon(Cursor cursor) {
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

    private class RowViewHolder {
        private TextView txtTerm;
        private View itemRow;
        private ImageView imgSearchTermAction;

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

        public ImageView getImgSearchTermAction() {
            if (imgSearchTermAction == null) {
                imgSearchTermAction = (ImageView) itemRow.findViewById(R.id.imgSearchTermAction);
                imgSearchTermAction.setOnClickListener(searchTermActionListener);
            }
            return imgSearchTermAction;
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
            }
            return txtTermHeader;
        }
    }

    public static class SearchFilterQueryProvider implements FilterQueryProvider {

        private String constraint;
        private WeakReference<Context> context;

        @Nullable
        public String getConstraint() {
            return constraint;
        }

        public SearchFilterQueryProvider(Context context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        public Cursor runQuery(CharSequence constraint) {
            if (context != null && context.get() != null && constraint != null) {
                this.constraint = constraint.toString();
                return SearchUtil.searchQueryCall(constraint.toString(),
                        context.get().getApplicationContext());
            }
            return null;
        }
    }
}
