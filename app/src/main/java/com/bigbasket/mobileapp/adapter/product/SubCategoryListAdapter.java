package com.bigbasket.mobileapp.adapter.product;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.model.product.Category;

import java.util.List;

/**
 * Created by jugal on 12/11/14.
 */
public class SubCategoryListAdapter extends BaseExpandableListAdapter {
    private BaseFragment baseFragment;
    private List<Category> categoryList;
    private LayoutInflater layoutInflater;

    public SubCategoryListAdapter(BaseFragment baseFragment1, List<Category> categoryList, FragmentActivity fragmentActivity) {
        this.baseFragment = baseFragment1;
        this.categoryList = categoryList;
        layoutInflater = (LayoutInflater) fragmentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return categoryList.get(groupPosition).getCategory().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return categoryList.get(groupPosition).getCategory() != null ? categoryList.get(groupPosition).getCategory().size() : 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return categoryList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return categoryList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private class SubCatHolder {
        private View base;
        private TextView txtListText;
        private ImageView listArrow;

        private SubCatHolder(View base) {
            this.base = base;
        }

        private TextView getListText() {
            if (txtListText == null)
                txtListText = (TextView) base.findViewById(R.id.txtListText);
            return txtListText;
        }

        public ImageView getListArrow() {
            if (listArrow == null)
                listArrow = (ImageView) base.findViewById(R.id.listArrow);
            return listArrow;
        }
    }


    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final Category bottomCategory = (Category) getChild(groupPosition, childPosition);
        View row = convertView;
        SubCatHolder subCatHolder;
        if (row == null) {
            row = layoutInflater.inflate(R.layout.uiv3_list_text, null);
            subCatHolder = new SubCatHolder(row);
            row.setTag(subCatHolder);
        } else {
            subCatHolder = (SubCatHolder) row.getTag();
        }
        TextView txtListItem = subCatHolder.getListText();
        txtListItem.setText(bottomCategory.getName());

        if (childPosition == 0 || childPosition == 1) {
            txtListItem.setTextColor(baseFragment.getResources().getColor(R.color.active_order_red_color));
        } else {
            txtListItem.setTextColor(baseFragment.getResources().getColor(R.color.uiv3_list_primary_text_color));
        }

        ImageView imgArrow = subCatHolder.getListArrow();
        imgArrow.setVisibility(View.GONE);

        /*
        if (childPosition == 1) {
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //Intent for calling activity
                    Bundle bundle = new Bundle();
                    bundle.putString("slug_name_category", categoryList.get(groupPosition).getSlug());
                    CategoryProductsFragment categoryProductsFragment = new CategoryProductsFragment();
                    categoryProductsFragment.setArguments(bundle);
                    baseFragment.changeFragment(categoryProductsFragment);
                }
            });
        } else {
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //Intent for calling activity
                    Bundle bundle = new Bundle();
                    bundle.putString("slug_name_category", bottomCategory.getSlug());
                    CategoryProductsFragment categoryProductsFragment = new CategoryProductsFragment();
                    categoryProductsFragment.setArguments(bundle);
                    baseFragment.changeFragment(categoryProductsFragment);
                }
            });
        }

        */

        return row;
    }

    @Override
    public View getGroupView(int groupPosition, final boolean isExpanded, View convertView,
                             ViewGroup parent) {
        final Category subcategory = (Category) getGroup(groupPosition);
        View row = convertView;
        SubCatHolder subCatHolder;
        if (row == null) {
            row = layoutInflater.inflate(R.layout.uiv3_list_text, null);
            subCatHolder = new SubCatHolder(row);
            row.setTag(subCatHolder);
        } else {
            subCatHolder = (SubCatHolder) row.getTag();
        }
        TextView txtListItem = subCatHolder.getListText();
        txtListItem.setText(subcategory.getName());
        ImageView imgArrow = subCatHolder.getListArrow();
        if (groupPosition == 0 || groupPosition == 1 || groupPosition == 2) {
            txtListItem.setTextColor(baseFragment.getResources().getColor(R.color.active_order_red_color));
        } else {
            txtListItem.setTextColor(baseFragment.getResources().getColor(R.color.uiv3_list_primary_text_color));
        }

        if (subcategory.getCategory() != null && subcategory.getCategory().size() > 0) {
            imgArrow.setVisibility(View.VISIBLE);
        } else {
            imgArrow.setVisibility(View.GONE);
        }

        imgArrow.setImageResource(isExpanded ? R.drawable.ic_keyboard_arrow_down_grey600_24dp :
                R.drawable.ic_keyboard_arrow_right_grey600_24dp);

        /*
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (subcategory.getCategory() != null && subcategory.getCategory().size() > 0) {
                    if (!isExpanded) {
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                    } else {
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("slug_name_category", subcategory.getSlug());
                    CategoryProductsFragment categoryProductsFragment = new CategoryProductsFragment();
                    categoryProductsFragment.setArguments(bundle);
                    baseFragment.changeFragment(categoryProductsFragment);
                }
            }
        });
        /*
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (subcategory.getCategory() != null && subcategory.getCategory().size() > 0) {
                    if (!isExpanded) {
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                    } else {
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("slug_name_category", subcategory.getSlug());
                    CategoryProductsFragment categoryProductsFragment = new CategoryProductsFragment();
                    categoryProductsFragment.setArguments(bundle);
                    baseFragment.changeFragment(categoryProductsFragment);
                }
            }
        });

        */
        return row;
    }
}
