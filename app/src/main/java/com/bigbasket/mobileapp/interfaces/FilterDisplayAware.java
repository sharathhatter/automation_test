package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.product.FilterOptionCategory;
import com.bigbasket.mobileapp.model.product.FilteredOn;

import java.util.ArrayList;

public interface FilterDisplayAware {

    public void setFilterView(ArrayList<FilterOptionCategory> filterOptionCategories,
                              ArrayList<FilteredOn> filteredOn, String fragmentTag);
}
